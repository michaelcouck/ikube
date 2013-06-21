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
		ThreadUtilities.initialize();

		Deencapsulation.setField(twitterHandler, "twitterResourceHandler", twitterResourceHandler);

		final IndexableTweets indexableTweets = populateFields(new IndexableTweets(), Boolean.TRUE, 5);
		indexableTweets.setThreads(3);
		indexableTweets.setConsumerKey("Sohh43DylUwaXr7smSojBA");
		indexableTweets.setConsumerSecret("90xubtexbSwhHBbKXM62pF4QfJnz1NWVkpevwde3Qxo");
		indexableTweets.setToken("380355068-JIMLrQyZglGs4WLXo2UShCmXMAMjWeaiZ15ZJkrp");
		indexableTweets.setTokenSecret("OyhI9UyioglNWrhJnQQWY2ULmNtt9Azfl70z0l8jOPM");

		ThreadUtilities.submit(null, new Runnable() {
			public void run() {
				ForkJoinTask<?> forkJoinTask;
				try {
					forkJoinTask = twitterHandler.handleIndexableForked(indexContext, indexableTweets);
					ForkJoinPool forkJoinPool = new ForkJoinPool(indexableTweets.getThreads());
					ThreadUtilities.addForkJoinPool(indexContext.getName(), forkJoinPool);
					forkJoinPool.invoke(forkJoinTask);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		ThreadUtilities.sleep(5000);
		ThreadUtilities.cancellForkJoinPool(indexContext.getName());
		ThreadUtilities.destroy();
	}

}