package rocks.inspectit.statistics.entities;

import java.util.Map;

public class WebPageStatisticsEntity extends AbstractStatisticsEntity {
	public static final String WEBPAGE_TRAFFIC_MEASUREMENT = "website_traffic";
	public static final String WEBPAGE_TRAFFIC_USERS_FIELD = "num_users";
	public static final String WEBPAGE_TRAFFIC_NEW_USERS_FIELD = "newUsers";
	public static final String WEBPAGE_TRAFFIC_SESSIONS_FIELD = "sessions";
	public static final String WEBPAGE_TRAFFIC_BOUNCES_FIELD = "bounces";
	public static final String WEBPAGE_TRAFFIC_BOUNCE_RATE_FIELD = "bounceRate";
	public static final String WEBPAGE_TRAFFIC_AVG_SESSION_DURATION_FIELD = "avgSessionDuration";
	public static final String WEBPAGE_TRAFFIC_PROFILE_KEY = "profileId";
	public static final String WEBPAGE_TRAFFIC_SOURCE_KEY = "source";
	public static final String WEBPAGE_TRAFFIC_COUNTRY_KEY = "country";
	public static final String WEBPAGE_TRAFFIC_CITY_KEY = "city";
	public static final String WEBPAGE_TRAFFIC_DEVICE_KEY = "deviceCategory";
	public static final String[] KEY_NAMES = new String[] { WEBPAGE_TRAFFIC_PROFILE_KEY, WEBPAGE_TRAFFIC_SOURCE_KEY, WEBPAGE_TRAFFIC_COUNTRY_KEY, WEBPAGE_TRAFFIC_CITY_KEY, WEBPAGE_TRAFFIC_DEVICE_KEY };
	public static final String[] FIELD_NAMES = new String[] { WEBPAGE_TRAFFIC_USERS_FIELD, WEBPAGE_TRAFFIC_NEW_USERS_FIELD, WEBPAGE_TRAFFIC_SESSIONS_FIELD, WEBPAGE_TRAFFIC_BOUNCES_FIELD,
			WEBPAGE_TRAFFIC_BOUNCE_RATE_FIELD, WEBPAGE_TRAFFIC_AVG_SESSION_DURATION_FIELD };

	private static WebPageStatisticsEntity template;

	public static WebPageStatisticsEntity getTemplate() {
		if (null == template) {
			template = new WebPageStatisticsEntity();
		}
		return template;
	}

	private int users;
	private int newUsers;
	private int sessions;
	private int bounces;
	private double bounceRate;
	private double avgSessionDuration;

	private WebPageStatisticsEntity() {
		super(WEBPAGE_TRAFFIC_MEASUREMENT, 0L, null);
	}

	public WebPageStatisticsEntity(long timestamp, String profileId, String source, String country, String city, String deviceCategory, int users, int newUsers, int sessions, int bounces,
			double bounceRate, double avgSessionDuration) {
		super(WEBPAGE_TRAFFIC_MEASUREMENT, timestamp, new String[] { profileId, source, country, city, deviceCategory });
		this.users = users;
		this.newUsers = newUsers;
		this.sessions = sessions;
		this.bounces = bounces;
		this.bounceRate = bounceRate;
		this.avgSessionDuration = avgSessionDuration;
	}

	public WebPageStatisticsEntity(String[] keys, Object[] fields, long timestamp) {
		super(WEBPAGE_TRAFFIC_MEASUREMENT, timestamp, keys);

		if (fields.length < 6) {
			throw new IllegalArgumentException("Invalid amount of field values!");
		}

		this.users = getIntValue(fields[0]);
		this.newUsers = getIntValue(fields[1]);
		this.sessions = getIntValue(fields[2]);
		this.bounces = getIntValue(fields[3]);
		this.bounceRate = getDoubleValue(fields[4]);
		this.avgSessionDuration = getDoubleValue(fields[5]);
	}

	@Override
	public String[] getKeyNames() {
		return KEY_NAMES;
	}

	@Override
	public String[] getFieldNames() {
		return FIELD_NAMES;
	}

	@Override
	public Object[] getFieldValuesList() {
		return new Object[] { users, newUsers, sessions, bounces, bounceRate, avgSessionDuration };
	}

	@Override
	public void setFields(Map<String, Object> fieldValues) {
		users = getIntValue(fieldValues.get(WEBPAGE_TRAFFIC_USERS_FIELD));
		newUsers = getIntValue(fieldValues.get(WEBPAGE_TRAFFIC_NEW_USERS_FIELD));
		sessions = getIntValue(fieldValues.get(WEBPAGE_TRAFFIC_SESSIONS_FIELD));
		bounces = getIntValue(fieldValues.get(WEBPAGE_TRAFFIC_BOUNCES_FIELD));
		bounceRate = getDoubleValue(fieldValues.get(WEBPAGE_TRAFFIC_BOUNCE_RATE_FIELD));
		avgSessionDuration = getDoubleValue(fieldValues.get(WEBPAGE_TRAFFIC_AVG_SESSION_DURATION_FIELD));
	}

	/**
	 * @return the users
	 */
	public int getUsers() {
		return users;
	}

	/**
	 * @param users
	 *            the users to set
	 */
	public void setUsers(int users) {
		this.users = users;
	}

	/**
	 * @return the newUsers
	 */
	public int getNewUsers() {
		return newUsers;
	}

	/**
	 * @param newUsers
	 *            the newUsers to set
	 */
	public void setNewUsers(int newUsers) {
		this.newUsers = newUsers;
	}

	/**
	 * @return the sessions
	 */
	public int getSessions() {
		return sessions;
	}

	/**
	 * @param sessions
	 *            the sessions to set
	 */
	public void setSessions(int sessions) {
		this.sessions = sessions;
	}

	/**
	 * @return the bounces
	 */
	public int getBounces() {
		return bounces;
	}

	/**
	 * @param bounces
	 *            the bounces to set
	 */
	public void setBounces(int bounces) {
		this.bounces = bounces;
	}

	/**
	 * @return the bounceRate
	 */
	public double getBounceRate() {
		return bounceRate;
	}

	/**
	 * @param bounceRate
	 *            the bounceRate to set
	 */
	public void setBounceRate(double bounceRate) {
		this.bounceRate = bounceRate;
	}

	/**
	 * @return the avgSessionDuration
	 */
	public double getAvgSessionDuration() {
		return avgSessionDuration;
	}

	/**
	 * @param avgSessionDuration
	 *            the avgSessionDuration to set
	 */
	public void setAvgSessionDuration(double avgSessionDuration) {
		this.avgSessionDuration = avgSessionDuration;
	}

}
