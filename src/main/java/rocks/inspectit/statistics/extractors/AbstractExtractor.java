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
import rocks.inspectit.statistics.entities.EventEntity;
import rocks.inspectit.statistics.source.CSVFTPSource;
import rocks.inspectit.statistics.source.CSVSource;
import rocks.inspectit.statistics.source.IDataSource;
import rocks.inspectit.statistics.source.InfluxDBSource;

public abstract class AbstractExtractor<T extends AbstractStatisticsEntity> implements IBackupImporter<T> {
	/**
	 * 5 hours time offset.
	 */
	private static final long TIME_OFFSET = 1000L * 60 * 60 * 20;
	private final Properties properties;
	private String apiUri;
	private T template;
	protected IDataSource<T> influxDBSource;
	protected IDataSource<T> csvFtpDataSource;
	private long absoluteCountsSinceTime;

	public AbstractExtractor(Properties properties) {
		this.properties = properties;
		initProperties(properties);
		checkProperties(properties);
	}

	/**
	 * @param apiUri
	 * @param template
	 * @param influxDBSource
	 * @param csvImportDataSource
	 * @param csvExportDataSource
	 * @param absoluteCountsSinceTime
	 */
	public void init(String apiUri, T template,InfluxDB influxDB, long absoluteCountsSinceTime) {
		this.apiUri = apiUri;
		this.template = template;
		this.influxDBSource = new InfluxDBSource<T>(influxDB, properties.getProperty(StatisticsExtractor.INFLUX_DB_DATABASE_KEY));
		this.csvFtpDataSource = new CSVFTPSource<T>("backup_" + template.getMeasurementName() + ".csv", getProperties().getProperty(StatisticsExtractor.FTP_USER_KEY), getProperties().getProperty(StatisticsExtractor.FTP_PASSWORD_KEY), getProperties().getProperty(StatisticsExtractor.FTP_HOSTNAME_KEY), getProperties().getProperty(StatisticsExtractor.FTP_DIRECTORY_KEY));
		this.absoluteCountsSinceTime = absoluteCountsSinceTime;
	}

	protected String getJSONString() {
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
	 * Retrieves and stores statistics.
	 * 
	 * @throws IOException
	 */
	public List<T> retrieveStatistics() throws IOException {
		return getResultList(getJSONString());
	}

	public void storeResultsToDatabase(final List<T> resultList) {
		filterExistingEntries(resultList, influxDBSource);
		calculateRelativeCounts(resultList, influxDBSource);

		// store to database and csv
		if (!resultList.isEmpty()) {
			influxDBSource.store(resultList);
		} else {
			System.out.println("No new entries for " + template.getMeasurementName() + "!");
		}
	}

	public void createBackup(final List<T> resultList) throws IOException {
		System.out.println("Creating Backup for " + template.getMeasurementName());
		
		List<T> newList = new ArrayList<T>(resultList.size());
		newList.addAll(resultList);
		filterExistingEntries(newList, csvFtpDataSource);
		calculateRelativeCounts(newList, csvFtpDataSource);

		csvFtpDataSource.store(newList);

		System.out.println("Backup Succeeded");
	}

	protected void calculateRelativeCounts(final List<T> resultList, IDataSource<T> dataSource) {
		// calculate relative counts
		if (needsRelativationOfValues() && !resultList.isEmpty()) {
			for (T entity : resultList) {
				Map<String, Number> absoluteCounts = dataSource.getAbsoluteCounts(absoluteCountsSinceTime, entity.getIdentifier(), template);
				if (null != absoluteCounts && !absoluteCounts.isEmpty()) {
					Map<String, Object> relativeCounts = new HashMap<String, Object>();
					Map<String, Object> fieldValues = entity.getFieldValues();
					for (String key : absoluteCounts.keySet()) {
						relativeCounts.put(key, ((Number) fieldValues.get(key)).doubleValue() - absoluteCounts.get(key).doubleValue());
					}
					entity.setFields(relativeCounts);
				}
			}
		}
	}

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
	public abstract List<T> getResultList(String jsonString);

	protected abstract boolean needsRelativationOfValues();

	protected abstract void initProperties(Properties properties);

	protected abstract void checkProperties(Properties properties);

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
