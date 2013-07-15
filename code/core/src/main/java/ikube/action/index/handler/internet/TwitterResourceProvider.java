package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableTweets;
import ikube.toolkit.ThreadUtilities;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.twitter.api.StreamDeleteEvent;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.StreamWarningEvent;
import org.springframework.social.twitter.api.StreamingOperations;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

class TwitterResourceProvider implements IResourceProvider<Tweet> {

	private class TwitterStreamListener implements StreamListener {

		private AtomicLong atomicLong = new AtomicLong(0);

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onTweet(final Tweet tweet) {
			if (atomicLong.getAndIncrement() % 10000 == 0) {
				logger.info("Tweet : " + tweets.size() + ", " + atomicLong.get() + ", "
						+ ToStringBuilder.reflectionToString(tweet, ToStringStyle.SHORT_PREFIX_STYLE));
			}
			if (tweets.size() < 1000) {
				tweets.push(tweet);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onLimit(final int numberOfLimitedTweets) {
			logger.info("Tweets limited : " + numberOfLimitedTweets);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onDelete(final StreamDeleteEvent deleteEvent) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onWarning(final StreamWarningEvent warnEvent) {
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
				ThreadUtilities.sleep(100);
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
