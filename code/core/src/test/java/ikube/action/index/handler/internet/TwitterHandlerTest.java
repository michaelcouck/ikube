package ikube.action.index.handler.internet;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import ikube.AbstractTest;
import ikube.model.IndexContext;
import ikube.model.IndexableTweets;
import ikube.toolkit.ObjectToolkit;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.ThreadUtilities;

import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinTask;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TwitterProfile;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class TwitterHandlerTest extends AbstractTest {

	@MockClass(realClass = TwitterResourceProvider.class)
	public static class TwitterResourceProviderMock {
		@Mock
		public Tweet getResource() {
			return mock(Tweet.class);
		}
	}

	private TwitterHandler twitterHandler;
	private IndexableTweets indexableTweets;
	private TwitterResourceHandler twitterResourceHandler;

	@Before
	public void before() throws Exception {
		twitterHandler = new TwitterHandler();

		indexableTweets = new IndexableTweets();
		indexableTweets.setThreads(3);
		indexableTweets.setConsumerKey("Sohh43DylUwaXr7smSojBA");
		indexableTweets.setConsumerSecret("90xubtexbSwhHBbKXM62pF4QfJnz1NWVkpevwde3Qxo");
		indexableTweets.setToken("380355068-JIMLrQyZglGs4WLXo2UShCmXMAMjWeaiZ15ZJkrp");
		indexableTweets.setTokenSecret("OyhI9UyioglNWrhJnQQWY2ULmNtt9Azfl70z0l8jOPM");

		twitterResourceHandler = mock(TwitterResourceHandler.class);
		Deencapsulation.setField(twitterHandler, "twitterResourceHandler", twitterResourceHandler);

		Mockit.setUpMocks(TwitterResourceProviderMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(TwitterResourceProviderMock.class);
	}

	@Test
	public void handleIndexable() throws Exception {
		final String forkJoinPoolName = indexContext.getName();
		new Thread(new Runnable() {
			public void run() {
				ThreadUtilities.sleep(3000);
				ThreadUtilities.cancellForkJoinPool(forkJoinPoolName);
			}
		}).start();

		final ForkJoinTask<?> forkJoinTask = twitterHandler.handleIndexableForked(indexContext, indexableTweets);
		try {
			ThreadUtilities.executeForkJoinTasks(forkJoinPoolName, indexableTweets.getThreads(), forkJoinTask);
			ThreadUtilities.sleep(15000);
		} catch (CancellationException e) {
			// Ignore
		}
		verify(twitterResourceHandler, atLeastOnce())
				.handleResource(any(IndexContext.class), any(IndexableTweets.class), any(Document.class), any(Tweet.class));
	}

	@Test
	public void handleResource() {
		final Tweet tweet = ObjectToolkit.populateFields(new Tweet(0, "The tweet text", new Date(), "michael.couck", "", Long.valueOf(1), Long.valueOf(1),
				"en", "Twitter"), Boolean.TRUE, 10);
		TwitterProfile twitterProfile = ObjectToolkit.populateFields(new TwitterProfile(1, "michael.couck", "Michael", "ikube.be", "michael.couck", "The dude",
				"Gent", new Date()), Boolean.TRUE, 10);
		tweet.setUser(twitterProfile);

		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				twitterHandler.handleResource(indexContext, indexableTweets, tweet);
			}
		}, "Emoticon strategy : ", 1000, Boolean.TRUE);
		assertTrue(executionsPerSecond > 1000);
	}

}