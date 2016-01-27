package rocks.inspectit.statistics;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import rocks.inspectit.statistics.extractors.DockerhubStatisticsExtractor;
import rocks.inspectit.statistics.extractors.GithubDownloadsStatisticsExtractor;
import rocks.inspectit.statistics.extractors.GithubTrafficStatisticsExtractor;

public class StatisticsExtractor {
	public static final String INFLUX_DB_URL_KEY = "influxdb.url";
	public static final String INFLUX_DB_USER_KEY = "influxdb.user";
	public static final String INFLUX_DB_PASSWORD_KEY = "influxdb.password";
	public static final String INFLUX_DB_DATABASE_KEY = "influxdb.database";

	public static void main(String[] args) {
		File configFile;
		Properties properties = new Properties();
		if (args.length == 1) {
			// read config from config file
			configFile = new File(args[0]);
			try (FileReader reader = new FileReader(configFile)) {
				properties.load(reader);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}

		if (!properties.contains(INFLUX_DB_URL_KEY) && System.getenv(INFLUX_DB_URL_KEY) != null) {
			properties.setProperty(INFLUX_DB_URL_KEY, System.getenv(INFLUX_DB_URL_KEY));
		}
		if (!properties.contains(INFLUX_DB_USER_KEY) && System.getenv(INFLUX_DB_USER_KEY) != null) {
			properties.setProperty(INFLUX_DB_USER_KEY, System.getenv(INFLUX_DB_USER_KEY));
		}
		if (!properties.contains(INFLUX_DB_PASSWORD_KEY) && System.getenv(INFLUX_DB_PASSWORD_KEY) != null) {
			properties.setProperty(INFLUX_DB_PASSWORD_KEY, System.getenv(INFLUX_DB_PASSWORD_KEY));
		}
		if (!properties.contains(INFLUX_DB_DATABASE_KEY) && System.getenv(INFLUX_DB_DATABASE_KEY) != null) {
			properties.setProperty(INFLUX_DB_DATABASE_KEY, System.getenv(INFLUX_DB_DATABASE_KEY));
		}

		checkProperties(properties);
		InfluxDB influx = InfluxDBFactory.connect(properties.getProperty(StatisticsExtractor.INFLUX_DB_URL_KEY), properties.getProperty(StatisticsExtractor.INFLUX_DB_USER_KEY),
				properties.getProperty(StatisticsExtractor.INFLUX_DB_PASSWORD_KEY));

		GithubDownloadsStatisticsExtractor githubDownloadsExtractor = new GithubDownloadsStatisticsExtractor(properties, influx);
		DockerhubStatisticsExtractor dockerHubExtractor = new DockerhubStatisticsExtractor(properties, influx);
		GithubTrafficStatisticsExtractor githubTrafficExtractor = new GithubTrafficStatisticsExtractor(properties, influx);
		try {
			githubDownloadsExtractor.retrieveStatistics();
			githubTrafficExtractor.retrieveStatistics();
			dockerHubExtractor.retrieveStatistics();
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

		if (!properties.containsKey(INFLUX_DB_PASSWORD_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + INFLUX_DB_PASSWORD_KEY);
		}
		if (!properties.containsKey(INFLUX_DB_URL_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + INFLUX_DB_URL_KEY);
		}
		if (!properties.containsKey(INFLUX_DB_USER_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + INFLUX_DB_USER_KEY);
		}
		if (!properties.containsKey(INFLUX_DB_DATABASE_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + INFLUX_DB_DATABASE_KEY);
		}
	}

}
