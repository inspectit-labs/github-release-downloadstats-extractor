package rocks.inspectit.statistics.extractors;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.time.DateUtils;
import org.influxdb.InfluxDB;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rocks.inspectit.statistics.Constants;
import rocks.inspectit.statistics.StatisticsExtractor;
import rocks.inspectit.statistics.entities.DockerhubStatisticsEtity;
import rocks.inspectit.statistics.source.CSVSource;
import rocks.inspectit.statistics.source.InfluxDBSource;

public class DockerhubStatisticsExtractor extends AbstractExtractor<DockerhubStatisticsEtity> {
	private static final String URL_KEY = "dockerhub.api.url";
	private static final String EXPORT_CSV_FILE_KEY = "dockerhub.target.csv.file.export";
	private static final String IMPORT_CSV_FILE_KEY = "dockerhub.target.csv.file.import";
	private static final String IMPORT_FROM_CSV = "dockerhub.import.from.csv.before";

	public DockerhubStatisticsExtractor(Properties properties, InfluxDB influxDB) {
		super(properties);

		InfluxDBSource<DockerhubStatisticsEtity> influxDBSource = new InfluxDBSource<DockerhubStatisticsEtity>(influxDB, properties.getProperty(StatisticsExtractor.INFLUX_DB_DATABASE_KEY));
		CSVSource<DockerhubStatisticsEtity> csvExportDataSource = new CSVSource<DockerhubStatisticsEtity>(properties.getProperty(EXPORT_CSV_FILE_KEY));
		boolean importFromCsv = Boolean.parseBoolean(properties.getProperty(IMPORT_FROM_CSV, "false"));
		CSVSource<DockerhubStatisticsEtity> csvImportDataSource = null;
		if (importFromCsv) {
			csvImportDataSource = new CSVSource<DockerhubStatisticsEtity>(properties.getProperty(IMPORT_CSV_FILE_KEY));
		}

		init(getProperties().getProperty(URL_KEY), DockerhubStatisticsEtity.getTemplate(), influxDBSource, csvImportDataSource, csvExportDataSource, 0L);

	}

	@Override
	protected List<DockerhubStatisticsEtity> getResultList(String jsonString) {
		System.out.println("Retrieving Docker Hub statistics...");
		List<DockerhubStatisticsEtity> statistics = new ArrayList<DockerhubStatisticsEtity>();
		try {
			long timestamp = System.currentTimeMillis();
			JSONObject jsonObject = new JSONObject(jsonString);
			Date roundedDate = DateUtils.truncate(new Date(timestamp), Calendar.DATE);

			JSONArray repositories = jsonObject.getJSONArray("results");
			for (int z = 0; z < repositories.length(); z++) {
				String name = repositories.getJSONObject(z).get("name").toString();
				int starCount = Integer.parseInt(repositories.getJSONObject(z).get("star_count").toString());
				int pullCount = Integer.parseInt(repositories.getJSONObject(z).get("pull_count").toString());

				DockerhubStatisticsEtity entity = new DockerhubStatisticsEtity(roundedDate.getTime(), name, pullCount, starCount);
				statistics.add(entity);
			}

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Successfully retrieved Docker Hub statistics.");
		return statistics;
	}

	@Override
	protected boolean fieldCountsAbsolute() {
		return true;
	}

	@Override
	protected void initProperties(Properties properties) {
		if (!properties.contains(URL_KEY) && System.getenv(URL_KEY) != null) {
			properties.setProperty(URL_KEY, System.getenv(URL_KEY));
		}

		if (!properties.contains(EXPORT_CSV_FILE_KEY) && System.getenv(EXPORT_CSV_FILE_KEY) != null) {
			properties.setProperty(EXPORT_CSV_FILE_KEY, System.getenv(EXPORT_CSV_FILE_KEY));
		}

		if (!properties.contains(IMPORT_FROM_CSV) && System.getenv(IMPORT_FROM_CSV) != null) {
			properties.setProperty(IMPORT_FROM_CSV, System.getenv(IMPORT_FROM_CSV));
		}
		if (!properties.contains(IMPORT_CSV_FILE_KEY) && System.getenv(IMPORT_CSV_FILE_KEY) != null) {
			properties.setProperty(IMPORT_CSV_FILE_KEY, System.getenv(IMPORT_CSV_FILE_KEY));
		}
	}

	@Override
	protected void checkProperties(Properties properties) {
		if (!properties.containsKey(URL_KEY)) {
			throw new IllegalArgumentException("GitHub API URL not specified: " + URL_KEY);
		}
		if (!properties.containsKey(EXPORT_CSV_FILE_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + EXPORT_CSV_FILE_KEY);
		}
		if (Boolean.parseBoolean(properties.getProperty(IMPORT_FROM_CSV, "false")) && !properties.containsKey(IMPORT_CSV_FILE_KEY)) {
			throw new IllegalArgumentException("Property not specified although enabled: " + IMPORT_CSV_FILE_KEY);
		}

	}

}
