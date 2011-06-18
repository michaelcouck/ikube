package ikube.index.handler.internet;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.action.Reset;
import ikube.database.IDataBase;
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

	private IndexableInternet indexableInternet;
	private IDataBase dataBase;

	public IndexableInternetHandlerTest() {
		super(IndexableInternetHandlerTest.class);
	}

	@Before
	public void before() {
		indexableInternet = ApplicationContextManager.getBean("internet");
		dataBase = ApplicationContextManager.getBean(IDataBase.class);
		dataBase.remove(Url.DELETE_ALL_URLS);
	}

	@After
	public void after() {
		dataBase.remove(Url.DELETE_ALL_URLS);
	}

	@Test
	public void handle() throws Exception {
		indexContext.getIndex().setIndexWriter(INDEX_WRITER);

		IndexableInternetHandler indexableInternetHandler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
		ApplicationContextManager.getBean(Reset.class).execute(indexContext);
		List<Thread> threads = indexableInternetHandler.handle(indexContext, indexableInternet);

		ThreadUtilities.waitForThreads(threads);

		List<Url> urls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
		int totalUrlsCrawled = urls.size();
		logger.info("Urls crawled : " + totalUrlsCrawled);
		// Print everything in the database
		for (Url url : urls) {
			logger.info("Url : " + url);
		}
		assertTrue("Expected more than 10 and got : " + totalUrlsCrawled, totalUrlsCrawled > 10);
	}

}