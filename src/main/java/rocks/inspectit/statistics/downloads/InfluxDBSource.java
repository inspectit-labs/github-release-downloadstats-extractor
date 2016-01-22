package rocks.inspectit.statistics.downloads;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

public class InfluxDBSource implements IDataSource {

	private static final String SELECT_ALL_COMMAND = "SELECT * FROM downloads";
	private static final String QUERY_GLOBAL_COUNT_COMMAND = "SELECT sum(count) FROM downloads WHERE Architecture='?1' AND Artifact='?2' AND BuildNr='?3' AND MajorVersion='?4' AND MinorVersion='?5' AND OS='?6' AND time > ?7";
	private static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private final String databaseName;
	private final InfluxDB influxDB;

	/**
	 * @param url
	 * @param user
	 * @param password
	 * @param databaseName
	 */
	public InfluxDBSource(String url, String user, String password, String databaseName) {
		influxDB = InfluxDBFactory.connect(url, user, password);

		this.databaseName = databaseName;
	}

	@Override
	public void store(Collection<DownloadStatistics> statistics) {

		BatchPoints batchPoints = BatchPoints.database(databaseName).retentionPolicy("default").consistency(ConsistencyLevel.ALL).build();

		for (DownloadStatistics statisticsPoint : statistics) {
			Point point = Point.measurement(Constants.DOWNLOADS_MEASUREMENT).time(statisticsPoint.getTimestamp(), TimeUnit.MILLISECONDS)
					.field(Constants.COUNT_FIELD, statisticsPoint.getDownloadCount()).tag(Constants.MAJOR_VERSION_TAG, statisticsPoint.getMajorVersion())
					.tag(Constants.MINOR_VERSION_TAG, statisticsPoint.getMinorVersion()).tag(Constants.BUILD_NR_TAG, statisticsPoint.getBuildNr())
					.tag(Constants.ARTIFACT_TAG, statisticsPoint.getArtifactType()).tag(Constants.OS_TAG, statisticsPoint.getOs()).tag(Constants.ARCHITECTURE_TAG, statisticsPoint.getArchitecture())
					.build();

			batchPoints.point(point);
		}

		influxDB.write(batchPoints);
		System.out.println("Successfully wrote data to InfluxDB");

	}

	@Override
	public List<DownloadStatistics> load() {
		Query query = new Query(SELECT_ALL_COMMAND, databaseName);
		QueryResult results = influxDB.query(query);

		if (null == results || null == results.getResults() || results.getResults().isEmpty()) {
			return Collections.emptyList();
		}
		List<DownloadStatistics> statistics = new ArrayList<DownloadStatistics>();
		Result result = results.getResults().get(0);
		for (List<Object> entry : result.getSeries().get(0).getValues()) {
			Date date = null;
			try {
				String str = (String) entry.get(0);
				str = str.replace("Z", "");
				if (str.contains(".")) {
					str = str.substring(0, str.lastIndexOf('.'));
				}
				date = format.parse((String) entry.get(0));
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			long time = date.getTime();
			String architecture = (String) entry.get(1);
			String artifact = (String) entry.get(2);
			String buildNr = (String) entry.get(3);
			String majorV = (String) entry.get(4);
			String minorV = (String) entry.get(5);
			String os = (String) entry.get(6);
			int count = ((Double) entry.get(7)).intValue();
			DownloadStatistics stat = new DownloadStatistics(time, artifact, os, architecture, majorV, minorV, buildNr, count);
			statistics.add(stat);
		}
		return statistics;
	}

	@Override
	public int getAbsoluteCounts(long since, Identifier identifier) {
		String queryStr = QUERY_GLOBAL_COUNT_COMMAND;
		queryStr = queryStr.replace("?1", identifier.getArchitecture());
		queryStr = queryStr.replace("?2", identifier.getArtifactType());
		queryStr = queryStr.replace("?3", identifier.getBuildNr());
		queryStr = queryStr.replace("?4", identifier.getMajorVersion());
		queryStr = queryStr.replace("?5", identifier.getMinorVersion());
		queryStr = queryStr.replace("?6", identifier.getOs());
		queryStr = queryStr.replace("?7", String.valueOf(since * 1000000L));
		Query query = new Query(queryStr, databaseName);
		QueryResult results = influxDB.query(query);

		if (null == results || null == results.getResults() || results.getResults().isEmpty() || null == results.getResults().get(0).getSeries() || results.getResults().get(0).getSeries().isEmpty()
				|| null == results.getResults().get(0).getSeries().get(0) || null == results.getResults().get(0).getSeries().get(0).getValues()
				|| results.getResults().get(0).getSeries().get(0).getValues().isEmpty() || null == results.getResults().get(0).getSeries().get(0).getValues().get(0)
				|| results.getResults().get(0).getSeries().get(0).getValues().get(0).isEmpty() || null == results.getResults().get(0).getSeries().get(0).getValues().get(0).get(0)) {
			return 0;
		}
		Result result = results.getResults().get(0);
		Series series = result.getSeries().get(0);
		Object value = series.getValues().get(0).get(1);
		int sum = 0;
		if (value != null) {
			sum = ((Double) value).intValue();
		}

		return sum;
	}

}
