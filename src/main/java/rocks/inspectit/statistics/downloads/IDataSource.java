package rocks.inspectit.statistics.downloads;

import java.util.Collection;
import java.util.List;

public interface IDataSource {
	/**
	 * Store download statistics to data source.
	 * 
	 * @param statistics
	 *            data to store
	 */
	void store(Collection<DownloadStatistics> statistics);

	/**
	 * Load all download statistics from the past.
	 * 
	 * @return list of download statistics
	 */
	List<DownloadStatistics> load();

	/**
	 * Returns the absolute download count since the specified timestamp for all artifacts with the
	 * given identifier.
	 * 
	 * @param since
	 *            timestamp
	 * @param identifier
	 *            identifier of artifact
	 * @return
	 */
	int getAbsoluteCounts(long since, Identifier identifier);
}
