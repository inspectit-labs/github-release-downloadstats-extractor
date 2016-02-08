package rocks.inspectit.statistics.extractors;

import java.util.List;
import java.util.Properties;

import org.influxdb.InfluxDB;

import rocks.inspectit.statistics.StatisticsExtractor;
import rocks.inspectit.statistics.entities.DockerhubStatisticsEtity;
import rocks.inspectit.statistics.entities.EventEntity;
import rocks.inspectit.statistics.source.CSVSource;
import rocks.inspectit.statistics.source.InfluxDBSource;

public class EventsExtractor extends AbstractExtractor<EventEntity> {
	public EventsExtractor(Properties properties, InfluxDB influxDB) {
		super(properties);
		init(null, EventEntity.getTemplate(), influxDB, 0L);
	}

	@Override
	public List<EventEntity> getResultList(String jsonString) {
		return influxDBSource.load(EventEntity.getTemplate());
	}

	@Override
	protected boolean needsRelativationOfValues() {
		return false;
	}

	@Override
	protected void initProperties(Properties properties) {

	}

	@Override
	protected void checkProperties(Properties properties) {

	}

}
