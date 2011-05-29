package ikube.index.handler.internet;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.action.Reset;
import ikube.cluster.IClusterManager;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ThreadUtilities;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableInternetHandlerTest extends BaseTest {

	private IClusterManager clusterManager;

	public IndexableInternetHandlerTest() {
		super(IndexableInternetHandlerTest.class);
	}

	@Before
	public void before() {
		clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.clear(IConstants.URL);
		clusterManager.clear(IConstants.URL_DONE);
		clusterManager.clear(IConstants.URL_HASH);
	}

	@After
	public void after() {
		clusterManager.clear(IConstants.URL);
		clusterManager.clear(IConstants.URL_DONE);
		clusterManager.clear(IConstants.URL_HASH);
	}

	/**
	 * TODO Ignored while the internet is still not connected
	 */
	@Test
	public void handle() throws Exception {
		indexContext.getIndex().setIndexWriter(INDEX_WRITER);
		IndexableInternet indexableInternet = ApplicationContextManager.getBean("internet");
		IndexableInternetHandler indexableInternetHandler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
		ApplicationContextManager.getBean(Reset.class).execute(indexContext);
		List<Thread> threads = indexableInternetHandler.handle(indexContext, indexableInternet);

		ThreadUtilities.waitForThreads(threads);

		int totalUrlsCrawled = clusterManager.size(IConstants.URL_DONE);
		logger.info("Urls crawled : " + totalUrlsCrawled);
		// Print everything in the database
		for (Url url : clusterManager.get(Url.class, IConstants.URL_DONE, null, null, Integer.MAX_VALUE)) {
			logger.info("Url : " + url);
		}
		assertTrue("Expected more than 10 and got : " + totalUrlsCrawled, totalUrlsCrawled > 10);
	}

}