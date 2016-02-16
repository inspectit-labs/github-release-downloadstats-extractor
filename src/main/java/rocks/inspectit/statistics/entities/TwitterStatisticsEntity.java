package rocks.inspectit.statistics.entities;

import rocks.inspectit.statistics.entities.EntityField.MetricType;

public class TwitterStatisticsEntity extends AbstractStatisticsEntity {
	/**
	 * Tags and fields.
	 */
	public static final String TWITTER_MEASUREMENT = "twitter";
	public static final String TWITTER_OWN_TWEETS_FIELD = "own_tweets";
	public static final String TWITTER_RETWEETS_FIELD = "retweets";
	public static final String TWITTER_RETWEETS_OF_OWN_FIELD = "retweets_own_tweets";
	public static final String TWITTER_MENTIONS_FIELD = "mentions";
	public static final String TWITTER_LIKES_FIELD = "likes";
	public static final String TWITTER_MENTION_LIKES_FIELD = "mention_likes";
	public static final String TWITTER_FOLLOWERS_FIELD = "followers";

	public static final String[] KEY_NAMES = new String[0];
	public static final String[] FIELD_NAMES = new String[] { TWITTER_OWN_TWEETS_FIELD, TWITTER_RETWEETS_FIELD, TWITTER_RETWEETS_OF_OWN_FIELD, TWITTER_MENTIONS_FIELD, TWITTER_LIKES_FIELD,
			TWITTER_MENTION_LIKES_FIELD, TWITTER_FOLLOWERS_FIELD };

	/**
	 * template instance.
	 */
	private static TwitterStatisticsEntity template;

	/**
	 * 
	 * @return template instance
	 */
	public static TwitterStatisticsEntity getTemplate() {
		if (null == template) {
			template = new TwitterStatisticsEntity();
		}
		return template;
	}

	@EntityField(name = TWITTER_OWN_TWEETS_FIELD, metricType = MetricType.RELATIVE)
	protected int ownTweets;
	@EntityField(name = TWITTER_RETWEETS_FIELD, metricType = MetricType.RELATIVE)
	protected int retweets;
	@EntityField(name = TWITTER_RETWEETS_OF_OWN_FIELD, metricType = MetricType.RELATIVE)
	protected int retweetsOfOwnTweets;
	@EntityField(name = TWITTER_MENTIONS_FIELD, metricType = MetricType.RELATIVE)
	protected int mentions;
	@EntityField(name = TWITTER_LIKES_FIELD, metricType = MetricType.RELATIVE)
	protected int likes;
	@EntityField(name = TWITTER_MENTION_LIKES_FIELD, metricType = MetricType.RELATIVE)
	protected int mentionLikes;
	@EntityField(name = TWITTER_FOLLOWERS_FIELD, metricType = MetricType.ABSOLUTE)
	protected int followers;

	/**
	 * Constructor.
	 */
	private TwitterStatisticsEntity() {
		super(TWITTER_MEASUREMENT, 0L, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param timestamp
	 * @param ownTweets
	 * @param retweets
	 * @param retweetsOfOwnTweets
	 * @param mentions
	 * @param likes
	 * @param mentionLikes
	 * @param followers
	 */
	public TwitterStatisticsEntity(long timestamp, int ownTweets, int retweets, int retweetsOfOwnTweets, int mentions, int likes, int mentionLikes, int followers) {
		super(TWITTER_MEASUREMENT, timestamp, new String[0]);
		this.ownTweets = ownTweets;
		this.retweets = retweets;
		this.mentions = mentions;
		this.likes = likes;
		this.mentionLikes = mentionLikes;
		this.followers = followers;
		this.setRetweetsOfOwnTweets(retweetsOfOwnTweets);
	}

	/**
	 * Constructor.
	 * 
	 * @param keys
	 *            key values
	 * @param fields
	 *            field values
	 * @param timestamp
	 *            timestamp
	 */
	public TwitterStatisticsEntity(String[] keys, Object[] fields, long timestamp) {
		super(TWITTER_MEASUREMENT, timestamp, keys);

		if (fields.length < 7) {
			throw new IllegalArgumentException("Invalid amount of field values!");
		}

		ownTweets = getIntValue(fields[0]);
		retweets = getIntValue(fields[1]);
		setRetweetsOfOwnTweets(getIntValue(fields[2]));
		mentions = getIntValue(fields[3]);
		likes = getIntValue(fields[4]);
		mentionLikes = getIntValue(fields[5]);
		followers = getIntValue(fields[6]);
	}

	@Override
	public String[] getKeyNames() {
		return KEY_NAMES;
	}

	@Override
	public String[] getFieldNames() {
		return FIELD_NAMES;
	}

	@Override
	public Object[] getFieldValuesList() {
		return new Object[] { ownTweets, retweets, getRetweetsOfOwnTweets(), mentions, likes, mentionLikes, followers };
	}

	/**
	 * @return the ownTweets
	 */
	public int getOwnTweets() {
		return ownTweets;
	}

	/**
	 * @param ownTweets
	 *            the ownTweets to set
	 */
	public void setOwnTweets(int ownTweets) {
		this.ownTweets = ownTweets;
	}

	/**
	 * @return the retweets
	 */
	public int getRetweets() {
		return retweets;
	}

	/**
	 * @param retweets
	 *            the retweets to set
	 */
	public void setRetweets(int retweets) {
		this.retweets = retweets;
	}

	/**
	 * @return the mentions
	 */
	public int getMentions() {
		return mentions;
	}

	/**
	 * @param mentions
	 *            the mentions to set
	 */
	public void setMentions(int mentions) {
		this.mentions = mentions;
	}

	/**
	 * @return the likes
	 */
	public int getLikes() {
		return likes;
	}

	/**
	 * @param likes
	 *            the likes to set
	 */
	public void setLikes(int likes) {
		this.likes = likes;
	}

	/**
	 * @return the mentionLikes
	 */
	public int getMentionLikes() {
		return mentionLikes;
	}

	/**
	 * @param mentionLikes
	 *            the mentionLikes to set
	 */
	public void setMentionLikes(int mentionLikes) {
		this.mentionLikes = mentionLikes;
	}

	/**
	 * @return the followers
	 */
	public int getFollowers() {
		return followers;
	}

	/**
	 * @param followers
	 *            the followers to set
	 */
	public void setFollowers(int followers) {
		this.followers = followers;
	}

	/**
	 * @return the retweetsOfOwnTweets
	 */
	public int getRetweetsOfOwnTweets() {
		return retweetsOfOwnTweets;
	}

	/**
	 * @param retweetsOfOwnTweets
	 *            the retweetsOfOwnTweets to set
	 */
	public void setRetweetsOfOwnTweets(int retweetsOfOwnTweets) {
		this.retweetsOfOwnTweets = retweetsOfOwnTweets;
	}

}
