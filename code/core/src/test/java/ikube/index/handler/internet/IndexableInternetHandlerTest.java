package ikube.index.handler.internet;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.action.Reset;
import ikube.index.handler.internet.crawler.PageHandler;
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

	public IndexableInternetHandlerTest() {
		super(IndexableInternetHandlerTest.class);
	}

	@Before
	public void before() {
		PageHandler.OUT_SET.clear();
	}

	@After
	public void after() {
		PageHandler.OUT_SET.clear();
	}

	@Test
	public void handle() throws Exception {
		indexContext.getIndex().setIndexWriter(INDEX_WRITER);
		IndexableInternet indexableInternet = ApplicationContextManager.getBean("internet");
		IndexableInternetHandler indexableInternetHandler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
		ApplicationContextManager.getBean(Reset.class).execute(indexContext);
		List<Thread> threads = indexableInternetHandler.handle(indexContext, indexableInternet);

		ThreadUtilities.waitForThreads(threads);

		int totalUrlsCrawled = PageHandler.OUT_SET.size();
		logger.info("Urls crawled : " + totalUrlsCrawled);
		// Print everything in the database
		for (Url url : PageHandler.OUT_SET) {
			logger.info("Url : " + url);
		}
		assertTrue("Expected more than 10 and got : " + totalUrlsCrawled, totalUrlsCrawled > 10);
	}

}