package rocks.inspectit.statistics.entities;

import java.util.Map;

public class TwitterStatisticsEntity extends AbstractStatisticsEntity {
	private static final String TWITTER_MEASUREMENT = "twitter";
	private static final String TWITTER_OWN_TWEETS_FIELD = "own_tweets";
	private static final String TWITTER_RETWEETS_FIELD = "retweets";
	private static final String TWITTER_RETWEETS_OF_OWN_FIELD = "retweets_own_tweets";
	private static final String TWITTER_MENTIONS_FIELD = "mentions";
	private static final String TWITTER_LIKES_FIELD = "likes";
	private static final String TWITTER_MENTION_LIKES_FIELD = "mention_likes";
	private static final String TWITTER_FOLLOWERS_FIELD = "followers";

	private static final String[] KEY_NAMES = new String[0];
	private static final String[] FIELD_NAMES = new String[] { TWITTER_OWN_TWEETS_FIELD, TWITTER_RETWEETS_FIELD, TWITTER_RETWEETS_OF_OWN_FIELD, TWITTER_MENTIONS_FIELD, TWITTER_LIKES_FIELD,
			TWITTER_MENTION_LIKES_FIELD, TWITTER_FOLLOWERS_FIELD };

	private static TwitterStatisticsEntity template;

	public static TwitterStatisticsEntity getTemplate() {
		if (null == template) {
			template = new TwitterStatisticsEntity();
		}
		return template;
	}

	int ownTweets;
	int retweets;
	private int retweetsOfOwnTweets;
	int mentions;
	int likes;
	int mentionLikes;
	int followers;

	private TwitterStatisticsEntity() {
		super(TWITTER_MEASUREMENT, 0L, null);
	}

	/**
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

	@Override
	public void setFields(Map<String, Object> fieldValues) {
		ownTweets = getIntValue(fieldValues.get(TWITTER_OWN_TWEETS_FIELD));
		retweets = getIntValue(fieldValues.get(TWITTER_RETWEETS_FIELD));
		setRetweetsOfOwnTweets(getIntValue(fieldValues.get(TWITTER_RETWEETS_OF_OWN_FIELD)));
		mentions = getIntValue(fieldValues.get(TWITTER_MENTIONS_FIELD));
		likes = getIntValue(fieldValues.get(TWITTER_LIKES_FIELD));
		mentionLikes = getIntValue(fieldValues.get(TWITTER_MENTION_LIKES_FIELD));
		followers = getIntValue(fieldValues.get(TWITTER_FOLLOWERS_FIELD));
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
