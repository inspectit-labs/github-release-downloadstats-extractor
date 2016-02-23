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
import org.json.JSONObject;

import rocks.inspectit.statistics.entities.DockerhubStatisticsEtity;

public class DockerhubStatisticsExtractor extends AbstractExtractor<DockerhubStatisticsEtity> {
	private static final String URL_KEY = "dockerhub.api.url";

	public DockerhubStatisticsExtractor(Properties properties, InfluxDB influxDB) {
		super(properties);
		init(DockerhubStatisticsEtity.getTemplate(), influxDB);
	}

	@Override
	public List<DockerhubStatisticsEtity> getResultList() {
		System.out.println("Retrieving Docker Hub statistics...");
		String jsonString = getJSONString(getProperties().getProperty(URL_KEY));
		List<DockerhubStatisticsEtity> statistics = new ArrayList<DockerhubStatisticsEtity>();
		try {
			Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			long timestamp = DateUtils.truncate(cal,Calendar.DATE).getTimeInMillis();
			
			JSONObject jsonObject = new JSONObject(jsonString);
			JSONArray repositories = jsonObject.getJSONArray("results");
			for (int z = 0; z < repositories.length(); z++) {
				String name = repositories.getJSONObject(z).get("name").toString();
				int starCount = Integer.parseInt(repositories.getJSONObject(z).get("star_count").toString());
				int pullCount = Integer.parseInt(repositories.getJSONObject(z).get("pull_count").toString());

				DockerhubStatisticsEtity entity = new DockerhubStatisticsEtity(timestamp, name, pullCount, starCount);
				statistics.add(entity);
			}

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Successfully retrieved Docker Hub statistics.");
		return statistics;
	}

	@Override
	protected boolean needsRelativationOfValues() {
		return true;
	}

	@Override
	protected void initProperties(Properties properties) {
		if (!properties.containsKey(URL_KEY) && System.getenv(URL_KEY) != null) {
			properties.setProperty(URL_KEY, System.getenv(URL_KEY));
		}
	}

	@Override
	protected void checkProperties(Properties properties) {
		if (!properties.containsKey(URL_KEY)) {
			throw new IllegalArgumentException("GitHub API URL not specified: " + URL_KEY);
		}
	}

}
