package ikube.integration.index.handler.internet;

import static org.junit.Assert.assertTrue;
import ikube.action.Index;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.index.handler.internet.IndexableInternetHandler;
import ikube.integration.AbstractIntegration;
import ikube.listener.ListenerManager;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ThreadUtilities;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;

import mockit.Deencapsulation;

import org.apache.lucene.index.IndexWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableInternetHandlerIntegration extends AbstractIntegration {

	private IDataBase					dataBase;
	private IndexContext<?>				indexContext;
	private IndexableInternet			indexableInternet;
	private IndexableInternetHandler	indexableInternetHandler;

	@Before
	public void before() {
		ApplicationContextManager.getBean(ListenerManager.class).removeListeners();
		indexContext = ApplicationContextManager.getBean("indexContext");
		indexableInternet = ApplicationContextManager.getBean("coldwell");
		indexableInternetHandler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
		dataBase = ApplicationContextManager.getBean(IDataBase.class);
		ApplicationContextManager.getBean(IClusterManager.class).startWorking(Index.class.getSimpleName(), indexContext.getName(),
				indexableInternet.getName());
		delete(dataBase, Url.class);
	}

	@After
	public void after() {
		ApplicationContextManager.getBean(ListenerManager.class).removeListeners();
	}

	@Test
	public void handle() throws Exception {
		String ip = InetAddress.getLocalHost().getHostAddress();
		IndexWriter indexWriter = IndexManager.openIndexWriter(realIndexContext, System.currentTimeMillis(), ip);
		indexContext.getIndex().setIndexWriter(indexWriter);
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
		// indexableInternetHandler.extractLinksFromContent(dataBase, indexableInternet, baseUrl, inputStream);
		Deencapsulation.invoke(indexableInternetHandler, "extractLinksFromContent", dataBase, indexableInternet, baseUrl, inputStream);

		baseUrl = new Url();
		baseUrl.setName("name");
		baseUrl.setUrl("http://www.hazelcast.com/product.jsp");
		indexableInternet.setUri(null);
		indexableInternet.setUrl(baseUrl.getUrl());
		indexableInternet.setBaseUrl(null);
		inputStream = new URL(baseUrl.getUrl()).openStream();

		// indexableInternetHandler.extractLinksFromContent(dataBase, indexableInternet, baseUrl, inputStream);
		Deencapsulation.invoke(indexableInternetHandler, "extractLinksFromContent", dataBase, indexableInternet, baseUrl, inputStream);
	}

}