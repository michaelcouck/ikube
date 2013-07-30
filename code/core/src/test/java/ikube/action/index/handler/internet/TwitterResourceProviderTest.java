package ikube.action.index.handler.internet;

import static junit.framework.Assert.assertNotNull;
import ikube.AbstractTest;
import ikube.model.IndexableTweets;
import ikube.toolkit.PerformanceTester;

import java.io.IOException;

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

	@Test
	public void getResource() {
		Tweet tweet = twitterResourceProvider.getResource();
		assertNotNull(tweet);
		// Now we'll deplete the stream and see that we always get a tweet
		PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() {
				Tweet tweet = twitterResourceProvider.getResource();
				assertNotNull(tweet);
			}
		}, "Depletion of the tweets ", 1000, true);
	}

}