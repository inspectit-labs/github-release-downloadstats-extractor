package rocks.inspectit.statistics.entities;

import java.util.Map;

import rocks.inspectit.statistics.Constants;

public class GithubDownloadStatisticsEntity extends AbstractStatisticsEntity {
	private static final String[] KEY_NAMES = new String[] { Constants.GITHUB_ARTIFACT_TAG, Constants.GITHUB_OS_TAG, Constants.GITHUB_ARCHITECTURE_TAG, Constants.GITHUB_MAJOR_VERSION_TAG,
			Constants.GITHUB_MINOR_VERSION_TAG, Constants.GITHUB_BUILD_NR_TAG };
	private static final String[] FIELD_NAMES = new String[] { Constants.GITHUB_COUNT_FIELD };

	private static GithubDownloadStatisticsEntity template;

	public static GithubDownloadStatisticsEntity getTemplate() {
		if (null == template) {
			template = new GithubDownloadStatisticsEntity();
		}
		return template;
	}

	private int downloadCount;

	private GithubDownloadStatisticsEntity() {
		super(Constants.GITHUB_DOWNLOADS_MEASUREMENT, 0L, null);
	}

	public GithubDownloadStatisticsEntity(long timestamp, String artifact, String os, String architecture, String majorVersion, String minorVersion, String buildNr, int donwloadCount) {
		super(Constants.GITHUB_DOWNLOADS_MEASUREMENT, timestamp, new String[] { artifact, os, architecture, majorVersion, minorVersion, buildNr });
		setDownloadCount(donwloadCount);
	}

	public GithubDownloadStatisticsEntity(String[] keys, Object[] fields, long timestamp) {
		super(Constants.GITHUB_DOWNLOADS_MEASUREMENT, timestamp, keys);

		if (fields.length < 1) {
			throw new IllegalArgumentException("Invalid amount of field values!");
		}

		if (fields[0] instanceof Number) {
			downloadCount = ((Number) fields[0]).intValue();
		} else if (fields[0] instanceof String) {
			downloadCount = Integer.parseInt(((String) fields[0]));
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
		return new Object[] { downloadCount };
	}

	/**
	 * @return the downloadCount
	 */
	public int getDownloadCount() {
		return downloadCount;
	}

	/**
	 * @param downloadCount
	 *            the downloadCount to set
	 */
	public void setDownloadCount(int downloadCount) {
		this.downloadCount = downloadCount;
	}

	@Override
	public void setFields(Map<String, Object> fieldValues) {
		Object countObj = fieldValues.get(Constants.GITHUB_COUNT_FIELD);
		if (null == countObj || !(countObj instanceof Number || countObj instanceof String)) {
			throw new IllegalArgumentException("Invalid field value!");
		}

		if (countObj instanceof Number) {
			downloadCount = ((Number) countObj).intValue();
		} else if (countObj instanceof String) {
			downloadCount = Integer.parseInt(((String) countObj));
		} else {
			throw new IllegalArgumentException("Invalid field value!");
		}

	}

}
