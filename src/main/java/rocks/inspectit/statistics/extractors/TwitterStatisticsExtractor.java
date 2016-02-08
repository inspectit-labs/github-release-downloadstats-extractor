package rocks.inspectit.statistics.extractors;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateUtils;
import org.influxdb.InfluxDB;

import rocks.inspectit.statistics.entities.TwitterStatisticsEntity;
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

	public TwitterStatisticsExtractor(Properties properties, InfluxDB influxDB) {
		super(properties);
		init(null, TwitterStatisticsEntity.getTemplate(), influxDB, 0L);
	}

	@Override
	public List<TwitterStatisticsEntity> getResultList(String jsonString) {
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
			while (null != response && !response.isEmpty()) {
				for (Status stat : response) {
					if (!stat.isRetweet()) {
						likes += stat.getFavoriteCount();
						tweets++;
					} else {
						retweets++;
					}
				}
				i++;
				response = twitter.getUserTimeline(new Paging(i, 100));
			}

			// retrieve followers
			IDs followerIDs = twitter.getFollowersIDs(-1);
			while (null != followerIDs && followerIDs.getIDs().length > 0) {
				followers += followerIDs.getIDs().length;
				followerIDs = twitter.getFollowersIDs(followerIDs.getNextCursor());
			}

			// retrieve retweets of own tweets
			i = 1;
			response = twitter.getRetweetsOfMe(new Paging(i, 100));
			while (null != response && !response.isEmpty()) {
				for (Status stat : response) {
					retweetsOfOwn += stat.getRetweetCount();
				}
				i++;
				response = twitter.getRetweetsOfMe(new Paging(i, 100));
			}

			// retrieve mentions and likes of mentions
			i = 1;
			response = twitter.getMentionsTimeline(new Paging(i, 100));
			while (null != response && !response.isEmpty()) {
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

		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		long timestamp = DateUtils.truncate(cal, Calendar.DATE).getTimeInMillis();
		TwitterStatisticsEntity entity = new TwitterStatisticsEntity(timestamp, tweets, retweets, retweetsOfOwn, mentions, likes, mentionLikes, followers);

		List<TwitterStatisticsEntity> statistics = new ArrayList<TwitterStatisticsEntity>();
		statistics.add(entity);
		System.out.println("Successfully retrieved Twitter statistics.");
		return statistics;
	}

	@Override
	protected boolean needsRelativationOfValues() {
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
	}

}
