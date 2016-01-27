package rocks.inspectit.statistics;

import rocks.inspectit.statistics.entities.DockerhubStatisticsEtity;
import rocks.inspectit.statistics.entities.GithubDownloadStatisticsEntity;

public interface Constants {

	/**
	 * general constants
	 */
	String TIMESTAMP = "time";

	/**
	 * {@link DockerhubStatisticsEtity} constants.
	 */
	String DOCKER_HUB_MEASUREMENT = "docker_hub";
	String DOCKER_HUB_PULL_COUNT_FIELD = "pull_count";
	String DOCKER_HUB_STAR_COUNT_FIELD = "star_count";
	String DOCKER_HUB_NAME_TAG = "repository_name";

	/**
	 * {@link GithubDownloadStatisticsEntity} constants.
	 */
	String GITHUB_DOWNLOADS_MEASUREMENT = "downloads";
	String GITHUB_COUNT_FIELD = "count";
	String GITHUB_MAJOR_VERSION_TAG = "MajorVersion";
	String GITHUB_MINOR_VERSION_TAG = "MinorVersion";
	String GITHUB_BUILD_NR_TAG = "BuildNr";
	String GITHUB_ARTIFACT_TAG = "Artifact";
	String GITHUB_OS_TAG = "OS";
	String GITHUB_ARCHITECTURE_TAG = "Architecture";
	long GITHUB_DATA_SINCE_TIMESTAMP = 1451606400000L;
	
	String GITHUB_TRAFFIC_MEASUREMENT = "github_traffic";
	String GITHUB_TRAFFIC_VISITORS_FIELD = "visitors_count";
	String GITHUB_TRAFFIC_UNIQUE_VISITORS_FIELD = "unique_visitors_count";
	String GITHUB_TRAFFIC_PERCENTAGE_FIELD = "percentage_visitors";
	String GITHUB_TRAFFIC_UNIQUE_PERCENTAGE_FIELD = "percentage_unique";
	String GITHUB_TRAFFIC_REFERRING_SITE_TAG = "referring_site";
}
