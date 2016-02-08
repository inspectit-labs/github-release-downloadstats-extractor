package rocks.inspectit.statistics;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import rocks.inspectit.statistics.extractors.DockerhubStatisticsExtractor;
import rocks.inspectit.statistics.extractors.EventsExtractor;
import rocks.inspectit.statistics.extractors.GithubDownloadsStatisticsExtractor;
import rocks.inspectit.statistics.extractors.GithubRepositoryStatisticsExtractor;
import rocks.inspectit.statistics.extractors.GithubTrafficStatisticsExtractor;
import rocks.inspectit.statistics.extractors.TwitterStatisticsExtractor;

public class BackupImporter {

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

		if (!properties.contains(StatisticsExtractor.INFLUX_DB_URL_KEY) && System.getenv(StatisticsExtractor.INFLUX_DB_URL_KEY) != null) {
			properties.setProperty(StatisticsExtractor.INFLUX_DB_URL_KEY, System.getenv(StatisticsExtractor.INFLUX_DB_URL_KEY));
		}
		if (!properties.contains(StatisticsExtractor.INFLUX_DB_USER_KEY) && System.getenv(StatisticsExtractor.INFLUX_DB_USER_KEY) != null) {
			properties.setProperty(StatisticsExtractor.INFLUX_DB_USER_KEY, System.getenv(StatisticsExtractor.INFLUX_DB_USER_KEY));
		}
		if (!properties.contains(StatisticsExtractor.INFLUX_DB_PASSWORD_KEY) && System.getenv(StatisticsExtractor.INFLUX_DB_PASSWORD_KEY) != null) {
			properties.setProperty(StatisticsExtractor.INFLUX_DB_PASSWORD_KEY, System.getenv(StatisticsExtractor.INFLUX_DB_PASSWORD_KEY));
		}
		if (!properties.contains(StatisticsExtractor.INFLUX_DB_DATABASE_KEY) && System.getenv(StatisticsExtractor.INFLUX_DB_DATABASE_KEY) != null) {
			properties.setProperty(StatisticsExtractor.INFLUX_DB_DATABASE_KEY, System.getenv(StatisticsExtractor.INFLUX_DB_DATABASE_KEY));
		}
		if (!properties.contains(StatisticsExtractor.FTP_USER_KEY) && System.getenv(StatisticsExtractor.FTP_USER_KEY) != null) {
			properties.setProperty(StatisticsExtractor.FTP_USER_KEY, System.getenv(StatisticsExtractor.FTP_USER_KEY));
		}
		if (!properties.contains(StatisticsExtractor.FTP_PASSWORD_KEY) && System.getenv(StatisticsExtractor.FTP_PASSWORD_KEY) != null) {
			properties.setProperty(StatisticsExtractor.FTP_PASSWORD_KEY, System.getenv(StatisticsExtractor.FTP_PASSWORD_KEY));
		}
		if (!properties.contains(StatisticsExtractor.FTP_HOSTNAME_KEY) && System.getenv(StatisticsExtractor.FTP_HOSTNAME_KEY) != null) {
			properties.setProperty(StatisticsExtractor.FTP_HOSTNAME_KEY, System.getenv(StatisticsExtractor.FTP_HOSTNAME_KEY));
		}
		if (!properties.contains(StatisticsExtractor.FTP_DIRECTORY_KEY) && System.getenv(StatisticsExtractor.FTP_DIRECTORY_KEY) != null) {
			properties.setProperty(StatisticsExtractor.FTP_DIRECTORY_KEY, System.getenv(StatisticsExtractor.FTP_DIRECTORY_KEY));
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
		
		importBackup(githubDownloadsExtractor,dockerHubExtractor,githubTrafficExtractor,githubRepositoryExtractor,twitterExtractor,eventsExtractor);
	}

	private static void importBackup(IBackupImporter<?>... importers) {
		for (IBackupImporter<?> importer : importers) {
			importer.importBackup();
		}
	}

	/**
	 * Check validity of properties.
	 * 
	 * @param properties
	 *            {@link Properties} to check.
	 */
	public static void checkProperties(Properties properties) {

		if (!properties.containsKey(StatisticsExtractor.INFLUX_DB_PASSWORD_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + StatisticsExtractor.INFLUX_DB_PASSWORD_KEY);
		}
		if (!properties.containsKey(StatisticsExtractor.INFLUX_DB_URL_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + StatisticsExtractor.INFLUX_DB_URL_KEY);
		}
		if (!properties.containsKey(StatisticsExtractor.INFLUX_DB_USER_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + StatisticsExtractor.INFLUX_DB_USER_KEY);
		}
		if (!properties.containsKey(StatisticsExtractor.INFLUX_DB_DATABASE_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + StatisticsExtractor.INFLUX_DB_DATABASE_KEY);
		}
		if (!properties.containsKey(StatisticsExtractor.FTP_USER_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + StatisticsExtractor.FTP_USER_KEY);
		}
		if (!properties.containsKey(StatisticsExtractor.FTP_PASSWORD_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + StatisticsExtractor.FTP_PASSWORD_KEY);
		}
		if (!properties.containsKey(StatisticsExtractor.FTP_HOSTNAME_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + StatisticsExtractor.FTP_HOSTNAME_KEY);
		}
		if (!properties.containsKey(StatisticsExtractor.FTP_DIRECTORY_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + StatisticsExtractor.FTP_DIRECTORY_KEY);
		}
	}

}
