package rocks.inspectit.statistics.source;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import rocks.inspectit.statistics.entities.AbstractStatisticsEntity;
import rocks.inspectit.statistics.entities.AbstractStatisticsEntity.Identifier;


public interface IDataSource<T extends AbstractStatisticsEntity> {
	/**
	 * Store Docker Hub statistics to data source.
	 * 
	 * @param statistics
	 *            data to store
	 */
	void store(Collection<T> statistics);

	/**
	 * Load all Docker Hub statistics from the past.
	 * 
	 * @return list of Docker Hub statistics
	 */
	List<T> load(T template);

	/**
	 * Returns the absolute Docker Hub values since the specified timestamp for all artifacts with the
	 * given identifier.
	 * 
	 * @param since
	 *            timestamp
	 * @param identifier
	 *            identifier of artifact
	 * @return
	 */
	Map<String, Number> getAbsoluteCounts(long since, Identifier identifier, T template);
	
	long getLatestTimestamp(T template);
}
