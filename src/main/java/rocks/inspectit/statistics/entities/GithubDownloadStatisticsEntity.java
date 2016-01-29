package rocks.inspectit.statistics.entities;

import java.util.Map;

import rocks.inspectit.statistics.Constants;

public class GithubDownloadStatisticsEntity extends AbstractStatisticsEntity {
	private static final String GITHUB_DOWNLOADS_MEASUREMENT = "downloads";
	private static final String GITHUB_COUNT_FIELD = "count";
	private static final String GITHUB_MAJOR_VERSION_TAG = "MajorVersion";
	private static final String GITHUB_MINOR_VERSION_TAG = "MinorVersion";
	private static final String GITHUB_BUILD_NR_TAG = "BuildNr";
	private static final String GITHUB_ARTIFACT_TAG = "Artifact";
	private static final String GITHUB_OS_TAG = "OS";
	private static final String GITHUB_ARCHITECTURE_TAG = "Architecture";
	
	private static final String[] KEY_NAMES = new String[] { GITHUB_ARTIFACT_TAG, GITHUB_OS_TAG, GITHUB_ARCHITECTURE_TAG, GITHUB_MAJOR_VERSION_TAG,
			GITHUB_MINOR_VERSION_TAG, GITHUB_BUILD_NR_TAG };
	private static final String[] FIELD_NAMES = new String[] { GITHUB_COUNT_FIELD };

	private static GithubDownloadStatisticsEntity template;

	public static GithubDownloadStatisticsEntity getTemplate() {
		if (null == template) {
			template = new GithubDownloadStatisticsEntity();
		}
		return template;
	}

	private int downloadCount;

	private GithubDownloadStatisticsEntity() {
		super(GITHUB_DOWNLOADS_MEASUREMENT, 0L, null);
	}

	public GithubDownloadStatisticsEntity(long timestamp, String artifact, String os, String architecture, String majorVersion, String minorVersion, String buildNr, int donwloadCount) {
		super(GITHUB_DOWNLOADS_MEASUREMENT, timestamp, new String[] { artifact, os, architecture, majorVersion, minorVersion, buildNr });
		setDownloadCount(donwloadCount);
	}

	public GithubDownloadStatisticsEntity(String[] keys, Object[] fields, long timestamp) {
		super(GITHUB_DOWNLOADS_MEASUREMENT, timestamp, keys);

		if (fields.length < 1) {
			throw new IllegalArgumentException("Invalid amount of field values!");
		}

		downloadCount = getIntValue(fields[0]);

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
		downloadCount = getIntValue(fieldValues.get(GITHUB_COUNT_FIELD));

	}

}
