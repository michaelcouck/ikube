package ikube.index.handler.internet;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.action.Reset;
import ikube.database.IDataBase;
import ikube.model.IndexableInternet;
import ikube.model.Url;
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
	public void visit() throws Exception {
		indexContext.setIndexWriter(indexWriter);
		IndexableInternet indexableInternet = ApplicationContextManager.getBean(IndexableInternet.class);
		IndexableInternetHandler indexableInternetHandler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
		ApplicationContextManager.getBean(Reset.class).execute(indexContext);
		List<Thread> threads = indexableInternetHandler.handle(indexContext, indexableInternet);

		waitForThreads(threads);

		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		List<Url> urls = dataBase.find(Url.class, Integer.MIN_VALUE, Integer.MAX_VALUE);
		logger.info("Urls crawled : " + urls);
		assertTrue(urls.size() > 40);
	}

}