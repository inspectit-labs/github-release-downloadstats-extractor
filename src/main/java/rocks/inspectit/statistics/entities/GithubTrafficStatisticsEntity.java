package rocks.inspectit.statistics.entities;

import java.util.Map;

import rocks.inspectit.statistics.Constants;

public class GithubTrafficStatisticsEntity extends AbstractStatisticsEntity {
	private static final String[] KEY_NAMES = new String[] { Constants.GITHUB_TRAFFIC_REFERRING_SITE_TAG };
	private static final String[] FIELD_NAMES = new String[] { Constants.GITHUB_TRAFFIC_VISITORS_FIELD, Constants.GITHUB_TRAFFIC_UNIQUE_VISITORS_FIELD, Constants.GITHUB_TRAFFIC_PERCENTAGE_FIELD,
			Constants.GITHUB_TRAFFIC_UNIQUE_PERCENTAGE_FIELD };

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
		super(Constants.GITHUB_TRAFFIC_MEASUREMENT, 0L, null);
	}

	public GithubTrafficStatisticsEntity(long timestamp, String referringSite, int numVisitors, int numUniqueVisitors, double percentageVisitors, double percentageUnique) {
		super(Constants.GITHUB_TRAFFIC_MEASUREMENT, timestamp, new String[] { referringSite });
		this.visitors = numVisitors;
		this.uniqueVisitors = numUniqueVisitors;
		this.percentageVisitors = percentageVisitors;
		this.percentageUnique = percentageUnique;
	}

	public GithubTrafficStatisticsEntity(String[] keys, Object[] fields, long timestamp) {
		super(Constants.GITHUB_TRAFFIC_MEASUREMENT, timestamp, keys);

		if (fields.length < 4) {
			throw new IllegalArgumentException("Invalid amount of field values!");
		}

		if (fields[0] instanceof Number) {
			visitors = ((Number) fields[0]).intValue();
		} else if (fields[0] instanceof String) {
			visitors = Integer.parseInt(((String) fields[0]));
		} else {
			throw new IllegalArgumentException("Invalid field value!");
		}

		if (fields[1] instanceof Number) {
			uniqueVisitors = ((Number) fields[1]).intValue();
		} else if (fields[1] instanceof String) {
			uniqueVisitors = Integer.parseInt(((String) fields[1]));
		} else {
			throw new IllegalArgumentException("Invalid field value!");
		}
		if (fields[2] instanceof Number) {
			percentageVisitors = ((Number) fields[2]).doubleValue();
		} else if (fields[2] instanceof String) {
			percentageVisitors = Double.parseDouble(((String) fields[2]));
		} else {
			throw new IllegalArgumentException("Invalid field value!");
		}
		if (fields[3] instanceof Number) {
			percentageUnique = ((Number) fields[3]).doubleValue();
		} else if (fields[3] instanceof String) {
			percentageUnique = Double.parseDouble(((String) fields[3]));
		} else {
			throw new IllegalArgumentException("Invalid field value!");
		}
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
		Object visitorsObj = fieldValues.get(Constants.GITHUB_TRAFFIC_VISITORS_FIELD);
		if (null == visitorsObj || !(visitorsObj instanceof Number || visitorsObj instanceof String)) {
			throw new IllegalArgumentException("Invalid field value!");
		}

		if (visitorsObj instanceof Number) {
			visitors = ((Number) visitorsObj).intValue();
		} else if (visitorsObj instanceof String) {
			visitors = Integer.parseInt(((String) visitorsObj));
		} else {
			throw new IllegalArgumentException("Invalid field value!");
		}

		Object uniqueVisitorsObj = fieldValues.get(Constants.GITHUB_TRAFFIC_UNIQUE_VISITORS_FIELD);
		if (null == uniqueVisitorsObj || !(uniqueVisitorsObj instanceof Number || uniqueVisitorsObj instanceof String)) {
			throw new IllegalArgumentException("Invalid field value!");
		}

		if (uniqueVisitorsObj instanceof Number) {
			uniqueVisitors = ((Number) uniqueVisitorsObj).intValue();
		} else if (uniqueVisitorsObj instanceof String) {
			uniqueVisitors = Integer.parseInt(((String) uniqueVisitorsObj));
		} else {
			throw new IllegalArgumentException("Invalid field value!");
		}

		Object percentageObj = fieldValues.get(Constants.GITHUB_TRAFFIC_PERCENTAGE_FIELD);
		if (null == percentageObj || !(percentageObj instanceof Number || percentageObj instanceof String)) {
			throw new IllegalArgumentException("Invalid field value!");
		}

		if (percentageObj instanceof Number) {
			percentageVisitors = ((Number) percentageObj).doubleValue();
		} else if (percentageObj instanceof String) {
			percentageVisitors = Double.parseDouble(((String) percentageObj));
		} else {
			throw new IllegalArgumentException("Invalid field value!");
		}

		Object percentageUniqueObj = fieldValues.get(Constants.GITHUB_TRAFFIC_UNIQUE_PERCENTAGE_FIELD);
		if (null == percentageUniqueObj || !(percentageUniqueObj instanceof Number || percentageUniqueObj instanceof String)) {
			throw new IllegalArgumentException("Invalid field value!");
		}

		if (percentageUniqueObj instanceof Number) {
			percentageUnique = ((Number) percentageUniqueObj).doubleValue();
		} else if (percentageUniqueObj instanceof String) {
			percentageUnique = Double.parseDouble(((String) percentageUniqueObj));
		} else {
			throw new IllegalArgumentException("Invalid field value!");
		}
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
