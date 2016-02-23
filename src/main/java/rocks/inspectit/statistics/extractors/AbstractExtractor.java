package rocks.inspectit.statistics.extractors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.influxdb.InfluxDB;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import rocks.inspectit.statistics.IBackupImporter;
import rocks.inspectit.statistics.StatisticsExtractor;
import rocks.inspectit.statistics.entities.AbstractStatisticsEntity;
import rocks.inspectit.statistics.entities.EntityField.MetricType;
import rocks.inspectit.statistics.source.CSVFTPSource;
import rocks.inspectit.statistics.source.IDataSource;
import rocks.inspectit.statistics.source.InfluxDBSource;

public abstract class AbstractExtractor<T extends AbstractStatisticsEntity> implements IBackupImporter<T> {
	/**
	 * 5 hours time offset.
	 */
	private static final long TIME_OFFSET = 1000L * 60 * 60 * 20;

	/**
	 * Properties.
	 */
	private final Properties properties;

	/**
	 * Template instnace.
	 */
	private T template;

	/**
	 * Influx DB source.
	 */
	protected IDataSource<T> influxDBSource;

	/**
	 * CSV FTP source.
	 */
	protected IDataSource<T> csvFtpDataSource;

	/**
	 * Timestamp from which on absolute counts shell be calculated.
	 */
	protected long absoluteCountsSinceTime;

	/**
	 * Constructor.
	 * 
	 * @param properties
	 *            extractor properties.
	 */
	public AbstractExtractor(Properties properties) {
		this.properties = properties;
		initProperties(properties);
		checkProperties(properties);
	}

	/**
	 * Initializes the extractor.
	 * 
	 * @param template
	 *            template instance of the entity type
	 * @param influxDB
	 *            influx DB connector
	 * @param absoluteCountsSinceTime
	 *            timestamp from which on absolute counts shell be calculated.
	 */

	public void init(T template, InfluxDB influxDB) {
		this.template = template;
		this.influxDBSource = new InfluxDBSource<T>(influxDB, properties.getProperty(StatisticsExtractor.INFLUX_DB_DATABASE_KEY));
		this.csvFtpDataSource = new CSVFTPSource<T>("backup_" + template.getMeasurementName() + ".csv", getProperties().getProperty(StatisticsExtractor.FTP_USER_KEY), getProperties().getProperty(
				StatisticsExtractor.FTP_PASSWORD_KEY), getProperties().getProperty(StatisticsExtractor.FTP_HOSTNAME_KEY), getProperties().getProperty(StatisticsExtractor.FTP_DIRECTORY_KEY));
	}

	/**
	 * Retrieves the JSON String from the given API URI.
	 * 
	 * @param apiUri
	 *            API URI to send the request to
	 * @return JSON string
	 */
	protected String getJSONString(String apiUri) {
		if (null != apiUri) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			WebTarget target = client.target(apiUri);
			Response response = target.request().header("User-Agent", "DownloadCounter").get();
			String jsonString = response.readEntity(String.class);
			response.close();
			return jsonString;
		} else {
			return null;
		}

	}

	/**
	 * Stores results to database.
	 * 
	 * @param resultList
	 *            results to store
	 */
	public void storeResultsToDatabase(final List<T> resultList) {
		if (!resultList.isEmpty()) {
			influxDBSource.store(resultList);
		} else {
			System.out.println("No new entries for " + template.getMeasurementName() + "!");
		}
	}

	/**
	 * Processes data including filtering of existing entries, calculation of relative counts and
	 * filtering of empty entries.
	 * 
	 * @param resultList
	 *            the result list
	 */
	public void preprocessData(final List<T> resultList) {
		if (!resultList.isEmpty()) {
			filterExistingEntries(resultList, csvFtpDataSource);
			calculateRelativeCounts(resultList, csvFtpDataSource);
			filterEmptyEntries(resultList, csvFtpDataSource);
		}
	}

	/**
	 * Filters out empty entries
	 * 
	 * @param resultList
	 * @param dataSource
	 */
	private void filterEmptyEntries(final List<T> resultList, IDataSource<T> dataSource) {
		if (!resultList.isEmpty()) {
			List<T> newList = new ArrayList<T>();
			for (T entity : resultList) {
				T before = dataSource.getLast(entity.getIdentifier(), template);
				if (before == null || entity.hasNewInformation(before)) {
					newList.add(entity);
				}
			}
			resultList.clear();
			resultList.addAll(newList);
		}
	}

	/**
	 * Creates backup.
	 * 
	 * @param resultList
	 * @throws IOException
	 */
	public void createBackup(final List<T> resultList) throws IOException {
		System.out.println("Creating Backup for " + template.getMeasurementName());

		csvFtpDataSource.store(resultList);

		System.out.println("Backup Succeeded");
	}

	/**
	 * Calculates relative counts from absolute counts.
	 * 
	 * @param resultList
	 * @param dataSource
	 */
	protected void calculateRelativeCounts(final List<T> resultList, IDataSource<T> dataSource) {
		// calculate relative counts
		if (needsRelativationOfValues() && !resultList.isEmpty()) {
			for (T entity : resultList) {
				Map<String, Number> absoluteCounts = dataSource.getAbsoluteCounts(absoluteCountsSinceTime, entity.getIdentifier(), template);
				if (null != absoluteCounts && !absoluteCounts.isEmpty()) {
					Map<String, Object> relativeCounts = new HashMap<String, Object>();
					Map<String, Object> fieldValues = entity.getFieldValues(MetricType.RELATIVE);
					for (String key : absoluteCounts.keySet()) {
						relativeCounts.put(key, ((Number) fieldValues.get(key)).doubleValue() - absoluteCounts.get(key).doubleValue());
					}
					entity.setFields(relativeCounts);
				}
			}
		}
	}

	/**
	 * Filters already existing entries.
	 * 
	 * @param resultList
	 * @param dataSource
	 */
	protected void filterExistingEntries(final List<T> resultList, IDataSource<T> dataSource) {
		// filter already existing entries
		long timestampThreshold = getLatestTimestamp(dataSource);
		List<T> filteredList = new ArrayList<T>();
		for (T entity : resultList) {
			if (entity.getTimestamp() > timestampThreshold + TIME_OFFSET) {
				filteredList.add(entity);
			}
		}
		resultList.clear();
		resultList.addAll(filteredList);
	}

	/**
	 * Returns the latest timestamp from the given data source.
	 * 
	 * @param dataSource
	 * @return
	 */
	protected long getLatestTimestamp(IDataSource<T> dataSource) {
		return dataSource.getLatestTimestamp(template);
	}

	/**
	 * Creates a list of Download Statistics for individual artifacts.
	 * 
	 * @param jsonString
	 *            the retrieved jsonString
	 * @return the resultList
	 */
	public abstract List<T> getResultList();

	protected abstract boolean needsRelativationOfValues();

	protected abstract void initProperties(Properties properties);

	protected abstract void checkProperties(Properties properties);

	public T getTemplate() {
		return template;
	}

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	@Override
	public void importBackup() {
		System.out.println("Importing Data from backup for " + template.getMeasurementName() + "...");
		List<T> oldData = csvFtpDataSource.load(template);
		influxDBSource.store(oldData);
		System.out.println("Backup imported.");
	}
}
