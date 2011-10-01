package ikube.index.handler.internet;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.action.Index;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.listener.ListenerManager;
import ikube.listener.Scheduler;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ThreadUtilities;

import java.io.InputStream;
import java.net.URL;
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

	private IDataBase					dataBase;
	private IndexContext<?>				indexContext;
	private IndexableInternet			indexableInternet;
	private IndexableInternetHandler	indexableInternetHandler;

	public IndexableInternetHandlerTest() {
		super(IndexableInternetHandlerTest.class);
	}

	@Before
	public void before() {
		Scheduler.shutdown();
		ListenerManager.removeListeners();
		indexContext = ApplicationContextManager.getBean("ikube");
		indexableInternet = ApplicationContextManager.getBean("ikubeGoogleCode");
		indexableInternetHandler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
		dataBase = ApplicationContextManager.getBean(IDataBase.class);
		ApplicationContextManager.getBean(IClusterManager.class).startWorking(Index.class.getSimpleName(), indexContext.getName(),
				indexableInternet.getName());
		delete(dataBase, Url.class);
	}

	@After
	public void after() {
		Scheduler.shutdown();
		ListenerManager.removeListeners();
		// delete(dataBase, Url.class);
	}

	@Test
	public void handle() throws Exception {
		indexContext.getIndex().setIndexWriter(INDEX_WRITER);
		List<Thread> threads = indexableInternetHandler.handle(indexContext, indexableInternet);
		ThreadUtilities.waitForThreads(threads);
		int expectedAtLeast = 10;
		List<Url> urls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
		int totalUrlsCrawled = urls.size();
		logger.info("Urls crawled : " + totalUrlsCrawled);
		assertTrue("Expected more than " + expectedAtLeast + " and got : " + totalUrlsCrawled, totalUrlsCrawled > expectedAtLeast);
	}

	@Test
	public void extractLinksFromContent() throws Exception {
		Url baseUrl = new Url();
		baseUrl.setName("name");
		baseUrl.setUrl("http://code.google.com/p/ikube/");
		indexableInternet.setUri(null);
		indexableInternet.setUrl(baseUrl.getUrl());
		indexableInternet.setBaseUrl(null);
		InputStream inputStream = new URL(baseUrl.getUrl()).openStream();
		indexableInternetHandler.extractLinksFromContent(dataBase, indexableInternet, baseUrl, inputStream);

		baseUrl = new Url();
		baseUrl.setName("name");
		baseUrl.setUrl("http://www.hazelcast.com/product.jsp");
		indexableInternet.setUri(null);
		indexableInternet.setUrl(baseUrl.getUrl());
		indexableInternet.setBaseUrl(null);
		inputStream = new URL(baseUrl.getUrl()).openStream();
		indexableInternetHandler.extractLinksFromContent(dataBase, indexableInternet, baseUrl, inputStream);

		// TODO Mock the cache and check that there are some urls added to it
	}

}