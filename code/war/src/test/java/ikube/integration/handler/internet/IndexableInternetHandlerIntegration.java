package ikube.integration.handler.internet;

import static org.junit.Assert.*;

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
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Future;

import mockit.Deencapsulation;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.lucene.index.IndexWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableInternetHandlerIntegration extends AbstractIntegration {

	private IDataBase dataBase;
	private IndexContext<?> indexContext;
	private IndexableInternet indexableInternet;
	private IndexableInternetHandler indexableInternetHandler;

	@Before
	public void before() {
		ApplicationContextManager.getBean(ListenerManager.class).removeListeners();
		indexContext = ApplicationContextManager.getBean("indexContext");
		indexContext.setIndexDirectoryPath("./indexes");
		indexableInternet = ApplicationContextManager.getBean("hazelcast");
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
		indexableInternet.setUrl("http://sum.agj1.post.bpgnet.net/wiki");
		indexableInternet.setExcludedPattern("download");
		// indexableInternet.setUserid("U365981");
		// indexableInternet.setLoginUrl(loginUrl);
		// indexableInternet.setPassword(password);
		List<Future<?>> threads = indexableInternetHandler.handle(indexContext, indexableInternet);
		ThreadUtilities.waitForFutures(threads, Integer.MAX_VALUE);
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
		Deencapsulation.invoke(indexableInternetHandler, "extractLinksFromContent", dataBase, indexableInternet, baseUrl, inputStream);
		// TODO Verify the links collected

		baseUrl = new Url();
		baseUrl.setName("name");
		baseUrl.setUrl("http://www.hazelcast.com/product.jsp");
		indexableInternet.setUri(null);
		indexableInternet.setUrl(baseUrl.getUrl());
		indexableInternet.setBaseUrl(null);
		inputStream = new URL(baseUrl.getUrl()).openStream();
		Deencapsulation.invoke(indexableInternetHandler, "extractLinksFromContent", dataBase, indexableInternet, baseUrl, inputStream);
		// TODO Verify the links collected
	}

	@Test
	@Ignore
	public void login() throws Exception {
		HttpClient httpClient = new HttpClient();
		// indexableInternetHandler.login(indexableInternet, httpClient);
		indexableInternet.setLoginUrl("http://localhost:9300/ikube/admin/login.html");
		indexableInternet.setUserid("guest");
		indexableInternet.setPassword("guest");
		Deencapsulation.invoke(indexableInternetHandler, "login", indexableInternet, httpClient);
		GetMethod get = new GetMethod("http://localhost:9300/ikube/admin/servers.html");
		httpClient.executeMethod(get);
		InputStream responseInputStream = get.getResponseBodyAsStream();
		String content = FileUtilities.getContents(responseInputStream, Integer.MAX_VALUE).toString();
		logger.info(content);
		assertTrue("Must contain the timestamp heading from the servers table : ", content.contains("Timestamp"));
	}

}