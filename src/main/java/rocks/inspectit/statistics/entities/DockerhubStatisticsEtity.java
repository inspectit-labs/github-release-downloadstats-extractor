package rocks.inspectit.statistics.entities;

import java.util.Map;

import rocks.inspectit.statistics.Constants;

public class DockerhubStatisticsEtity extends AbstractStatisticsEntity {
	
	private static final String[] KEY_NAMES  = new String[] { Constants.DOCKER_HUB_NAME_TAG };
private static final String[] FIELD_NAMES = new String[] { Constants.DOCKER_HUB_PULL_COUNT_FIELD, Constants.DOCKER_HUB_STAR_COUNT_FIELD };

	private static DockerhubStatisticsEtity template;

	public static DockerhubStatisticsEtity getTemplate() {
		if (null == template) {
			template = new DockerhubStatisticsEtity();
		}
		return template;
	}
	
	private int starCount;
	private int pullCount;
	
	private DockerhubStatisticsEtity() {
		super(Constants.DOCKER_HUB_MEASUREMENT, 0L, null);
	}

	public DockerhubStatisticsEtity(long timestamp, String repositoryName,  int pullCount, int starCount) {
		super(Constants.DOCKER_HUB_MEASUREMENT, timestamp, new String[] { repositoryName });
		this.pullCount = pullCount;
		this.starCount = starCount;
	}

	public DockerhubStatisticsEtity(String[] keys, Object[] fields, long timestamp) {
		super(Constants.DOCKER_HUB_MEASUREMENT, timestamp, keys);

		if (fields.length < 2) {
			throw new IllegalArgumentException("Invalid amount of field values!");
		}

		if (fields[0] instanceof Number) {
			pullCount = ((Number) fields[0]).intValue();
		} else if (fields[0] instanceof String) {
			pullCount = Integer.parseInt(((String) fields[0]));
		} else {
			throw new IllegalArgumentException("Invalid field value!");
		}
		
		if (fields[1] instanceof Number) {
			starCount = ((Number) fields[1]).intValue();
		} else if (fields[1] instanceof String) {
			starCount = Integer.parseInt(((String) fields[1]));
		} else {
			throw new IllegalArgumentException("Invalid field value!");
		}

	}



	/**
	 * @return the starCount
	 */
	public int getStarCount() {
		return starCount;
	}

	/**
	 * @param starCount
	 *            the starCount to set
	 */
	public void setStarCount(int starCount) {
		this.starCount = starCount;
	}

	/**
	 * @return the pullCount
	 */
	public int getPullCount() {
		return pullCount;
	}

	/**
	 * @param pullCount
	 *            the pullCount to set
	 */
	public void setPullCount(int pullCount) {
		this.pullCount = pullCount;
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
		return new Object[] { pullCount, starCount };
	}

	@Override
	public void setFields(Map<String, Object> fieldValues) {
		Object pullCountObj = fieldValues.get(Constants.DOCKER_HUB_PULL_COUNT_FIELD);
		if(null == pullCountObj || !(pullCountObj instanceof Number || pullCountObj instanceof String)){
			throw new IllegalArgumentException("Invalid field value!");
		}
		
		if (pullCountObj instanceof Number) {
			pullCount = ((Number) pullCountObj).intValue();
		} else if (pullCountObj instanceof String) {
			pullCount = Integer.parseInt(((String) pullCountObj));
		} else {
			throw new IllegalArgumentException("Invalid field value!");
		}
		
		Object starCountObj = fieldValues.get(Constants.DOCKER_HUB_STAR_COUNT_FIELD);
		if(null == starCountObj || !(starCountObj instanceof Number || starCountObj instanceof String)){
			throw new IllegalArgumentException("Invalid field value!");
		}
		
		if (starCountObj instanceof Number) {
			starCount = ((Number) starCountObj).intValue();
		} else if (starCountObj instanceof String) {
			starCount = Integer.parseInt(((String) starCountObj));
		} else {
			throw new IllegalArgumentException("Invalid field value!");
		}
		
	}

}
