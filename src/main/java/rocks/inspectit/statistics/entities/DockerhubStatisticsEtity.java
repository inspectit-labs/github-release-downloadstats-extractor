package rocks.inspectit.statistics.entities;

import java.util.Map;

import rocks.inspectit.statistics.Constants;

public class DockerhubStatisticsEtity extends AbstractStatisticsEntity {
	public static final String DOCKER_HUB_MEASUREMENT = "docker_hub";
	public static final String DOCKER_HUB_PULL_COUNT_FIELD = "pull_count";
	public static final String DOCKER_HUB_STAR_COUNT_FIELD = "star_count";
	public static final String DOCKER_HUB_NAME_TAG = "repository_name";
	
	public static final String[] KEY_NAMES = new String[] { DOCKER_HUB_NAME_TAG };
	public static final String[] FIELD_NAMES = new String[] { DOCKER_HUB_PULL_COUNT_FIELD, DOCKER_HUB_STAR_COUNT_FIELD };

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
		super(DOCKER_HUB_MEASUREMENT, 0L, null);
	}

	public DockerhubStatisticsEtity(long timestamp, String repositoryName, int pullCount, int starCount) {
		super(DOCKER_HUB_MEASUREMENT, timestamp, new String[] { repositoryName });
		this.pullCount = pullCount;
		this.starCount = starCount;
	}

	public DockerhubStatisticsEtity(String[] keys, Object[] fields, long timestamp) {
		super(DOCKER_HUB_MEASUREMENT, timestamp, keys);

		if (fields.length < 2) {
			throw new IllegalArgumentException("Invalid amount of field values!");
		}

		pullCount = getIntValue(fields[0]);
		starCount = getIntValue(fields[1]);

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
		pullCount = getIntValue(fieldValues.get(DOCKER_HUB_PULL_COUNT_FIELD));
		starCount = getIntValue(fieldValues.get(DOCKER_HUB_STAR_COUNT_FIELD));
	}

}
