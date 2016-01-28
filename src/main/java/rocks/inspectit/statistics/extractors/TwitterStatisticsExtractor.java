package rocks.inspectit.statistics.extractors;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.time.DateUtils;
import org.influxdb.InfluxDB;
import org.json.JSONArray;

import rocks.inspectit.statistics.StatisticsExtractor;
import rocks.inspectit.statistics.entities.GithubTrafficStatisticsEntity;
import rocks.inspectit.statistics.entities.TwitterStatisticsEntity;
import rocks.inspectit.statistics.source.CSVSource;
import rocks.inspectit.statistics.source.InfluxDBSource;
import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class TwitterStatisticsExtractor extends AbstractExtractor<TwitterStatisticsEntity> {

	private static final String CONSUMER_KEY_KEY = "twitter.consumer.key";
	private static final String CONSUMER_SECRET_KEY = "twitter.consumer.secret";
	private static final String TOKEN_KEY = "twitter.token";
	private static final String TOKEN_SECRET_KEY = "twitter.token.secret";
	private static final String EXPORT_CSV_FILE_KEY = "twitter.target.csv.file.export";
	private static final String IMPORT_CSV_FILE_KEY = "twitter.target.csv.file.import";
	private static final String IMPORT_FROM_CSV = "twitter.import.from.csv.before";

	public TwitterStatisticsExtractor(Properties properties, InfluxDB influxDB) {
		super(properties);
		InfluxDBSource<TwitterStatisticsEntity> influxDBSource = new InfluxDBSource<TwitterStatisticsEntity>(influxDB, properties.getProperty(StatisticsExtractor.INFLUX_DB_DATABASE_KEY));
		CSVSource<TwitterStatisticsEntity> csvExportDataSource = new CSVSource<TwitterStatisticsEntity>(properties.getProperty(EXPORT_CSV_FILE_KEY));
		boolean importFromCsv = Boolean.parseBoolean(properties.getProperty(IMPORT_FROM_CSV, "false"));
		CSVSource<TwitterStatisticsEntity> csvImportDataSource = null;
		if (importFromCsv) {
			csvImportDataSource = new CSVSource<TwitterStatisticsEntity>(properties.getProperty(IMPORT_CSV_FILE_KEY));
		}

		init(null, TwitterStatisticsEntity.getTemplate(), influxDBSource, csvImportDataSource, csvExportDataSource, 0L);
	}

	@Override
	protected List<TwitterStatisticsEntity> getResultList(String jsonString) {
		System.out.println("Retrieving Twitter statistics...");
		Twitter twitter = TwitterFactory.getSingleton();
		AccessToken accessToken = new AccessToken(getProperties().getProperty(TOKEN_KEY), getProperties().getProperty(TOKEN_SECRET_KEY));
		twitter.setOAuthConsumer(getProperties().getProperty(CONSUMER_KEY_KEY), getProperties().getProperty(CONSUMER_SECRET_KEY));
		twitter.setOAuthAccessToken(accessToken);

		int tweets = 0;
		int retweets = 0;
		int retweetsOfOwn = 0;
		int mentions = 0;
		int likes = 0;
		int mentionLikes = 0;
		int followers = 0;
		
		try {
			
			// retrieve tweets and likes
			int i = 1;
			ResponseList<Status> response = twitter.getUserTimeline(new Paging(i, 100));
			while(null != response && !response.isEmpty()){
				for (Status stat : response) {
					if (!stat.isRetweet()) {
						likes += stat.getFavoriteCount();
						tweets++;
					}else{
						retweets++;
					}
				}
				i++;
				response = twitter.getUserTimeline(new Paging(i, 100));
			}
			
			// retrieve followers
			IDs followerIDs = twitter.getFollowersIDs(-1);
			while(null != followerIDs && followerIDs.getIDs().length > 0){
				followers += followerIDs.getIDs().length;
				followerIDs = twitter.getFollowersIDs(followerIDs.getNextCursor());
			}
			
			// retrieve retweets of own tweets
			i = 1;
			response = twitter.getRetweetsOfMe(new Paging(i, 100));
			while(null != response && !response.isEmpty()){
				for (Status stat : response) {
					retweetsOfOwn += stat.getRetweetCount();
				}
				i++;
				response = twitter.getRetweetsOfMe(new Paging(i, 100));
			}
			
			// retrieve mentions and likes of mentions
			i = 1;
			response = twitter.getMentionsTimeline(new Paging(i, 100));
			while(null != response && !response.isEmpty()){
				mentions += response.size();
				for (Status stat : response) {
					mentionLikes += stat.getFavoriteCount();
				}
				i++;
				response = twitter.getMentionsTimeline(new Paging(i, 100));
			}
			
			
		} catch (TwitterException e) {
			throw new RuntimeException(e);
		}
		
		long timestamp = System.currentTimeMillis();
		Date roundedDate = DateUtils.truncate(new Date(timestamp), Calendar.DATE);
		TwitterStatisticsEntity entity = new TwitterStatisticsEntity(roundedDate.getTime(), tweets,  retweets, retweetsOfOwn, mentions, likes, mentionLikes, followers);
		System.out.println(entity);

		List<TwitterStatisticsEntity> statistics = new ArrayList<TwitterStatisticsEntity>();
		statistics.add(entity);
		System.out.println("Successfully retrieved Twitter statistics.");
		return statistics;
	}

	@Override
	protected boolean fieldCountsAbsolute() {
		return true;
	}

	@Override
	protected String getJSONString() {
		// not required here
		return null;
	}

	@Override
	protected void initProperties(Properties properties) {
		if (!properties.contains(CONSUMER_KEY_KEY) && System.getenv(CONSUMER_KEY_KEY) != null) {
			properties.setProperty(CONSUMER_KEY_KEY, System.getenv(CONSUMER_KEY_KEY));
		}
		if (!properties.contains(CONSUMER_SECRET_KEY) && System.getenv(CONSUMER_SECRET_KEY) != null) {
			properties.setProperty(CONSUMER_SECRET_KEY, System.getenv(CONSUMER_SECRET_KEY));
		}

		if (!properties.contains(TOKEN_KEY) && System.getenv(TOKEN_KEY) != null) {
			properties.setProperty(TOKEN_KEY, System.getenv(TOKEN_KEY));
		}

		if (!properties.contains(TOKEN_SECRET_KEY) && System.getenv(TOKEN_SECRET_KEY) != null) {
			properties.setProperty(TOKEN_SECRET_KEY, System.getenv(TOKEN_SECRET_KEY));
		}

		if (!properties.contains(EXPORT_CSV_FILE_KEY) && System.getenv(EXPORT_CSV_FILE_KEY) != null) {
			properties.setProperty(EXPORT_CSV_FILE_KEY, System.getenv(EXPORT_CSV_FILE_KEY));
		}

		if (!properties.contains(IMPORT_FROM_CSV) && System.getenv(IMPORT_FROM_CSV) != null) {
			properties.setProperty(IMPORT_FROM_CSV, System.getenv(IMPORT_FROM_CSV));
		}
		if (!properties.contains(IMPORT_CSV_FILE_KEY) && System.getenv(IMPORT_CSV_FILE_KEY) != null) {
			properties.setProperty(IMPORT_CSV_FILE_KEY, System.getenv(IMPORT_CSV_FILE_KEY));
		}

	}

	@Override
	protected void checkProperties(Properties properties) {
		if (!properties.containsKey(CONSUMER_KEY_KEY)) {
			throw new IllegalArgumentException("GitHub API URL not specified: " + CONSUMER_KEY_KEY);
		}
		if (!properties.containsKey(CONSUMER_SECRET_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + CONSUMER_SECRET_KEY);
		}
		if (!properties.containsKey(TOKEN_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + TOKEN_KEY);
		}
		if (!properties.containsKey(TOKEN_SECRET_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + TOKEN_SECRET_KEY);
		}

		if (!properties.containsKey(EXPORT_CSV_FILE_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + EXPORT_CSV_FILE_KEY);
		}

		if (Boolean.parseBoolean(properties.getProperty(IMPORT_FROM_CSV, "false")) && !properties.containsKey(IMPORT_CSV_FILE_KEY)) {
			throw new IllegalArgumentException("Property not specified although enabled: " + IMPORT_CSV_FILE_KEY);
		}

	}

}
