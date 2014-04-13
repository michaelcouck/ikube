package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexableTweets;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ObjectToolkit;
import ikube.toolkit.PerformanceTester;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.social.twitter.api.StreamingOperations;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

import java.io.File;
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
		FileUtilities.deleteFile(new File(IConstants.ANALYTICS_DIRECTORY, "tweets"));
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

	@Test
	public void persistResources() {
		int stackSize = 100;
		Deencapsulation.setField(TwitterResourceProvider.class, "STACK_SIZE", stackSize);
		Deencapsulation.setField(twitterResourceProvider, "persistTweets", Boolean.TRUE);
		Tweet[] tweets = new Tweet[stackSize * 2];
		for (int i = 0; i < tweets.length; i++) {
			Tweet tweet = (Tweet) ObjectToolkit.getObject(Tweet.class);
			ObjectToolkit.populateFields(tweet, Boolean.TRUE, 10);
			tweets[i] = tweet;
		}
		twitterResourceProvider.persistResources(tweets);
		File tweetFile = FileUtilities.findFileRecursively(new File("./indexes"), ".json");
		assertNotNull(tweetFile);
	}

}