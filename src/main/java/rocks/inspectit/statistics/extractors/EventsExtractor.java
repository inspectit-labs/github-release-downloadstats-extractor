package rocks.inspectit.statistics.extractors;

import java.util.List;
import java.util.Properties;

import org.influxdb.InfluxDB;

import rocks.inspectit.statistics.entities.EventEntity;

public class EventsExtractor extends AbstractExtractor<EventEntity> {
	public EventsExtractor(Properties properties, InfluxDB influxDB) {
		super(properties);
		init(EventEntity.getTemplate(), influxDB);
	}

	@Override
	public List<EventEntity> getResultList() {
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
