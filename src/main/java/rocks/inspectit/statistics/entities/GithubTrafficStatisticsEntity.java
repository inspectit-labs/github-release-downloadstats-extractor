package rocks.inspectit.statistics.entities;

import java.util.Map;

public class GithubTrafficStatisticsEntity extends AbstractStatisticsEntity {
	public static final String GITHUB_TRAFFIC_MEASUREMENT = "github_traffic";
	public static final String GITHUB_TRAFFIC_VISITORS_FIELD = "visitors_count";
	public static final String GITHUB_TRAFFIC_UNIQUE_VISITORS_FIELD = "unique_visitors_count";
	public static final String GITHUB_TRAFFIC_PERCENTAGE_FIELD = "percentage_visitors";
	public static final String GITHUB_TRAFFIC_UNIQUE_PERCENTAGE_FIELD = "percentage_unique";
	public static final String GITHUB_TRAFFIC_REFERRING_SITE_TAG = "referring_site";

	public static final String[] KEY_NAMES = new String[] { GITHUB_TRAFFIC_REFERRING_SITE_TAG };
	public static final String[] FIELD_NAMES = new String[] { GITHUB_TRAFFIC_VISITORS_FIELD, GITHUB_TRAFFIC_UNIQUE_VISITORS_FIELD, GITHUB_TRAFFIC_PERCENTAGE_FIELD,
			GITHUB_TRAFFIC_UNIQUE_PERCENTAGE_FIELD };

	private static GithubTrafficStatisticsEntity template;

	public static GithubTrafficStatisticsEntity getTemplate() {
		if (null == template) {
			template = new GithubTrafficStatisticsEntity();
		}
		return template;
	}

	private int visitors;
	private int uniqueVisitors;
	private double percentageVisitors;
	private double percentageUnique;

	private GithubTrafficStatisticsEntity() {
		super(GITHUB_TRAFFIC_MEASUREMENT, 0L, null);
	}

	public GithubTrafficStatisticsEntity(long timestamp, String referringSite, int numVisitors, int numUniqueVisitors, double percentageVisitors, double percentageUnique) {
		super(GITHUB_TRAFFIC_MEASUREMENT, timestamp, new String[] { referringSite });
		this.visitors = numVisitors;
		this.uniqueVisitors = numUniqueVisitors;
		this.percentageVisitors = percentageVisitors;
		this.percentageUnique = percentageUnique;
	}

	public GithubTrafficStatisticsEntity(String[] keys, Object[] fields, long timestamp) {
		super(GITHUB_TRAFFIC_MEASUREMENT, timestamp, keys);

		if (fields.length < 4) {
			throw new IllegalArgumentException("Invalid amount of field values!");
		}

		visitors = getIntValue(fields[0]);
		uniqueVisitors = getIntValue(fields[1]);
		percentageVisitors = getDoubleValue(fields[2]);
		percentageUnique = getDoubleValue(fields[3]);

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
		return new Object[] { visitors, uniqueVisitors, percentageVisitors, percentageUnique };
	}

	@Override
	public void setFields(Map<String, Object> fieldValues) {
		visitors = getIntValue(fieldValues.get(GITHUB_TRAFFIC_VISITORS_FIELD));
		uniqueVisitors = getIntValue(fieldValues.get(GITHUB_TRAFFIC_UNIQUE_VISITORS_FIELD));
		percentageVisitors = getDoubleValue(fieldValues.get(GITHUB_TRAFFIC_PERCENTAGE_FIELD));
		percentageUnique = getDoubleValue(fieldValues.get(GITHUB_TRAFFIC_UNIQUE_PERCENTAGE_FIELD));
	}

	/**
	 * @return the percentage
	 */
	public double getPercentage() {
		return percentageVisitors;
	}

	/**
	 * @param percentage
	 *            the percentage to set
	 */
	public void setPercentage(double percentage) {
		this.percentageVisitors = percentage;
	}

	/**
	 * @return the percentageUnique
	 */
	public double getPercentageUnique() {
		return percentageUnique;
	}

	/**
	 * @param percentageUnique
	 *            the percentageUnique to set
	 */
	public void setPercentageUnique(double percentageUnique) {
		this.percentageUnique = percentageUnique;
	}

}
