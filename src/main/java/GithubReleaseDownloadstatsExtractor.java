import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.time.DateUtils;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class creates a text file which lists the monthly absolute number of downloads of inspectIT
 * in github {@link https://github.com/inspectIT/inspectIT}
 * 
 * @author Tobias Angerstein
 */
public class GithubReleaseDownloadstatsExtractor {
	private static final String URL_KEY = "github.api.url";
	private static final String DOWNLOAD_URL_KEY = "inspectit.download.url";
	private static final String INFLUX_DB_URL_KEY = "influxdb.url";
	private static final String INFLUX_DB_USER_KEY = "influxdb.user";
	private static final String INFLUX_DB_PASSWORD_KEY = "influxdb.password";
	private static final String INFLUX_DB_DATABASE_KEY = "influxdb.database";
	private static final String CSV_FILE_KEY = "target.csv.file";

	private static final String DOWNLOADS_MEASUREMENT = "downloads";
	private static final String COUNT_FIELD = "count";
	private static final String MAJOR_VERSION_TAG = "MajorVersion";
	private static final String MINOR_VERSION_TAG = "MinorVersion";
	private static final String BUILD_NR_TAG = "BuildNr";
	private static final String ARTIFACT_TAG = "Artifact";
	private static final String OS_TAG = "OS";
	private static final String ARCHITECTURE_TAG = "Architecture";
	private static final long HOUR_IN_MILLIS = 1000L * 60L * 60L;

	private static final String DEFAULT_CONFIG_FILE = "downloadStatistics.conf";

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
			properties.setProperty(URL_KEY, System.getenv(URL_KEY));
			properties.setProperty(DOWNLOAD_URL_KEY, System.getenv(DOWNLOAD_URL_KEY));
			properties.setProperty(INFLUX_DB_URL_KEY, System.getenv(INFLUX_DB_URL_KEY));
			properties.setProperty(INFLUX_DB_USER_KEY, System.getenv(INFLUX_DB_USER_KEY));
			properties.setProperty(INFLUX_DB_PASSWORD_KEY, System.getenv(INFLUX_DB_PASSWORD_KEY));
			properties.setProperty(INFLUX_DB_DATABASE_KEY, System.getenv(INFLUX_DB_DATABASE_KEY));
			properties.setProperty(CSV_FILE_KEY, System.getenv(CSV_FILE_KEY));
		} else {
			configFile = new File(args[0]);
			try (FileReader reader = new FileReader(configFile)) {
				properties.load(reader);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}

	
		

		GithubReleaseDownloadstatsExtractor extractor = new GithubReleaseDownloadstatsExtractor(properties);
		try {
			extractor.extract();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

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
		if (!properties.containsKey(CSV_FILE_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + CSV_FILE_KEY);
		}
	}

	private DateFormat format = new SimpleDateFormat("YYYY-MM-DD HH:00:00.000000000");
	private Properties properties;

	public GithubReleaseDownloadstatsExtractor(Properties properties) {
		this.properties = properties;
	}

	private void extract() throws IOException {
		List<DownloadStatistics> resultList = getResultList(getJSONArray());

		createLogfile(resultList);
		boolean ok = writeToInfluxDB(resultList);
		if (ok) {
			extendCSV(resultList, properties.getProperty(CSV_FILE_KEY));
		}

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
	 * Creates the resultList of "browser_download_url" and "download_count".
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
					String buildNr = "";
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

					statisticsPoint = new DownloadStatistics(roundedDate.getTime() + HOUR_IN_MILLIS, artifactName, getOSLabel(artifactDescription), getArchitectureLabel(artifactDescription),
							majorVersion, minorVersion, buildNr, downloadCount);
					statistics.add(statisticsPoint);
				}
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Successfully retrieved download statistics.");
		return statistics;
	}

	private String getOSLabel(String string) {
		if (string.contains("linux")) {
			return "Linux";
		} else if (string.contains("windows") || string.contains("win32")) {
			return "Windows";
		} else if (string.contains("macosx")) {
			return "MacOS";
		}
		return "undefined";
	}

	private String getArchitectureLabel(String string) {
		if (string.contains(".x86.")) {
			return "32 bit";
		} else if (string.contains(".x64.") || string.contains(".x86_64.")) {
			return "64 bit";
		}
		return "undefined";
	}

	/**
	 * Writes results in textfile.
	 * 
	 * @param resultList
	 *            the results
	 */
	private void createLogfile(List<DownloadStatistics> resultList) {
		String currentMonth = new SimpleDateFormat("yyyy-MM").format(Calendar.getInstance().getTime());
		File logFile = new File(currentMonth + ".txt");
		// html- file for the email body
		File emailContent = new File("emailContent.html");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
			for (DownloadStatistics result : resultList) {
				writer.write(result.toString());
				writer.newLine();
			}
			writer.close();
			BufferedWriter emailContentWriter = new BufferedWriter(new FileWriter(emailContent));
			for (DownloadStatistics result : resultList) {
				emailContentWriter.write("<p>" + result.toString() + "</p>");
				emailContentWriter.newLine();
			}
			emailContentWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Writes results to InfluxDB
	 * 
	 * @param resultList
	 *            results to write
	 */
	private boolean writeToInfluxDB(List<DownloadStatistics> resultList) {
		if (resultList.isEmpty()) {
			return false;
		}
		System.out.println("Writing data to InfluxDB...");
		InfluxDB influxDB = InfluxDBFactory.connect(properties.getProperty(INFLUX_DB_URL_KEY), properties.getProperty(INFLUX_DB_USER_KEY), properties.getProperty(INFLUX_DB_PASSWORD_KEY));

		String dbName = properties.getProperty(INFLUX_DB_DATABASE_KEY);

		String date = format.format(resultList.get(0).getTimestamp() - HOUR_IN_MILLIS);

		Query query = new Query("SELECT count(" + COUNT_FIELD + ") FROM " + DOWNLOADS_MEASUREMENT + " WHERE time = '" + date + "'", dbName);
		QueryResult queryResult = influxDB.query(query);
		int count = 0;
		if (!queryResult.getResults().isEmpty() && queryResult.getResults().get(0).getSeries() != null && !queryResult.getResults().get(0).getSeries().isEmpty()) {
			count = ((Double) queryResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(1)).intValue();
		}
		if (count > 0) {
			System.out.println("Skipping writing data to influx, a measurement point for that day already exists.");
			return false;
		} else {
			BatchPoints batchPoints = BatchPoints.database(dbName).tag("async", "true").retentionPolicy("default").consistency(ConsistencyLevel.ALL).build();
			Random rand = new Random(System.nanoTime());

			for (DownloadStatistics statisticsPoint : resultList) {
				Point point = Point.measurement(DOWNLOADS_MEASUREMENT).time(statisticsPoint.getTimestamp(), TimeUnit.MILLISECONDS).field(COUNT_FIELD, statisticsPoint.getDownloadCount())
						.tag(MAJOR_VERSION_TAG, statisticsPoint.getMajorVersion()).tag(MINOR_VERSION_TAG, statisticsPoint.getMinorVersion()).tag(BUILD_NR_TAG, statisticsPoint.getBuildNr())
						.tag(ARTIFACT_TAG, statisticsPoint.getArtifactType()).tag(OS_TAG, statisticsPoint.getOs()).tag(ARCHITECTURE_TAG, statisticsPoint.getArchitecture()).build();

				batchPoints.point(point);
				statisticsPoint.setDownloadCount(statisticsPoint.getDownloadCount() + rand.nextInt(10));
			}

			influxDB.write(batchPoints);
			System.out.println("Successfully wrote data to InfluxDB");
			return true;
		}

	}

	/**
	 * Extends CSV file.
	 * 
	 * @param resultList
	 *            result list
	 * @param fileName
	 *            name of the CSV file
	 * @throws IOException
	 */
	private void extendCSV(List<DownloadStatistics> resultList, String fileName) throws IOException {
		System.out.println("Extending CSV file " + fileName + " ...");
		File csvFile = new File(fileName);
		boolean headerNeeded = !csvFile.exists();
		BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
		if (headerNeeded) {
			writer.write(DownloadStatistics.getCSVHeader());
			writer.newLine();
		}

		for (DownloadStatistics result : resultList) {
			writer.write(result.toCSVString());
			writer.newLine();
		}
		writer.close();
		System.out.println("Successfully Extended CSV file " + fileName);
	}

}