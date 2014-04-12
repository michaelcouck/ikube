package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.model.IndexableTweets;
import ikube.toolkit.ObjectToolkit;
import ikube.toolkit.PerformanceTester;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TwitterProfile;

import java.io.IOException;
import java.util.concurrent.ForkJoinTask;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 24-04-2013
 */
public class TwitterHandlerTest extends AbstractTest {

	@MockClass(realClass = TwitterResourceProvider.class)
	public static class TwitterResourceProviderMock {

		@Mock
		@SuppressWarnings({ "unused" })
		public void $init(final IndexableTweets indexableTweets) throws IOException {
		}

		@Mock
		public Tweet getResource() {
			return mock(Tweet.class);
		}
	}

	private TwitterHandler twitterHandler;
	private IndexableTweets indexableTweets;

	@Before
	public void before() throws Exception {
		Mockit.setUpMocks(TwitterResourceProviderMock.class);

		twitterHandler = new TwitterHandler();
		indexableTweets = mock(IndexableTweets.class);
		TwitterResourceHandler twitterResourceHandler = mock(TwitterResourceHandler.class);

		when(indexableTweets.getThreads()).thenReturn(0);

		Deencapsulation.setField(twitterHandler, "twitterResourceHandler", twitterResourceHandler);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(TwitterResourceProviderMock.class);
	}

	@Test
	public void handleIndexable() throws Exception {
		ForkJoinTask<?> forkJoinTask = twitterHandler.handleIndexableForked(indexContext, indexableTweets);
		assertNotNull(forkJoinTask);
	}

	@Test
	public void handleResource() {
		final Tweet tweet = (Tweet) ObjectToolkit.getObject(Tweet.class);
		ObjectToolkit.populateFields(tweet, Boolean.TRUE, 10);
		TwitterProfile twitterProfile = (TwitterProfile) ObjectToolkit.getObject(TwitterProfile.class);
		ObjectToolkit.populateFields(twitterProfile, Boolean.TRUE, 10);
		tweet.setUser(twitterProfile);

		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				twitterHandler.handleResource(indexContext, indexableTweets, tweet);
			}
		}, "Twitter handler performance : ", 1000, Boolean.TRUE);
		assertTrue(executionsPerSecond > 1000);
	}

}