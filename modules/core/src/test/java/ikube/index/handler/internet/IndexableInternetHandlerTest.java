package ikube.index.handler.internet;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.action.Reset;
import ikube.model.IndexableInternet;
import ikube.toolkit.ApplicationContextManager;

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
		indexContext.setIndexWriter(indexWriter);
		IndexableInternet indexableInternet = ApplicationContextManager.getBean(IndexableInternet.class);
		IndexableInternetHandler indexableInternetHandler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
		ApplicationContextManager.getBean(Reset.class).execute(indexContext);
		List<Thread> threads = indexableInternetHandler.handle(indexContext, indexableInternet);

		waitForThreads(threads);

		int totalUrlsCrawled = indexContext.getCache().getTotal(IConstants.URL);
		logger.info("Urls crawled : " + totalUrlsCrawled);
		assertTrue(totalUrlsCrawled > 40);
	}

	public static void main(String[] args) throws Exception {
		IndexableInternetHandlerTest indexableInternetHandlerTest = new IndexableInternetHandlerTest();
		indexableInternetHandlerTest.handle();
	}

}