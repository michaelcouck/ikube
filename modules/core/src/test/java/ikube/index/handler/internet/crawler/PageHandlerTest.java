package ikube.index.handler.internet.crawler;

import ikube.BaseTest;
import ikube.database.IDataBase;
import ikube.database.mem.DataBaseMem;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PageHandlerTest extends BaseTest {

	private IndexableInternet indexableInternet = ApplicationContextManager.getBean(IndexableInternet.class);
	private IDataBase dataBase = ApplicationContextManager.getBean(DataBaseMem.class);

	@Before
	public void before() {
		indexContext.getIndex().setIndexWriter(INDEX_WRITER);
		delete(dataBase, Url.class);
	}

	@After
	public void after() {
		indexContext.getIndex().setIndexWriter(null);
		delete(dataBase, Url.class);
	}

	@Test
	public void run() throws Exception {
		Url url = new Url();
		url.setId(HashUtilities.hash(indexableInternet.getUrl()));
		url.setUrl(indexableInternet.getUrl());

		// dataBase.persist(url);
		PageHandler pageHandler = new PageHandler(new ArrayList<Thread>());
		pageHandler.setIndexContext(indexContext);
		pageHandler.setIndexableInternet(indexableInternet);
		PageHandler.IN.put(url.getId(), url);
		Thread thread = new Thread(pageHandler);
		thread.start();
		thread.join();
		// Verify that there are urls in the database, that they are all indexed and there are no duplicates
		// List<Url> urls = dataBase.find(Url.class, 0, Byte.MAX_VALUE);
		// assertTrue(urls.size() > 0);
		// for (Url dbUrl : urls) {
		// assertTrue(dbUrl.isIndexed());
		// }
	}

}