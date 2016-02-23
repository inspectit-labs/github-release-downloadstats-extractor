package rocks.inspectit.statistics.extractors;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateUtils;
import org.influxdb.InfluxDB;
import org.json.JSONArray;
import org.json.JSONException;

import rocks.inspectit.statistics.entities.GithubDownloadStatisticsEntity;

public class GithubDownloadsStatisticsExtractor extends AbstractExtractor<GithubDownloadStatisticsEntity> {
	private static final String URL_KEY = "github.api.url";
	private static final String DOWNLOAD_URL_KEY = "github.downloads.download.url";
	private static final String DATA_SINCE_TIMESTAMP = "github.downloads.dataSinceTimestamp";
	

	public GithubDownloadsStatisticsExtractor(Properties properties, InfluxDB influxDB) {
		super(properties);
		init(GithubDownloadStatisticsEntity.getTemplate(), influxDB);
	}

	@Override
	protected void initProperties(Properties properties) {
		if (!properties.containsKey(URL_KEY) && System.getenv(URL_KEY) != null) {
			properties.setProperty(URL_KEY, System.getenv(URL_KEY));
		}
		if (!properties.containsKey(DOWNLOAD_URL_KEY) && System.getenv(DOWNLOAD_URL_KEY) != null) {
			properties.setProperty(DOWNLOAD_URL_KEY, System.getenv(DOWNLOAD_URL_KEY));
		}
		if (properties.containsKey(DATA_SINCE_TIMESTAMP)) {
			absoluteCountsSinceTime = Long.parseLong(properties.getProperty(DATA_SINCE_TIMESTAMP));
		} else if (System.getenv(DATA_SINCE_TIMESTAMP) != null) {
			absoluteCountsSinceTime = Long.parseLong(System.getenv(DATA_SINCE_TIMESTAMP));
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
		if (!properties.containsKey(DOWNLOAD_URL_KEY)) {
			throw new IllegalArgumentException("InspectIT Download URL not specified: " + DOWNLOAD_URL_KEY);
		}
	}

	@Override
	public List<GithubDownloadStatisticsEntity> getResultList() {
		System.out.println("Retrieving Github download statistics...");
		String jsonString = getJSONString(getProperties().getProperty(URL_KEY));
		List<GithubDownloadStatisticsEntity> statistics = new ArrayList<GithubDownloadStatisticsEntity>();
		try {
			Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			long timestamp = DateUtils.truncate(cal, Calendar.DATE).getTimeInMillis();

			JSONArray jsonArray = new JSONArray(jsonString);
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

					GithubDownloadStatisticsEntity statisticsPoint = new GithubDownloadStatisticsEntity(timestamp, artifactName, getOSLabel(artifactDescription),
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
	protected boolean needsRelativationOfValues() {
		return true;
	}

}
