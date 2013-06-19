package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableInternet;
import ikube.toolkit.ThreadUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.social.twitter.api.Stream;
import org.springframework.social.twitter.api.StreamDeleteEvent;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.StreamWarningEvent;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.UserStreamParameters;
import org.springframework.social.twitter.api.UserStreamParameters.WithOptions;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

/**
 * @author Michael Couck
 * @since 24.04.13
 * @version 01.00
 */
public class TwitterHandler extends IndexableHandler<IndexableInternet> {

	class ResourceProvider implements IResourceProvider<Tweet> {

		private Stack<Tweet> tweets = new Stack<>();

		ResourceProvider(final IndexableInternet indexableInternet) throws IOException {
			TwitterTemplate twitter = new TwitterTemplate("YR571S2JiVBOFyJS5MEg", "Kb8hS0luftwCJX3qVoyiLUMfZDtK1EozFoUkjNLUMx4",
					"7078572-srXzIDwmIc0lg69TBR0rLr9TqHYJiRqPIv14gunpk", "xfnQDjYEcdTCcYWeLcJyGuszQG9R5UnG5TrvtfG33BU");
			List<StreamListener> listeners = new ArrayList<StreamListener>();
			StreamListener listener = new StreamListener() {
				public void onTweet(Tweet tweet) {
					logger.info("TWEET:  " + tweet.getFromUser() + "   -   " + tweet.getText());
					tweets.push(tweet);
				}

				public void onLimit(int numberOfLimitedTweets) {
					logger.info("LIMIT:  " + numberOfLimitedTweets);
				}

				public void onDelete(StreamDeleteEvent deleteEvent) {
					logger.info("DELETE:  " + deleteEvent.getTweetId());
				}

				public void onWarning(StreamWarningEvent warnEvent) {
					logger.info("WARNING:  " + warnEvent.getCode());
				}
			};
			listeners.add(listener);
			UserStreamParameters params = new UserStreamParameters().with(WithOptions.FOLLOWINGS).includeReplies(true);
			// Stream userStream = twitter.streamingOperations().sample(listeners);
			Stream userStream = twitter.streamingOperations().filter("Disney", listeners);
			// twitter.streamingOperations().sample(listeners);
			ThreadUtilities.sleep(100000);
			userStream.stop();
		}

		public Tweet getResource() {
			if (tweets.isEmpty()) {
				return null;
			}
			return tweets.pop();
		}

		@Override
		public void setResources(List<Tweet> resources) {
			tweets.addAll(resources);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handleIndexable(final IndexContext<?> indexContext, final IndexableInternet indexableInternet) throws Exception {
		final AtomicInteger threads = new AtomicInteger(indexableInternet.getThreads());
		ForkJoinPool forkJoinPool = new ForkJoinPool(threads.get());
		ResourceProvider fileResourceProvider = new ResourceProvider(indexableInternet);
		RecursiveAction recursiveAction = getRecursiveAction(indexContext, indexableInternet, fileResourceProvider);
		forkJoinPool.invoke(recursiveAction);
		return new ArrayList<Future<?>>(Arrays.asList(recursiveAction));
	}

	@Override
	protected List<?> handleResource(final IndexContext<?> indexContext, final Indexable<?> indexable, final Object resource) {
		logger.info("Handling resource : " + resource + ", thread : " + Thread.currentThread().hashCode());
		return null;
	}

}