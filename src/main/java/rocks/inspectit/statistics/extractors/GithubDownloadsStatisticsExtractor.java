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

import rocks.inspectit.statistics.Constants;
import rocks.inspectit.statistics.StatisticsExtractor;
import rocks.inspectit.statistics.entities.GithubDownloadStatisticsEntity;
import rocks.inspectit.statistics.source.CSVSource;
import rocks.inspectit.statistics.source.InfluxDBSource;

public class GithubDownloadsStatisticsExtractor extends AbstractExtractor<GithubDownloadStatisticsEntity> {
	private static final String URL_KEY = "github.api.url";
	private static final String DOWNLOAD_URL_KEY = "github.downloads.inspectit.download.url";

	private static final String EXPORT_CSV_FILE_KEY = "github.downloads.target.csv.file.export";
	private static final String IMPORT_CSV_FILE_KEY = "github.downloads.target.csv.file.import";
	private static final String IMPORT_FROM_CSV = "github.downloads.import.from.csv.before";

	public GithubDownloadsStatisticsExtractor(Properties properties, InfluxDB influxDB) {
		super(properties);

		InfluxDBSource<GithubDownloadStatisticsEntity> influxDBSource = new InfluxDBSource<GithubDownloadStatisticsEntity>(influxDB, properties.getProperty(StatisticsExtractor.INFLUX_DB_DATABASE_KEY));
		CSVSource<GithubDownloadStatisticsEntity> csvExportDataSource = new CSVSource<GithubDownloadStatisticsEntity>(properties.getProperty(EXPORT_CSV_FILE_KEY));
		boolean importFromCsv = Boolean.parseBoolean(properties.getProperty(IMPORT_FROM_CSV, "false"));
		CSVSource<GithubDownloadStatisticsEntity> csvImportDataSource = null;
		if (importFromCsv) {
			csvImportDataSource = new CSVSource<GithubDownloadStatisticsEntity>(properties.getProperty(IMPORT_CSV_FILE_KEY));
		}

		init(getProperties().getProperty(URL_KEY), GithubDownloadStatisticsEntity.getTemplate(), influxDBSource, csvImportDataSource, csvExportDataSource, Constants.GITHUB_DATA_SINCE_TIMESTAMP);

	}

	@Override
	protected void initProperties(Properties properties) {
		if (!properties.contains(URL_KEY) && System.getenv(URL_KEY) != null) {
			properties.setProperty(URL_KEY, System.getenv(URL_KEY));
		}
		if (!properties.contains(DOWNLOAD_URL_KEY) && System.getenv(DOWNLOAD_URL_KEY) != null) {
			properties.setProperty(DOWNLOAD_URL_KEY, System.getenv(DOWNLOAD_URL_KEY));
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
		if (!properties.containsKey(DOWNLOAD_URL_KEY)) {
			throw new IllegalArgumentException("InspectIT Download URL not specified: " + DOWNLOAD_URL_KEY);
		}
		if (!properties.containsKey(EXPORT_CSV_FILE_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + EXPORT_CSV_FILE_KEY);
		}
		if (Boolean.parseBoolean(properties.getProperty(IMPORT_FROM_CSV, "false")) && !properties.containsKey(IMPORT_CSV_FILE_KEY)) {
			throw new IllegalArgumentException("Property not specified although enabled: " + IMPORT_CSV_FILE_KEY);
		}

	}

	@Override
	protected List<GithubDownloadStatisticsEntity> getResultList(String jsonString) {
		System.out.println("Retrieving Github download statistics...");
		List<GithubDownloadStatisticsEntity> statistics = new ArrayList<GithubDownloadStatisticsEntity>();
		try {
			long timestamp = System.currentTimeMillis();

			JSONArray jsonArray = new JSONArray(jsonString);
			Date roundedDate = DateUtils.truncate(new Date(timestamp), Calendar.DATE);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONArray assets = jsonArray.getJSONObject(i).getJSONArray("assets");
				for (int z = 0; z < assets.length(); z++) {
					String browserDownloadUrl = assets.getJSONObject(z).get("browser_download_url").toString();
					browserDownloadUrl = browserDownloadUrl.replace(getProperties().getProperty(DOWNLOAD_URL_KEY), "");
					String fullVersion = browserDownloadUrl.substring(0, browserDownloadUrl.indexOf('/'));
					String buildNr = "1";
					String majorVersion = "";
					String minorVersion = "";
					if (fullVersion.split("\\.").length == 3) {
						minorVersion = fullVersion.substring(fullVersion.lastIndexOf('.') + 1);
						majorVersion = fullVersion.substring(0, fullVersion.lastIndexOf('.'));
					} else if (fullVersion.split("\\.").length == 4) {
						buildNr = fullVersion.substring(fullVersion.lastIndexOf('.') + 1);
						String fullMinorVersion = fullVersion.substring(0, fullVersion.lastIndexOf('.'));
						minorVersion = fullMinorVersion.substring(fullMinorVersion.lastIndexOf('.') + 1);
						majorVersion = fullMinorVersion.substring(0, fullMinorVersion.lastIndexOf('.'));
					}
					String artifactDescription = assets.getJSONObject(z).get("name").toString();
					int downloadCount = Integer.parseInt(assets.getJSONObject(z).get("download_count").toString());
					String artifactName;
					if (artifactDescription.startsWith("inspectit-agent")) {
						artifactName = "Agent";
					} else if (artifactDescription.startsWith("inspectit-cmr")) {
						artifactName = "CMR";
					} else if (artifactDescription.startsWith("inspectit.installer-all")) {
						artifactName = "Installer";
					} else {
						artifactName = "Client";
					}

					GithubDownloadStatisticsEntity statisticsPoint = new GithubDownloadStatisticsEntity(roundedDate.getTime(), artifactName, getOSLabel(artifactDescription),
							getArchitectureLabel(artifactDescription), majorVersion, minorVersion, buildNr, downloadCount);
					statistics.add(statisticsPoint);
				}
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Successfully retrieved Github download statistics.");
		return statistics;
	}

	/**
	 * Interprets OS label.
	 * 
	 * @param string
	 *            String to analyze
	 * @return the label
	 */
	private String getOSLabel(String string) {
		if (string.contains("linux")) {
			return "Linux";
		} else if (string.contains("windows") || string.contains("win32") || string.contains("win")) {
			return "Windows";
		} else if (string.contains("macosx")) {
			return "MacOS";
		}
		return "undefined";
	}

	/**
	 * Interprets Architecture label (32 / 64 bit).
	 * 
	 * @param string
	 *            String to analyze
	 * @return the label
	 */
	private String getArchitectureLabel(String string) {
		if (string.contains(".x86.")) {
			return "32 bit";
		} else if (string.contains(".x64.") || string.contains(".x86_64.")) {
			return "64 bit";
		}
		return "undefined";
	}

	@Override
	protected boolean fieldCountsAbsolute() {
		return true;
	}

}
