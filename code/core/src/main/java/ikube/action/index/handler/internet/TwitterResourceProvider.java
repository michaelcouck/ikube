package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableTweets;

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
import org.springframework.social.twitter.api.impl.TwitterTemplate;

/**
 * This class will use the Spring social module to get tweets from Twitter, at a rate of around 1% of the tweets.
 * 
 * @author Michael Couck
 * @since 19.06.13
 * @version 01.00
 */
class TwitterResourceProvider implements IResourceProvider<Tweet> {

	private class TwitterStreamListener implements StreamListener {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onTweet(final Tweet tweet) {
			if (tweets.size() < 1000000) {
				tweets.push(tweet);
				if (counter.incrementAndGet() % 1000 == 0) {
					logger.info("Tweet counter : " + counter.get() + ", stack : " + tweets.size());
				}
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

	/** A counter to see how many tweets we have done. */
	private AtomicLong counter;
	/** This collection is used to stock pile the tweets, waiting for consumers. */
	private Stack<Tweet> tweets;

	/**
	 * Constructor takes the configuration for the Twitter account, and initializes the streaming classes that will accept the Twitter data.
	 * 
	 * @param indexableTweets the configuration for the Twitter account, importantly the OAuth login details
	 * @throws IOException
	 */
	TwitterResourceProvider(final IndexableTweets indexableTweets) throws IOException {
		tweets = new Stack<Tweet>();
		counter = new AtomicLong();
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
		while (tweets.isEmpty()) {
			try {
				Thread.currentThread().wait(1000);
			} catch (InterruptedException e) {
				logger.error(null, e);
			}
		}
		notifyAll();
		return tweets.pop();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void setResources(final List<Tweet> resources) {
		if (resources != null) {
			tweets.addAll(resources);
		}
		notifyAll();
	}

}