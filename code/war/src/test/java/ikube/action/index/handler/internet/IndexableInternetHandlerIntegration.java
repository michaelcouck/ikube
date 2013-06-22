package ikube.action.index.handler.internet;

import static org.junit.Assert.assertTrue;
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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import mockit.Deencapsulation;

import org.apache.lucene.index.IndexWriter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
		String ip = InetAddress.getLocalHost().getHostAddress();
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
		indexContext.setIndexWriters(indexWriter);

		ThreadUtilities.submit(null, new Runnable() {
			public void run() {
				ForkJoinTask<?> forkJoinTask;
				try {
					forkJoinTask = indexableInternetHandler.handleIndexableForked(indexContext, indexableInternet);
					ForkJoinPool forkJoinPool = new ForkJoinPool(indexableInternet.getThreads());
					ThreadUtilities.addForkJoinPool(indexContext.getName(), forkJoinPool);
					forkJoinPool.invoke(forkJoinTask);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		ThreadUtilities.sleep(3000);
		ThreadUtilities.cancellForkJoinPool(indexContext.getName());

		int expectedAtLeast = 1;
		assertTrue("There must be some documents in the index : ", indexContext.getIndexWriters()[0].numDocs() >= expectedAtLeast);
	}

	@Test
	public void extractLinksFromContent() throws Exception {
		int expectedAtLeast = 3;

		InputStream inputStream = new URL(indexableInternet.getUrl()).openStream();

		List<Url> urls = Deencapsulation.invoke(indexableInternetHandler, "extractLinksFromContent", indexableInternet, inputStream);
		assertTrue("Expected more than " + expectedAtLeast + " and got : " + urls.size(), urls.size() > expectedAtLeast);
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