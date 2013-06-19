package ikube.action.index.handler.internet;

import static ikube.toolkit.ObjectToolkit.populateFields;
import ikube.AbstractTest;
import ikube.model.IndexableInternet;
import ikube.toolkit.ThreadUtilities;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.social.twitter.api.Stream;
import org.springframework.social.twitter.api.StreamDeleteEvent;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.StreamWarningEvent;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
// @Ignore
public class TwitterHandlerTest extends AbstractTest {

	private TwitterHandler twitterHandler;

	@Before
	public void before() {
		twitterHandler = new TwitterHandler();
	}

	@Test
	@Ignore
	public void handleIndexable() throws Exception {
		ThreadUtilities.initialize();
		IndexableInternet indexableInternet = populateFields(IndexableInternet.class, new IndexableInternet(), Boolean.TRUE, 5);
		indexableInternet.setThreads(1);
		List<Future<?>> futures = twitterHandler.handleIndexable(indexContext, indexableInternet);
		ThreadUtilities.waitForFutures(futures, 60000);
	}

	@Test
	public void twitterApi() {
		TwitterTemplate twitter = new TwitterTemplate("YR571S2JiVBOFyJS5MEg", "Kb8hS0luftwCJX3qVoyiLUMfZDtK1EozFoUkjNLUMx4",
				"7078572-srXzIDwmIc0lg69TBR0rLr9TqHYJiRqPIv14gunpk", "xfnQDjYEcdTCcYWeLcJyGuszQG9R5UnG5TrvtfG33BU");
		StreamListener listener = new StreamListener() {

			@Override
			public void onTweet(Tweet tweet) {
				logger.info("TWEET:  " + tweet.getFromUser() + "   -   " + tweet.getText());
			}

			@Override
			public void onLimit(int numberOfLimitedTweets) {
				logger.info("LIMIT:  " + numberOfLimitedTweets);
			}

			@Override
			public void onDelete(StreamDeleteEvent deleteEvent) {
				logger.info("DELETE:  " + deleteEvent.getTweetId());
			}

			@Override
			public void onWarning(StreamWarningEvent warnEvent) {
				logger.info("WARNING:  " + warnEvent.getCode());
			}

		};
		// UserStreamParameters params = new UserStreamParameters().with(WithOptions.FOLLOWINGS).includeReplies(true);
		// Stream userStream = twitter.streamingOperations().sample(listeners);
		// Stream userStream = twitter.streamingOperations().filter("Disney", listeners);
		// Stream userStream = twitter.streamingOperations().firehose(Arrays.asList(listener));
		// Stream userStream = twitter.streamingOperations().sample(listeners);
		Stream userStream = twitter.streamingOperations().sample(Arrays.asList(listener));
		ThreadUtilities.sleep(100000);
		userStream.stop();
	}

}