package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.model.IndexableTweets;
import ikube.toolkit.PerformanceTester;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.social.twitter.api.StreamingOperations;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

import java.io.IOException;
import java.util.Arrays;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TwitterResourceProviderTest extends AbstractTest {

	@SuppressWarnings("UnusedDeclaration")
	@MockClass(realClass = TwitterTemplate.class)
	public static class TwitterTemplateMock {

		@Mock
		public void $init(
		  final String consumerKey,
		  final String consumerSecret,
		  final String accessToken,
		  final String accessTokenSecret) {
		}

		@Mock
		public StreamingOperations streamingOperations() {
			return mock(StreamingOperations.class);
		}
	}

	private TwitterResourceProvider twitterResourceProvider;

	private Tweet tweet;

	@Before
	public void before() throws IOException {
		tweet = mock(Tweet.class);
		Mockit.setUpMocks(TwitterTemplateMock.class);
		IndexableTweets indexableTweets = mock(IndexableTweets.class);
		when(indexableTweets.getParent()).thenReturn(indexContext);
		twitterResourceProvider = new TwitterResourceProvider(indexableTweets);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(TwitterTemplate.class);
	}

	@Test
	public void getResource() {
		twitterResourceProvider.setResources(Arrays.asList(tweet));
		Tweet returnTweet = twitterResourceProvider.getResource();
		assertNotNull(returnTweet);
		// Now we'll deplete the stream and see that we always get a tweet
		PerformanceTester.execute(new PerformanceTester.APerform() {
            public void execute() {
                twitterResourceProvider.setResources(Arrays.asList(tweet));
                Tweet tweet = twitterResourceProvider.getResource();
                assertNotNull(tweet);
            }
        }, "Depletion of the tweets ", 1000, true);
	}

}