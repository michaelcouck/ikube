package ikube.action.index.handler.internet;

import static junit.framework.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.model.IndexableTweets;
import ikube.toolkit.ObjectToolkit;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.ThreadUtilities;

import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinTask;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TwitterProfile;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class TwitterHandlerTest extends AbstractTest {

	private TwitterHandler twitterHandler;
	private IndexableTweets indexableTweets;
	@Cascading
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

		Mockit.setUpMocks();
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		try {
		} catch (CancellationException e) {
			logger.error("Fork join cancelled : ");
		} catch (Exception e) {
			logger.error(null, e);
		}
	}

	@Test
	public void handleIndexable() throws Exception {
		Deencapsulation.setField(twitterHandler, "twitterResourceHandler", twitterResourceHandler);

		final ForkJoinTask<?> forkJoinTask = twitterHandler.handleIndexableForked(indexContext, indexableTweets);
		ThreadUtilities.executeForkJoinTasks(indexContext.getName(), indexableTweets.getThreads(), forkJoinTask);

		ThreadUtilities.sleep(5000);
		ThreadUtilities.cancellForkJoinPool(indexContext.getName());
	}

	@Test
	public void handleResource() {
		TwitterResourceHandler twitterResourceHandler = Mockito.mock(TwitterResourceHandler.class);
		Deencapsulation.setField(twitterHandler, "twitterResourceHandler", twitterResourceHandler);

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