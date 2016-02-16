package rocks.inspectit.statistics;

import rocks.inspectit.statistics.entities.AbstractStatisticsEntity;

/**
 * Interface for backup importer classes.
 * 
 * @author Alexander Wert
 *
 * @param <T> Type of the entity to import.
 */
public interface IBackupImporter<T extends AbstractStatisticsEntity> {

	/**
	 * Imports backup.
	 */
	void importBackup();

}
