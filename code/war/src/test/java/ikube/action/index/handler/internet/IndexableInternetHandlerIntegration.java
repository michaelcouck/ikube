package ikube.action.index.handler.internet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import ikube.Integration;
import ikube.action.Index;
import ikube.action.index.IndexManager;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ThreadUtilities;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.Future;

import mockit.Deencapsulation;

import org.apache.lucene.index.IndexWriter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableInternetHandlerIntegration extends Integration {

	private IndexContext<?> indexContext;
	private IndexableInternet indexableInternet;
	private IndexableInternetHandler indexableInternetHandler;

	@Before
	public void before() {
		indexContext = monitorService.getIndexContext("indexContext");
		indexableInternet = ApplicationContextManager.getBean("ikubeGoogleCode");
		indexableInternetHandler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);

		clusterManager.startWorking(Index.class.getSimpleName(), indexContext.getName(), indexableInternet.getName());
		delete(ApplicationContextManager.getBean(IDataBase.class), Url.class);
	}

	@Test
	public void handle() throws Exception {
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
			indexContext.setIndexWriters(indexWriter);
			List<Future<?>> threads = indexableInternetHandler.handleIndexable(indexContext, indexableInternet);
			ThreadUtilities.waitForFutures(threads, Integer.MAX_VALUE);

			int expectedAtLeast = 1;
			assertTrue("There must be some documents in the index : ", indexContext.getIndexWriters()[0].numDocs() >= expectedAtLeast);
		} finally {
			ThreadUtilities.destroy(indexContext.getIndexName());
		}
	}

	@Test
	public void extractLinksFromContent() throws Exception {
		int expectedAtLeast = 3;

		InputStream inputStream = new URL(indexableInternet.getUrl()).openStream();
		Stack<Url> in = new Stack<Url>();
		Set<Long> out = new TreeSet<Long>();

		Deencapsulation.invoke(indexableInternetHandler, "extractLinksFromContent", indexableInternet, inputStream, in, out);
		assertTrue("Expected more than " + expectedAtLeast + " and got : " + in.size(), in.size() > expectedAtLeast);
	}

	@Test
	public void addDocument() {
		Url url = new Url();
		String title = "The title";
		String content = "<html><head><title>" + title + "</title></head></html>";
		url.setContentType("text/html");
		url.setRawContent(content.getBytes());
		IndexContext<?> indexContext = mock(IndexContext.class);
		IndexWriter indexWriter = Mockito.mock(IndexWriter.class);
		Mockito.when(indexContext.getIndexWriters()).thenReturn(new IndexWriter[] { indexWriter });
		indexableInternetHandler.handleResource(indexContext, indexableInternet, url);
		assertNotNull(url.getTitle());
		assertEquals(title, url.getTitle());
	}

	@Test
	@Ignore
	public void login() throws Exception {
		// HttpClient httpClient = new HttpClient();
		// indexableInternetHandler.login(indexableInternet, httpClient);
		// TODO First make sure that the security supports basic and digest so
		// the login method can in fact login. Second, login then try to access some other pages
		// and they should be returned of course
	}

}