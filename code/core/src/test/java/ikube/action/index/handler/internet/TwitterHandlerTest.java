package ikube.action.index.handler.internet;

import static ikube.toolkit.ObjectToolkit.populateFields;
import ikube.AbstractTest;
import ikube.model.IndexableTweets;
import ikube.toolkit.ThreadUtilities;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class TwitterHandlerTest extends AbstractTest {

	private TwitterHandler twitterHandler;

	@Before
	public void before() {
		twitterHandler = new TwitterHandler();
	}

	@Test
	public void handleIndexable() throws Exception {
		ThreadUtilities.initialize();
		IndexableTweets indexableTweets = populateFields(new IndexableTweets(), Boolean.TRUE, 5);
		indexableTweets.setThreads(1);
		indexableTweets.setConsumerKey("Sohh43DylUwaXr7smSojBA");
		indexableTweets.setConsumerSecret("90xubtexbSwhHBbKXM62pF4QfJnz1NWVkpevwde3Qxo");
		indexableTweets.setToken("380355068-JIMLrQyZglGs4WLXo2UShCmXMAMjWeaiZ15ZJkrp");
		indexableTweets.setTokenSecret("OyhI9UyioglNWrhJnQQWY2ULmNtt9Azfl70z0l8jOPM");

		twitterHandler.handleIndexable(indexContext, indexableTweets);
		ThreadUtilities.sleep(5000);
	}

}