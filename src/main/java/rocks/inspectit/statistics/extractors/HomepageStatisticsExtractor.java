package rocks.inspectit.statistics.extractors;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.influxdb.InfluxDB;

import rocks.inspectit.statistics.entities.WebPageStatisticsEntity;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.GaData;

public class HomepageStatisticsExtractor extends AbstractExtractor<WebPageStatisticsEntity> {
	private static final String SERVICE_ACCOUNT_PRIVATE_KEY_KEY = "google.api.ga.serviceAccountKey";
	private static final String PROFILE_ID_KEY = "google.api.ga.profileId";
	private static final String APPLICATION_NAME = "Statistics Extractor";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

	public HomepageStatisticsExtractor(Properties properties, InfluxDB influxDB) {
		super(properties);
		init(WebPageStatisticsEntity.getTemplate(), influxDB, 0L);
	}

	@Override
	public List<WebPageStatisticsEntity> getResultList() {
		try {
			Analytics analytics = initializeAnalytics();

			String dimensions = getGAString(WebPageStatisticsEntity.KEY_NAMES, new String[] { WebPageStatisticsEntity.WEBPAGE_TRAFFIC_PROFILE_KEY }) + ",ga:date";
			String metrics = getGAString(WebPageStatisticsEntity.FIELD_NAMES, null);
			GaData data = analytics.data().ga().get("ga:" + getProperties().getProperty(PROFILE_ID_KEY), "2daysAgo", "2daysAgo", metrics).setDimensions(dimensions).execute();

			if (data != null && null != data.getRows() && !data.getRows().isEmpty()) {
				List<WebPageStatisticsEntity> statistics = new ArrayList<>();
				Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
				for (List<String> row : data.getRows()) {
					String time = row.get(4);
					cal.set(Integer.parseInt(time.substring(0, 4)), Integer.parseInt(time.substring(4, 6)), Integer.parseInt(time.substring(6, 8)));
					long timestamp = DateUtils.truncate(cal, Calendar.DATE).getTimeInMillis();
					WebPageStatisticsEntity entity = new WebPageStatisticsEntity(timestamp, getProperties().getProperty(PROFILE_ID_KEY), row.get(0), row.get(1), row.get(2), row.get(3),
							Integer.parseInt(row.get(5)), Integer.parseInt(row.get(6)), Integer.parseInt(row.get(7)), Integer.parseInt(row.get(8)), Double.parseDouble(row.get(9)),
							Double.parseDouble(row.get(10)));
					statistics.add(entity);
				}

				return statistics;
			} else {
				return Collections.emptyList();
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String getGAString(String[] stringArray, String[] excludes) {
		String result = "";
		boolean first = true;
		outerLoop: for (String value : stringArray) {
			if (null != excludes) {
				for (String exc : excludes) {
					if (exc.equals(value)) {
						continue outerLoop;
					}
				}
			}
			value = value.replace("num_", "");
			if (first) {
				result += "ga:";
				result += value;
				first = false;
			} else {
				result += ",";
				result += "ga:";
				result += value;
			}
		}
		return result;
	}

	@Override
	protected boolean needsRelativationOfValues() {
		return false;
	}

	@Override
	protected void initProperties(Properties properties) {

		if (!properties.contains(SERVICE_ACCOUNT_PRIVATE_KEY_KEY) && System.getenv(SERVICE_ACCOUNT_PRIVATE_KEY_KEY) != null) {
			properties.setProperty(SERVICE_ACCOUNT_PRIVATE_KEY_KEY, System.getenv(SERVICE_ACCOUNT_PRIVATE_KEY_KEY));
		}

		if (!properties.contains(PROFILE_ID_KEY) && System.getenv(PROFILE_ID_KEY) != null) {
			properties.setProperty(PROFILE_ID_KEY, System.getenv(PROFILE_ID_KEY));
		}
	}

	@Override
	protected void checkProperties(Properties properties) {
		if (!properties.containsKey(SERVICE_ACCOUNT_PRIVATE_KEY_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + SERVICE_ACCOUNT_PRIVATE_KEY_KEY);
		}

		if (!properties.containsKey(PROFILE_ID_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + PROFILE_ID_KEY);
		}
	}

	private Analytics initializeAnalytics() throws Exception {
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

		InputStream inStream = IOUtils.toInputStream(getProperties().getProperty(SERVICE_ACCOUNT_PRIVATE_KEY_KEY));
		GoogleCredential credential = GoogleCredential.fromStream(inStream);

		credential = credential.createScoped(AnalyticsScopes.all());
		return new Analytics.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
	}

}
