package ikube.action.index.handler.internet;

import static ikube.toolkit.ObjectToolkit.populateFields;
import ikube.AbstractTest;
import ikube.model.IndexableTweets;
import ikube.toolkit.ThreadUtilities;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class TwitterHandlerTest extends AbstractTest {

	private TwitterHandler twitterHandler;
	@Cascading
	private TwitterResourceHandler twitterResourceHandler;

	@Before
	public void before() throws Exception {
		Mockit.setUpMocks();
		twitterHandler = new TwitterHandler();
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void handleIndexable() throws Exception {
		Deencapsulation.setField(twitterHandler, "twitterResourceHandler", twitterResourceHandler);

		IndexableTweets indexableTweets = populateFields(new IndexableTweets(), Boolean.TRUE, 5);
		indexableTweets.setThreads(3);
		indexableTweets.setConsumerKey("Sohh43DylUwaXr7smSojBA");
		indexableTweets.setConsumerSecret("90xubtexbSwhHBbKXM62pF4QfJnz1NWVkpevwde3Qxo");
		indexableTweets.setToken("380355068-JIMLrQyZglGs4WLXo2UShCmXMAMjWeaiZ15ZJkrp");
		indexableTweets.setTokenSecret("OyhI9UyioglNWrhJnQQWY2ULmNtt9Azfl70z0l8jOPM");

		final ForkJoinTask<?> forkJoinTask = twitterHandler.handleIndexableForked(indexContext, indexableTweets);
		final ForkJoinPool forkJoinPool = new ForkJoinPool(indexableTweets.getThreads());
		ThreadUtilities.addForkJoinPool(indexContext.getName(), forkJoinPool);

		ThreadUtilities.submit(null, new Runnable() {
			public void run() {
				try {
					forkJoinPool.invoke(forkJoinTask);
				} catch (Exception e) {
					logger.error(null, e);
				}
			}
		});

		ThreadUtilities.sleep(3000);
		ThreadUtilities.cancellForkJoinPool(indexContext.getName());
	}

}