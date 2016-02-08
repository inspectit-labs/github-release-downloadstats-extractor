package rocks.inspectit.statistics;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import rocks.inspectit.statistics.entities.AbstractStatisticsEntity;
import rocks.inspectit.statistics.extractors.AbstractExtractor;
import rocks.inspectit.statistics.extractors.DockerhubStatisticsExtractor;
import rocks.inspectit.statistics.extractors.EventsExtractor;
import rocks.inspectit.statistics.extractors.GithubDownloadsStatisticsExtractor;
import rocks.inspectit.statistics.extractors.GithubRepositoryStatisticsExtractor;
import rocks.inspectit.statistics.extractors.GithubTrafficStatisticsExtractor;
import rocks.inspectit.statistics.extractors.TwitterStatisticsExtractor;

public class StatisticsExtractor {
	public static final String INFLUX_DB_URL_KEY = "influxdb.url";
	public static final String INFLUX_DB_USER_KEY = "influxdb.user";
	public static final String INFLUX_DB_PASSWORD_KEY = "influxdb.password";
	public static final String INFLUX_DB_DATABASE_KEY = "influxdb.database";

	public static final String FTP_USER_KEY = "ftp.username";
	public static final String FTP_PASSWORD_KEY = "ftp.password";
	public static final String FTP_HOSTNAME_KEY = "ftp.hostname";
	public static final String FTP_DIRECTORY_KEY = "ftp.directory";

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
		if (!properties.contains(FTP_USER_KEY) && System.getenv(FTP_USER_KEY) != null) {
			properties.setProperty(FTP_USER_KEY, System.getenv(FTP_USER_KEY));
		}
		if (!properties.contains(FTP_PASSWORD_KEY) && System.getenv(FTP_PASSWORD_KEY) != null) {
			properties.setProperty(FTP_PASSWORD_KEY, System.getenv(FTP_PASSWORD_KEY));
		}
		if (!properties.contains(FTP_HOSTNAME_KEY) && System.getenv(FTP_HOSTNAME_KEY) != null) {
			properties.setProperty(FTP_HOSTNAME_KEY, System.getenv(FTP_HOSTNAME_KEY));
		}
		if (!properties.contains(FTP_DIRECTORY_KEY) && System.getenv(FTP_DIRECTORY_KEY) != null) {
			properties.setProperty(FTP_DIRECTORY_KEY, System.getenv(FTP_DIRECTORY_KEY));
		}

		checkProperties(properties);
		InfluxDB influx = InfluxDBFactory.connect(properties.getProperty(StatisticsExtractor.INFLUX_DB_URL_KEY), properties.getProperty(StatisticsExtractor.INFLUX_DB_USER_KEY),
				properties.getProperty(StatisticsExtractor.INFLUX_DB_PASSWORD_KEY));

		GithubDownloadsStatisticsExtractor githubDownloadsExtractor = new GithubDownloadsStatisticsExtractor(properties, influx);
		DockerhubStatisticsExtractor dockerHubExtractor = new DockerhubStatisticsExtractor(properties, influx);
		GithubTrafficStatisticsExtractor githubTrafficExtractor = new GithubTrafficStatisticsExtractor(properties, influx);
		GithubRepositoryStatisticsExtractor githubRepositoryExtractor = new GithubRepositoryStatisticsExtractor(properties, influx);
		TwitterStatisticsExtractor twitterExtractor = new TwitterStatisticsExtractor(properties, influx);
		EventsExtractor eventsExtractor = new EventsExtractor(properties, influx);

		retrieveStatistics(eventsExtractor, githubDownloadsExtractor, dockerHubExtractor, githubTrafficExtractor, githubRepositoryExtractor, twitterExtractor);

		// try {
		// githubDownloadsExtractor.retrieveStatistics();
		// githubTrafficExtractor.retrieveStatistics();
		// githubRepositoryExtractor.retrieveStatistics();
		// dockerHubExtractor.retrieveStatistics();
		// twitterExtractor.retrieveStatistics();
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// System.exit(-1);
		// }
	}

	private static void retrieveStatistics(AbstractExtractor<?>... extractors) {
		for (AbstractExtractor<?> extractor : extractors) {
			try {
				retrieveStatistics(extractor);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static <T extends AbstractStatisticsEntity> void retrieveStatistics(AbstractExtractor<T> extractor) throws IOException {
		List<T> results = extractor.retrieveStatistics();
		extractor.createBackup(results);
		extractor.storeResultsToDatabase(results);
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
		if (!properties.containsKey(FTP_USER_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + FTP_USER_KEY);
		}
		if (!properties.containsKey(FTP_PASSWORD_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + FTP_PASSWORD_KEY);
		}
		if (!properties.containsKey(FTP_HOSTNAME_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + FTP_HOSTNAME_KEY);
		}
		if (!properties.containsKey(FTP_DIRECTORY_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + FTP_DIRECTORY_KEY);
		}
	}

}
