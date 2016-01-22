package rocks.inspectit.statistics.downloads;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.time.DateUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class creates a text file which lists the monthly absolute number of downloads of inspectIT
 * in github {@link https://github.com/inspectIT/inspectIT} and writes this data to a influxDB time
 * series database.
 * 
 * @author Tobias Angerstein, Alexander Wert
 */
public class GithubReleaseDownloadstatsExtractor {
	private static final String URL_KEY = "github.api.url";
	private static final String DOWNLOAD_URL_KEY = "inspectit.download.url";
	private static final String INFLUX_DB_URL_KEY = "influxdb.url";
	private static final String INFLUX_DB_USER_KEY = "influxdb.user";
	private static final String INFLUX_DB_PASSWORD_KEY = "influxdb.password";
	private static final String INFLUX_DB_DATABASE_KEY = "influxdb.database";
	private static final String EXPORT_CSV_FILE_KEY = "target.csv.file.export";
	private static final String IMPORT_CSV_FILE_KEY = "target.csv.file.import";
	private static final String IMPORT_FROM_CSV = "import.from.csv.before";

	/**
	 * main- method
	 * 
	 * @param args
	 *            clientID clientSecret
	 */
	public static void main(String[] args) {

		File configFile;
		Properties properties = new Properties();
		if (args.length != 1) {
			// read config from environment variables
			if (System.getenv(URL_KEY) != null) {
				properties.setProperty(URL_KEY, System.getenv(URL_KEY));
			}
			if (System.getenv(DOWNLOAD_URL_KEY) != null) {
				properties.setProperty(DOWNLOAD_URL_KEY, System.getenv(DOWNLOAD_URL_KEY));
			}
			if (System.getenv(INFLUX_DB_URL_KEY) != null) {
				properties.setProperty(INFLUX_DB_URL_KEY, System.getenv(INFLUX_DB_URL_KEY));
			}
			if (System.getenv(INFLUX_DB_USER_KEY) != null) {
				properties.setProperty(INFLUX_DB_USER_KEY, System.getenv(INFLUX_DB_USER_KEY));
			}
			if (System.getenv(INFLUX_DB_PASSWORD_KEY) != null) {
				properties.setProperty(INFLUX_DB_PASSWORD_KEY, System.getenv(INFLUX_DB_PASSWORD_KEY));
			}
			if (System.getenv(INFLUX_DB_DATABASE_KEY) != null) {
				properties.setProperty(INFLUX_DB_DATABASE_KEY, System.getenv(INFLUX_DB_DATABASE_KEY));
			}

			if (System.getenv(EXPORT_CSV_FILE_KEY) != null) {
				properties.setProperty(EXPORT_CSV_FILE_KEY, System.getenv(EXPORT_CSV_FILE_KEY));
			}

			if (System.getenv(IMPORT_FROM_CSV) != null) {
				properties.setProperty(IMPORT_FROM_CSV, System.getenv(IMPORT_FROM_CSV));
			}
			if (System.getenv(IMPORT_CSV_FILE_KEY) != null) {
				properties.setProperty(IMPORT_CSV_FILE_KEY, System.getenv(IMPORT_CSV_FILE_KEY));
			}

		} else {
			// read config from config file
			configFile = new File(args[0]);
			try (FileReader reader = new FileReader(configFile)) {
				properties.load(reader);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		checkProperties(properties);
		GithubReleaseDownloadstatsExtractor extractor = new GithubReleaseDownloadstatsExtractor(properties);
		try {
			extractor.retrieveDownloadStatistics();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Check validity of properties.
	 * 
	 * @param properties
	 *            {@link Properties} to check.
	 */
	public static void checkProperties(Properties properties) {
		if (!properties.containsKey(URL_KEY)) {
			throw new IllegalArgumentException("GitHub API URL not specified: " + URL_KEY);
		}
		if (!properties.containsKey(DOWNLOAD_URL_KEY)) {
			throw new IllegalArgumentException("InspectIT Download URL not specified: " + DOWNLOAD_URL_KEY);
		}
		if (!properties.containsKey(INFLUX_DB_DATABASE_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + INFLUX_DB_DATABASE_KEY);
		}
		if (!properties.containsKey(INFLUX_DB_PASSWORD_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + INFLUX_DB_PASSWORD_KEY);
		}
		if (!properties.containsKey(INFLUX_DB_URL_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + INFLUX_DB_URL_KEY);
		}
		if (!properties.containsKey(INFLUX_DB_USER_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + INFLUX_DB_USER_KEY);
		}

		if (Boolean.parseBoolean(properties.getProperty(IMPORT_FROM_CSV, "false")) && !properties.containsKey(IMPORT_CSV_FILE_KEY)) {
			throw new IllegalArgumentException("Property not specified although enabled: " + IMPORT_CSV_FILE_KEY);
		}

	}

	private Properties properties;
	private IDataSource influxDBSource;
	private IDataSource csvExportDataSource;
	private IDataSource csvImportDataSource;

	/**
	 * Constructor.
	 * 
	 * @param properties
	 *            {@link Properties} to use as config.
	 */
	public GithubReleaseDownloadstatsExtractor(Properties properties) {
		this.properties = properties;
		influxDBSource = new InfluxDBSource(properties.getProperty(INFLUX_DB_URL_KEY), properties.getProperty(INFLUX_DB_USER_KEY), properties.getProperty(INFLUX_DB_PASSWORD_KEY),
				properties.getProperty(INFLUX_DB_DATABASE_KEY));
		csvExportDataSource = new CSVSource(properties.getProperty(EXPORT_CSV_FILE_KEY));
		boolean importFromCsv = Boolean.parseBoolean(properties.getProperty(IMPORT_FROM_CSV, "false"));
		if (importFromCsv) {
			csvImportDataSource = new CSVSource(properties.getProperty(IMPORT_CSV_FILE_KEY));
		}
	}

	/**
	 * Retrieves and stores download statistics.
	 * 
	 * @throws IOException
	 */
	private void retrieveDownloadStatistics() throws IOException {
		// import old data
		if (null != csvImportDataSource) {
			List<DownloadStatistics> oldData = csvImportDataSource.load();
			influxDBSource.store(oldData);
		}

		// calculate relative counts
		List<DownloadStatistics> resultList = getResultList(getJSONArray());
		for (DownloadStatistics stat : resultList) {
			int prevCount = influxDBSource.getAbsoluteCounts(Constants.GITHUB_DATA_SINCE_TIMESTAMP, stat.getIdentifier());
			int currentCount = stat.getDownloadCount();
			stat.setDownloadCount(currentCount - prevCount);
		}

		// store to database and csv
		influxDBSource.store(resultList);
		csvExportDataSource.store(influxDBSource.load());

	}

	/**
	 * Downloads the JSON.
	 * 
	 * @return JSONArray
	 */
	private JSONArray getJSONArray() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		WebTarget target = client.target(properties.getProperty(URL_KEY));
		Response response = target.request().header("User-Agent", "DownloadCounter").get();
		String jsonString = response.readEntity(String.class);
		response.close();
		try {
			return new JSONArray(jsonString);
		} catch (JSONException e) {
			System.err.println(jsonString);
		}
		return null;
	}

	/**
	 * Creates a list of Download Statistics for individual artifacts from "browser_download_url"
	 * and "download_count".
	 * 
	 * @param jsonArray
	 *            the retrieved jsonArray
	 * @return the resultList
	 */
	private List<DownloadStatistics> getResultList(JSONArray jsonArray) {
		System.out.println("Retrieving download statistics...");
		List<DownloadStatistics> statistics = new ArrayList<DownloadStatistics>();
		try {
			long timestamp = System.currentTimeMillis();
			Date roundedDate = DateUtils.truncate(new Date(timestamp), Calendar.DATE);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONArray assets = jsonArray.getJSONObject(i).getJSONArray("assets");
				for (int z = 0; z < assets.length(); z++) {
					String browserDownloadUrl = assets.getJSONObject(z).get("browser_download_url").toString();
					browserDownloadUrl = browserDownloadUrl.replace(properties.getProperty(DOWNLOAD_URL_KEY), "");
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
					DownloadStatistics statisticsPoint = null;
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

					statisticsPoint = new DownloadStatistics(roundedDate.getTime(), artifactName, getOSLabel(artifactDescription), getArchitectureLabel(artifactDescription), majorVersion,
							minorVersion, buildNr, downloadCount);
					statistics.add(statisticsPoint);
				}
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Successfully retrieved download statistics.");
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

}