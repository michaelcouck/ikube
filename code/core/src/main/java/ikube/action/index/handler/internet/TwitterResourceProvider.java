package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableTweets;
import ikube.toolkit.ThreadUtilities;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.twitter.api.StreamDeleteEvent;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.StreamWarningEvent;
import org.springframework.social.twitter.api.StreamingOperations;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

class TwitterResourceProvider implements IResourceProvider<Tweet> {

	private class TwitterStreamListener implements StreamListener {

		private AtomicLong atomicLong = new AtomicLong();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onTweet(Tweet tweet) {
			if (atomicLong.incrementAndGet() % 10000 == 0) {
				TwitterProfile user = tweet.getUser();
				logger.info("Tweets : " + atomicLong.get() + ", user : " + user.getScreenName() + ", location : " + user.getLocation() + ", tweet : "
						+ tweet.getText());
			}
			tweets.push(tweet);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onLimit(int numberOfLimitedTweets) {
			logger.info("Tweets limited : " + numberOfLimitedTweets);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onDelete(StreamDeleteEvent deleteEvent) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onWarning(StreamWarningEvent warnEvent) {
			logger.info("Tweet warning : " + warnEvent.getCode());
		}
	}

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Stack<Tweet> tweets = new Stack<>();

	TwitterResourceProvider(final IndexableTweets indexableTweets) throws IOException {
		TwitterTemplate twitter = new TwitterTemplate( //
				indexableTweets.getConsumerKey(), //
				indexableTweets.getConsumerSecret(), //
				indexableTweets.getToken(), //
				indexableTweets.getTokenSecret());
		StreamListener listener = new TwitterStreamListener();
		StreamingOperations streamingOperations = twitter.streamingOperations();
		streamingOperations.sample(Arrays.asList(listener));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Tweet getResource() {
		try {
			if (tweets.isEmpty()) {
				ThreadUtilities.sleep(10000);
				return getResource();
			}
			return tweets.pop();
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setResources(final List<Tweet> resources) {
		if (resources != null) {
			tweets.addAll(resources);
		}
	}

}
