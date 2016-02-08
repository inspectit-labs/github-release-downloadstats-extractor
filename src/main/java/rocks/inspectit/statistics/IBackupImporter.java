package rocks.inspectit.statistics;

import rocks.inspectit.statistics.entities.AbstractStatisticsEntity;

public interface IBackupImporter<T extends AbstractStatisticsEntity> {

	void importBackup();

}
