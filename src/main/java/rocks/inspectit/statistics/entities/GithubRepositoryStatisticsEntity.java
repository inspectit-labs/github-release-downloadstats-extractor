package rocks.inspectit.statistics.entities;

import java.util.Map;

public class GithubRepositoryStatisticsEntity extends AbstractStatisticsEntity {
	public static final String GITHUB_REPOSITORY_MEASUREMENT = "github_repositories";
	public static final String GITHUB_REPOSITORY_SIZE_FIELD = "size";
	public static final String GITHUB_REPOSITORY_STARS_FIELD = "star_count";
	public static final String GITHUB_REPOSITORY_WATCHERS_FIELD = "watcher_count";
	public static final String GITHUB_REPOSITORY_FORKS_FIELD = "fork_count";
	public static final String GITHUB_REPOSITORY_OPEN_ISSUE_FIELD = "open_issue_count";
	public static final String GITHUB_REPOSITORY_NAME_TAG = "repository_name";

	public static final String[] KEY_NAMES = new String[] { GITHUB_REPOSITORY_NAME_TAG };
	public static final String[] FIELD_NAMES = new String[] { GITHUB_REPOSITORY_SIZE_FIELD, GITHUB_REPOSITORY_STARS_FIELD, GITHUB_REPOSITORY_WATCHERS_FIELD, GITHUB_REPOSITORY_FORKS_FIELD,
			GITHUB_REPOSITORY_OPEN_ISSUE_FIELD };

	private static GithubRepositoryStatisticsEntity template;

	public static GithubRepositoryStatisticsEntity getTemplate() {
		if (null == template) {
			template = new GithubRepositoryStatisticsEntity();
		}
		return template;
	}

	private int size;
	private int stars;
	private int watchers;
	private int forks;
	private int openIssues;

	private GithubRepositoryStatisticsEntity() {
		super(GITHUB_REPOSITORY_MEASUREMENT, 0L, null);
	}

	/**
	 * @param measurementName
	 * @param timestamp
	 * @param keys
	 * @param size
	 * @param stars
	 * @param watchers
	 * @param forks
	 * @param openIssues
	 */
	public GithubRepositoryStatisticsEntity(long timestamp, String repositoryName, int size, int stars, int watchers, int forks, int openIssues) {
		super(GITHUB_REPOSITORY_MEASUREMENT, timestamp, new String[] { repositoryName });
		this.size = size;
		this.stars = stars;
		this.watchers = watchers;
		this.forks = forks;
		this.openIssues = openIssues;
	}

	public GithubRepositoryStatisticsEntity(String[] keys, Object[] fields, long timestamp) {
		super(GITHUB_REPOSITORY_MEASUREMENT, timestamp, keys);

		if (fields.length < 5) {
			throw new IllegalArgumentException("Invalid amount of field values!");
		}

		size = getIntValue(fields[0]);
		stars = getIntValue(fields[1]);
		watchers = getIntValue(fields[2]);
		forks = getIntValue(fields[3]);
		openIssues = getIntValue(fields[4]);
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
		return new Object[] { size, stars, watchers, forks, openIssues };
	}

	@Override
	public void setFields(Map<String, Object> fieldValues) {
		size = getIntValue(fieldValues.get(GITHUB_REPOSITORY_SIZE_FIELD));
		stars = getIntValue(fieldValues.get(GITHUB_REPOSITORY_STARS_FIELD));
		watchers = getIntValue(fieldValues.get(GITHUB_REPOSITORY_WATCHERS_FIELD));
		forks = getIntValue(fieldValues.get(GITHUB_REPOSITORY_FORKS_FIELD));
		openIssues = getIntValue(fieldValues.get(GITHUB_REPOSITORY_OPEN_ISSUE_FIELD));
	}

}
