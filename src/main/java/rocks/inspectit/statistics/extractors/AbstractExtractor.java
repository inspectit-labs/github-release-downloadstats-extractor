package rocks.inspectit.statistics.extractors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import rocks.inspectit.statistics.entities.AbstractStatisticsEntity;
import rocks.inspectit.statistics.source.IDataSource;

public abstract class AbstractExtractor<T extends AbstractStatisticsEntity> {
	/**
	 * 5 hours time offset.
	 */
	private static final long TIME_OFFSET = 1000L * 60 * 60 * 20;
	private final Properties properties;
	private String apiUri;
	private T template;
	private IDataSource<T> influxDBSource;
	private IDataSource<T> csvImportDataSource;
	private IDataSource<T> csvExportDataSource;
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
	public void init(String apiUri, T template, IDataSource<T> influxDBSource, IDataSource<T> csvImportDataSource, IDataSource<T> csvExportDataSource, long absoluteCountsSinceTime) {
		this.apiUri = apiUri;
		this.template = template;
		this.influxDBSource = influxDBSource;
		this.csvImportDataSource = csvImportDataSource;
		this.csvExportDataSource = csvExportDataSource;
		this.absoluteCountsSinceTime = absoluteCountsSinceTime;
	}

	protected String getJSONString() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		WebTarget target = client.target(apiUri);
		Response response = target.request().header("User-Agent", "DownloadCounter").get();
		String jsonString = response.readEntity(String.class);
		response.close();
		return jsonString;
	}

	/**
	 * Retrieves and stores statistics.
	 * 
	 * @throws IOException
	 */
	public void retrieveStatistics() throws IOException {

		// import old data
		if (null != csvImportDataSource) {
			System.out.println("Importing Data from backup for " + template.getMeasurementName() + "...");
			List<T> oldData = csvImportDataSource.load(template);
			influxDBSource.store(oldData);
			System.out.println("Backup imported.");
		}

		// get results
		List<T> resultList = getResultList(getJSONString());

		// filter already existing entries
		long timestampThreshold = getLatestTimestamp();
		List<T> filteredList = new ArrayList<T>();
		for (T entity : resultList) {
			if (entity.getTimestamp() > timestampThreshold + TIME_OFFSET) {
				filteredList.add(entity);
			}
		}
		resultList = filteredList;

		// calculate relative counts
		if (needsRelativationOfValues() && !resultList.isEmpty()) {
			for (T entity : resultList) {
				Map<String, Number> absoluteCounts = influxDBSource.getAbsoluteCounts(absoluteCountsSinceTime, entity.getIdentifier(), template);
				if (null != absoluteCounts) {
					Map<String, Object> relativeCounts = new HashMap<String, Object>();
					Map<String, Object> fieldValues = entity.getFieldValues();
					for (String key : absoluteCounts.keySet()) {
						relativeCounts.put(key, ((Number) fieldValues.get(key)).doubleValue() - absoluteCounts.get(key).doubleValue());
					}
					entity.setFields(relativeCounts);
				}
			}
		}

		// store to database and csv
		if (!resultList.isEmpty()) {
			influxDBSource.store(resultList);
		} else {
			System.out.println("No new entries for " + template.getMeasurementName() + "!");
		}
		csvExportDataSource.store(influxDBSource.load(template));

	}

	protected long getLatestTimestamp() {
		return influxDBSource.getLatestTimestamp(template);
	}

	/**
	 * Creates a list of Download Statistics for individual artifacts.
	 * 
	 * @param jsonString
	 *            the retrieved jsonString
	 * @return the resultList
	 */
	protected abstract List<T> getResultList(String jsonString);

	protected abstract boolean needsRelativationOfValues();

	protected abstract void initProperties(Properties properties);

	protected abstract void checkProperties(Properties properties);

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

}
