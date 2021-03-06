package rocks.inspectit.statistics.source;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javafx.util.Pair;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import rocks.inspectit.statistics.entities.AbstractStatisticsEntity;
import rocks.inspectit.statistics.entities.AbstractStatisticsEntity.Identifier;
import rocks.inspectit.statistics.entities.EntityField.MetricType;

public class InfluxDBSource<T extends AbstractStatisticsEntity> implements IDataSource<T> {

	private static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private final String databaseName;
	private final InfluxDB influxDB;

	/**
	 * @param url
	 * @param user
	 * @param password
	 * @param databaseName
	 */
	public InfluxDBSource(InfluxDB influxDB, String databaseName) {
		this.influxDB = influxDB;

		this.databaseName = databaseName;
	}

	@Override
	public void store(Collection<T> statistics) {
		if (statistics.isEmpty()) {
			return;
		}
		String measurementName = statistics.iterator().next().getMeasurementName();
		System.out.println("Writing data to InfluxDB for " + measurementName + " ... ");
		BatchPoints batchPoints = BatchPoints.database(databaseName).retentionPolicy("default").consistency(ConsistencyLevel.ALL).build();

		for (AbstractStatisticsEntity entity : statistics) {
			Point point = Point.measurement(entity.getMeasurementName()).time(entity.getTimestamp(), TimeUnit.MILLISECONDS).tag(entity.getKeyValues()).fields(entity.getFieldValues()).build();
			batchPoints.point(point);
		}

		influxDB.write(batchPoints);
		System.out.println("Successfully wrote data to InfluxDB for " + measurementName + ".");

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> load(T template) {
		String keys = "";
		for (String key : template.getKeyNames()) {
			if (keys.isEmpty()) {
				keys += key;
			} else {
				keys += ", " + key;
			}
		}

		String fields = "";
		for (String field : template.getFieldNames()) {
			if (fields.isEmpty()) {
				fields += field;
			} else {
				fields += ", " + field;
			}
		}

		String queryStatement;
		if (!keys.isEmpty()) {
			queryStatement = "SELECT " + keys + ", " + fields + " FROM " + template.getMeasurementName();
		} else {
			queryStatement = "SELECT " + fields + " FROM " + template.getMeasurementName();
		}

		Query query = new Query(queryStatement, databaseName);
		QueryResult results = influxDB.query(query);

		if (null == results || null == results.getResults() || results.getResults().isEmpty()) {
			return Collections.emptyList();
		}
		List<T> statistics = new ArrayList<T>();
		Result result = results.getResults().get(0);
		for (List<Object> entry : result.getSeries().get(0).getValues()) {
			Date date = null;
			try {
				String str = (String) entry.get(0);
				str = str.replace("Z", "");
				if (str.contains(".")) {
					str = str.substring(0, str.lastIndexOf('.'));
				}
				date = format.parse(str);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			long time = date.getTime();

			int numberOfKeys = template.getKeyNames().length;
			String[] keyValues = new String[numberOfKeys];
			int i = 1;
			while (i < 1 + numberOfKeys) {
				keyValues[i - 1] = (String) entry.get(i);
				i++;
			}

			int numberOfFields = template.getFieldNames().length;
			Object[] fieldValues = new Object[numberOfFields];

			while (i < 1 + numberOfKeys + numberOfFields) {
				fieldValues[i - (numberOfKeys + 1)] = entry.get(i);
				i++;
			}

			T entity;
			try {
				entity = (T) template.getClass().getConstructor(String[].class, Object[].class, long.class).newInstance(keyValues, fieldValues, time);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}

			statistics.add(entity);
		}
		return statistics;
	}
	
	
	public Map<Identifier, Map<String, Number>> getAbsoluteCounts(long since, T template) {
		String fields = "";
		for (String field : template.getFieldNames(MetricType.RELATIVE)) {
			if (fields.isEmpty()) {
				fields += "sum(" + field + ") as \""+field+"\"";
			} else {
				fields += ", sum(" + field + ") as \""+field+"\"";
			}
		}

		String whereClause = "WHERE time > " + String.valueOf(since * 1000000L);

		String queryStr = "SELECT " + fields + " FROM " + template.getMeasurementName() + " " + whereClause + " GROUP BY *";

		Query query = new Query(queryStr, databaseName);
		QueryResult results = influxDB.query(query);

		if (null == results || null == results.getResults() || results.getResults().isEmpty() || null == results.getResults().get(0).getSeries() || results.getResults().get(0).getSeries().isEmpty()
				|| null == results.getResults().get(0).getSeries().get(0) || null == results.getResults().get(0).getSeries().get(0).getValues()
				|| results.getResults().get(0).getSeries().get(0).getValues().isEmpty() || null == results.getResults().get(0).getSeries().get(0).getValues().get(0)
				|| results.getResults().get(0).getSeries().get(0).getValues().get(0).isEmpty() || null == results.getResults().get(0).getSeries().get(0).getValues().get(0).get(0)) {
			return null;
		}
		Result result = results.getResults().get(0);

		Map<Identifier, Map<String, Number>> resultMap = new HashMap<Identifier, Map<String, Number>>();
		for(Series series : result.getSeries()){
			Map<String, String> tags = series.getTags();
			String [] tagValueArray = new String[template.getKeyNames().length];
			int i = 0;
			for(String key : template.getKeyNames()){
				tagValueArray[i] = tags.get(key);
				i++;
			}
			Identifier identifier = new Identifier(tagValueArray);
			
			List<String> fieldNames = series.getColumns();
			Map<String, Number> fieldValues = new HashMap<String, Number>();
			resultMap.put(identifier, fieldValues);
			for (int j = 1; j < series.getValues().get(0).size(); j++) {
				Object value = series.getValues().get(0).get(j);
				if (value instanceof Double) {
					double sum = 0.0;
					if (value != null) {
						sum += (Double) value;
						fieldValues.put(fieldNames.get(j), sum);
					}
				}

			}
		}

		return resultMap;
	}



	@Override
	public long getLatestTimestamp(T template) {
		String queryStr = "SELECT LAST(" + template.getFieldNames()[0] + ") FROM " + template.getMeasurementName();
		Query query = new Query(queryStr, databaseName);
		QueryResult results = influxDB.query(query);

		if (null == results || null == results.getResults() || results.getResults().isEmpty() || null == results.getResults().get(0).getSeries() || results.getResults().get(0).getSeries().isEmpty()
				|| null == results.getResults().get(0).getSeries().get(0) || null == results.getResults().get(0).getSeries().get(0).getValues()
				|| results.getResults().get(0).getSeries().get(0).getValues().isEmpty() || null == results.getResults().get(0).getSeries().get(0).getValues().get(0)
				|| results.getResults().get(0).getSeries().get(0).getValues().get(0).isEmpty() || null == results.getResults().get(0).getSeries().get(0).getValues().get(0).get(0)) {
			return -1;
		}
		Result result = results.getResults().get(0);
		Series series = result.getSeries().get(0);

		Date date = null;
		try {
			;
			String str = (String) series.getValues().get(0).get(0);
			str = str.replace("Z", "");
			if (str.contains(".")) {
				str = str.substring(0, str.lastIndexOf('.'));
			}
			date = format.parse(str);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return date.getTime();
	}

}
