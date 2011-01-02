package ikube.index.handler.internet;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.action.Reset;
import ikube.database.IDataBase;
import ikube.database.mem.DataBaseMem;
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

	@Before
	public void before() {
		IDataBase dataBase = ApplicationContextManager.getBean(DataBaseMem.class);
		delete(dataBase, Url.class);
	}

	@After
	public void after() {
		IDataBase dataBase = ApplicationContextManager.getBean(DataBaseMem.class);
		delete(dataBase, Url.class);
	}

	@Test
	public void handle() throws Exception {
		indexContext.getIndex().setIndexWriter(INDEX_WRITER);
		IndexableInternet indexableInternet = ApplicationContextManager.getBean(IndexableInternet.class);
		IndexableInternetHandler indexableInternetHandler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
		ApplicationContextManager.getBean(Reset.class).execute(indexContext);
		List<Thread> threads = indexableInternetHandler.handle(indexContext, indexableInternet);

		ThreadUtilities.waitForThreads(threads);

		IDataBase dataBase = ApplicationContextManager.getBean(DataBaseMem.class);
		List<Url> urls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
		int totalUrlsCrawled = urls.size();
		logger.info("Urls crawled : " + totalUrlsCrawled);
		assertTrue(totalUrlsCrawled > 40);

		// Print everything in the database
		for (Url url : urls) {
			logger.info("Url : " + url);
		}
	}

}