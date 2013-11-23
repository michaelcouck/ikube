package ikube.action.index.handler.internet;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexableTweets;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.social.twitter.api.Tweet;

public class TwitterResourceProviderTest extends AbstractTest {

	private TwitterResourceProvider twitterResourceProvider;

	@Before
	public void before() throws IOException {
		IndexableTweets indexableTweets = new IndexableTweets();
		indexableTweets.setThreads(3);
		indexableTweets.setConsumerKey("Sohh43DylUwaXr7smSojBA");
		indexableTweets.setConsumerSecret("90xubtexbSwhHBbKXM62pF4QfJnz1NWVkpevwde3Qxo");
		indexableTweets.setToken("380355068-JIMLrQyZglGs4WLXo2UShCmXMAMjWeaiZ15ZJkrp");
		indexableTweets.setTokenSecret("OyhI9UyioglNWrhJnQQWY2ULmNtt9Azfl70z0l8jOPM");
		twitterResourceProvider = new TwitterResourceProvider(indexableTweets);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(IConstants.ANALYTICS_DIRECTORY, "tweets"));
	}

	@Test
	public void getResource() {
		final Tweet tweet = mock(Tweet.class);
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
		Tweet tweet = mock(Tweet.class);
		twitterResourceProvider.persistResources(Arrays.asList(tweet));
		File tweetFile = FileUtilities.findFileRecursively(new File(IConstants.ANALYTICS_DIRECTORY), ".json");
		assertNotNull(tweetFile);
	}

}