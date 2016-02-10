package rocks.inspectit.statistics.extractors;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

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
	private static final String SUBSCRIBERS_URL_KEY = "github.subscribers.api.url";

	public GithubRepositoryStatisticsExtractor(Properties properties, InfluxDB influxDB) {
		super(properties);
		init(GithubRepositoryStatisticsEntity.getTemplate(), influxDB,  0L);
	}

	@Override
	public List<GithubRepositoryStatisticsEntity> getResultList() {
		System.out.println("Retrieving Github repositories statistics...");
		String jsonString = getJSONString(getProperties().getProperty(URL_KEY));
		
		List<GithubRepositoryStatisticsEntity> statistics = new ArrayList<GithubRepositoryStatisticsEntity>();
		try {
			Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			long timestamp = DateUtils.truncate(cal,Calendar.DATE).getTimeInMillis();

			JSONArray jsonArray = new JSONArray(jsonString);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String repoName = jsonObject.get("name").toString();
				String watchersJsonString = getJSONString(getProperties().getProperty(SUBSCRIBERS_URL_KEY).replace("<REPO>",repoName));
				int watchers = new JSONArray(watchersJsonString).length();
				int size = Integer.parseInt(jsonObject.get("size").toString());
				int stars = Integer.parseInt(jsonObject.get("stargazers_count").toString());
				int forks = Integer.parseInt(jsonObject.get("forks_count").toString());
				int openIssues = Integer.parseInt(jsonObject.get("open_issues_count").toString());

				GithubRepositoryStatisticsEntity entity = new GithubRepositoryStatisticsEntity(timestamp, repoName, size, stars, watchers, forks, openIssues);

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
		if (!properties.contains(SUBSCRIBERS_URL_KEY) && System.getenv(SUBSCRIBERS_URL_KEY) != null) {
			properties.setProperty(SUBSCRIBERS_URL_KEY, System.getenv(SUBSCRIBERS_URL_KEY));
		}
	}

	@Override
	protected void checkProperties(Properties properties) {
		if (!properties.containsKey(URL_KEY)) {
			throw new IllegalArgumentException("GitHub API URL not specified: " + URL_KEY);
		}
		if (!properties.containsKey(SUBSCRIBERS_URL_KEY)) {
			throw new IllegalArgumentException("GitHub API URL not specified: " + SUBSCRIBERS_URL_KEY);
		}
	}

}
