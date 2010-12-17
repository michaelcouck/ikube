package ikube.index.handler.internet;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.action.Reset;
import ikube.cluster.IClusterManager;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ThreadUtilities;

import java.util.List;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableInternetHandlerTest extends BaseTest {

	@Test
	public void handle() throws Exception {
		indexContext.setIndexWriter(INDEX_WRITER);
		IndexableInternet indexableInternet = ApplicationContextManager.getBean(IndexableInternet.class);
		IndexableInternetHandler indexableInternetHandler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
		ApplicationContextManager.getBean(Reset.class).execute(indexContext);
		List<Thread> threads = indexableInternetHandler.handle(indexContext, indexableInternet);

		ThreadUtilities.waitForThreads(threads);

		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		int totalUrlsCrawled = clusterManager.size(Url.class);
		logger.info("Urls crawled : " + totalUrlsCrawled);
		assertTrue(totalUrlsCrawled > 40);
	}

}