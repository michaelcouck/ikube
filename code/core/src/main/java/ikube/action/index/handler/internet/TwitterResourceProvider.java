package ikube.action.index.handler.internet;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableTweets;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
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

import com.google.gson.Gson;

/**
 * This class will use the Spring social module to get stack from Twitter, at a rate of around 1% of the stack.
 * 
 * @author Michael Couck
 * @since 19.06.13
 * @version 01.00
 */
class TwitterResourceProvider implements IResourceProvider<Tweet> {

	private class TwitterStreamListener implements StreamListener {

		/** A counter to see how many stack we have done. */
		private AtomicLong counter;
		private Stack<Tweet> stack;
		/** The directories where the stack will be persisted to the file system. */
		private File analylticsDirectory;
		private File tweetsDirectory;

		public TwitterStreamListener() {
			counter = new AtomicLong();
			stack = new Stack<Tweet>();
			analylticsDirectory = FileUtilities.getOrCreateDirectory(IConstants.ANALYTICS_DIRECTORY);
			tweetsDirectory = FileUtilities.getOrCreateDirectory(new File(analylticsDirectory, "tweets"));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onTweet(final Tweet tweet) {
			if (stack.size() < 1000000) {
				if (stack.size() > 1000) {
					synchronized (stack) {
						logger.info("Tweet counter : " + counter.get() + ", stack : " + stack.size());
						setResources(stack);
						persistResources(stack);
						stack.clear();
						stack.notifyAll();
					}
				}
				stack.push(tweet);
				counter.incrementAndGet();
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

		private void persistResources(final List<Tweet> tweets) {
			try {
				File latestDirectory = IndexManager.getLatestIndexDirectory(tweetsDirectory, null);
				if (latestDirectory == null) {
					latestDirectory = FileUtilities.getOrCreateDirectory(new File(tweetsDirectory, Long.toString(System.currentTimeMillis())));
				} else {
					String[] files = latestDirectory.list();
					if (files != null && files.length > 10000) {
						latestDirectory = FileUtilities.getOrCreateDirectory(new File(tweetsDirectory, Long.toString(System.currentTimeMillis())));
					}
				}
				Gson gson = new Gson();
				for (final Tweet tweet : tweets) {
					String string = gson.toJson(tweet);
					File output = new File(latestDirectory, Long.toString(System.currentTimeMillis()) + ".json");
					File outputFile = FileUtilities.getOrCreateFile(output);
					FileUtilities.setContents(outputFile, string.getBytes());
					ThreadUtilities.sleep(1);
				}
			} catch (Exception e) {
				logger.error(null, e);
			}
		}
	}

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/** This collection is used to stock pile the stack, waiting for consumers. */
	private Stack<Tweet> tweets;

	/**
	 * Constructor takes the configuration for the Twitter account, and initializes the streaming classes that will accept the Twitter data.
	 * 
	 * @param indexableTweets the configuration for the Twitter account, importantly the OAuth login details
	 * @throws IOException
	 */
	TwitterResourceProvider(final IndexableTweets indexableTweets) throws IOException {
		tweets = new Stack<Tweet>();
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
				wait(1000);
			} catch (InterruptedException e) {
				logger.error(null, e);
			} catch (Exception e) {
				logger.error(null, e);
				return null;
			}
		}
		return tweets.pop();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void setResources(final List<Tweet> tweets) {
		if (tweets != null) {
			this.tweets.addAll(tweets);
		}
	}

}