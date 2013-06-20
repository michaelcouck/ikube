package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableTweets;
import ikube.toolkit.ThreadUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.social.twitter.api.StreamDeleteEvent;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.StreamWarningEvent;
import org.springframework.social.twitter.api.StreamingOperations;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

/**
 * @author Michael Couck
 * @since 24.04.13
 * @version 01.00
 */
public class TwitterHandler extends IndexableHandler<IndexableTweets> {

	class ResourceProvider implements IResourceProvider<Tweet> {

		private Stack<Tweet> tweets = new Stack<>();

		ResourceProvider(final IndexableTweets indexableTweets) throws IOException {
			TwitterTemplate twitter = new TwitterTemplate(//
					indexableTweets.getConsumerKey(), //
					indexableTweets.getConsumerSecret(), //
					indexableTweets.getToken(), //
					indexableTweets.getTokenSecret());
			StreamListener listener = new StreamListener() {

				@Override
				public void onTweet(Tweet tweet) {
					tweets.push(tweet);
				}

				@Override
				public void onLimit(int numberOfLimitedTweets) {
					logger.info("Tweets limited : " + numberOfLimitedTweets);
				}

				@Override
				public void onDelete(StreamDeleteEvent deleteEvent) {
				}

				@Override
				public void onWarning(StreamWarningEvent warnEvent) {
					logger.info("Tweet warning : " + warnEvent.getCode());
				}

			};
			StreamingOperations streamingOperations = twitter.streamingOperations();
			streamingOperations.sample(Arrays.asList(listener));
		}

		public Tweet getResource() {
			if (tweets.isEmpty()) {
				ThreadUtilities.sleep(100);
				return getResource();
			}
			return tweets.pop();
		}

		@Override
		public void setResources(final List<Tweet> resources) {
			if (resources != null) {
				tweets.addAll(resources);
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handleIndexable(final IndexContext<?> indexContext, final IndexableTweets indexableTweets) throws Exception {
		ForkJoinPool forkJoinPool = new ForkJoinPool(indexableTweets.getThreads());
		ResourceProvider twitterResourceProvider = new ResourceProvider(indexableTweets);
		RecursiveAction recursiveAction = getRecursiveAction(indexContext, indexableTweets, twitterResourceProvider);
		forkJoinPool.submit(recursiveAction);
		return new ArrayList<Future<?>>(Arrays.asList(recursiveAction));
	}

	@Override
	protected List<?> handleResource(final IndexContext<?> indexContext, final Indexable<?> indexable, final Object resource) {
		Tweet tweet = (Tweet) resource;
		String location = tweet.getUser().getLocation();
		logger.info("Handling resource : " + location);
		// logger.info("Handling resource : " + ToStringBuilder.reflectionToString(resource) + ", thread : " + Thread.currentThread().hashCode());
		return null;
	}

}