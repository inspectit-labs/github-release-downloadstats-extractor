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

import rocks.inspectit.statistics.StatisticsExtractor;
import rocks.inspectit.statistics.entities.GithubRepositoryStatisticsEntity;
import rocks.inspectit.statistics.source.CSVSource;
import rocks.inspectit.statistics.source.InfluxDBSource;

public class GithubRepositoryStatisticsExtractor extends AbstractExtractor<GithubRepositoryStatisticsEntity> {
	private static final String URL_KEY = "github.repository.api.url";

	private static final String EXPORT_CSV_FILE_KEY = "github.repository.target.csv.file.export";
	private static final String IMPORT_CSV_FILE_KEY = "github.repository.target.csv.file.import";
	private static final String IMPORT_FROM_CSV = "github.repository.import.from.csv.before";

	public GithubRepositoryStatisticsExtractor(Properties properties, InfluxDB influxDB) {
		super(properties);
		InfluxDBSource<GithubRepositoryStatisticsEntity> influxDBSource = new InfluxDBSource<GithubRepositoryStatisticsEntity>(influxDB,
				properties.getProperty(StatisticsExtractor.INFLUX_DB_DATABASE_KEY));
		CSVSource<GithubRepositoryStatisticsEntity> csvExportDataSource = new CSVSource<GithubRepositoryStatisticsEntity>(properties.getProperty(EXPORT_CSV_FILE_KEY));
		boolean importFromCsv = Boolean.parseBoolean(properties.getProperty(IMPORT_FROM_CSV, "false"));
		CSVSource<GithubRepositoryStatisticsEntity> csvImportDataSource = null;
		if (importFromCsv) {
			csvImportDataSource = new CSVSource<GithubRepositoryStatisticsEntity>(properties.getProperty(IMPORT_CSV_FILE_KEY));
		}

		init(getProperties().getProperty(URL_KEY), GithubRepositoryStatisticsEntity.getTemplate(), influxDBSource, csvImportDataSource, csvExportDataSource, 0L);

	}

	@Override
	protected List<GithubRepositoryStatisticsEntity> getResultList(String jsonString) {
		System.out.println("Retrieving Github repositories statistics...");
		List<GithubRepositoryStatisticsEntity> statistics = new ArrayList<GithubRepositoryStatisticsEntity>();
		try {
			long timestamp = System.currentTimeMillis();
			Date roundedDate = DateUtils.truncate(new Date(timestamp), Calendar.DATE);

			JSONArray jsonArray = new JSONArray(jsonString);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String repoName = jsonObject.get("name").toString();
				int size = Integer.parseInt(jsonObject.get("size").toString());
				int stars = Integer.parseInt(jsonObject.get("stargazers_count").toString());
				int watchers = Integer.parseInt(jsonObject.get("watchers_count").toString());
				int forks = Integer.parseInt(jsonObject.get("forks_count").toString());
				int openIssues = Integer.parseInt(jsonObject.get("open_issues_count").toString());

				GithubRepositoryStatisticsEntity entity = new GithubRepositoryStatisticsEntity(roundedDate.getTime(), repoName, size, stars, watchers, forks, openIssues);

				statistics.add(entity);

			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Successfully retrieved Github repositories statistics.");
		return statistics;
	}

	@Override
	protected boolean needsRelativationOfValues() {
		return false;
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
