package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableTweets;
import ikube.toolkit.ThreadUtilities;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.twitter.api.StreamDeleteEvent;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.StreamWarningEvent;
import org.springframework.social.twitter.api.StreamingOperations;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

public class TwitterResourceProvider implements IResourceProvider<Tweet> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Stack<Tweet> tweets = new Stack<>();

	TwitterResourceProvider(final IndexableTweets indexableTweets) throws IOException {
		TwitterTemplate twitter = new TwitterTemplate(//
				indexableTweets.getConsumerKey(), //
				indexableTweets.getConsumerSecret(), //
				indexableTweets.getToken(), //
				indexableTweets.getTokenSecret());
		StreamListener listener = new StreamListener() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void onTweet(Tweet tweet) {
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

		};
		StreamingOperations streamingOperations = twitter.streamingOperations();
		streamingOperations.sample(Arrays.asList(listener));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tweet getResource() {
		if (tweets.isEmpty()) {
			ThreadUtilities.sleep(100);
			return getResource();
		}
		return tweets.pop();
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
