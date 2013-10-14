package ikube.action.index.handler.internet;

import static org.junit.Assert.assertTrue;
import ikube.IntegrationTest;
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
import java.util.concurrent.ForkJoinTask;

import mockit.Deencapsulation;

import org.apache.lucene.index.IndexWriter;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableInternetHandlerIntegration extends IntegrationTest {

	private String url = "http://www.hazelcast.com";
	private IndexContext<?> indexContext;
	private IndexableInternet indexableInternet;
	private IndexableInternetHandler indexableInternetHandler;

	@Before
	public void before() {
		indexContext = monitorService.getIndexContext("indexContext");
		indexableInternet = ApplicationContextManager.getBean("ikubeGoogleCode");
		indexableInternet.setUrl(url);
		indexableInternet.setBaseUrl(url);
		indexableInternet.setThreads(10);
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

		ForkJoinTask<?> forkJoinTask = indexableInternetHandler.handleIndexableForked(indexContext, indexableInternet);
		ThreadUtilities.executeForkJoinTasks(indexContext.getName(), indexableInternet.getThreads(), forkJoinTask);
		ThreadUtilities.sleep(3000);
		ThreadUtilities.cancellForkJoinPool(indexContext.getName());
		int expectedAtLeast = 1;
		logger.info("Num docs : " + indexContext.getIndexWriters()[0].numDocs());
		assertTrue("There must be some documents in the index : ", indexContext.getIndexWriters()[0].numDocs() >= expectedAtLeast);
	}

	@Test
	public void extractLinksFromContent() throws Exception {
		int expectedAtLeast = 3;
		InputStream inputStream = new URL(indexableInternet.getUrl()).openStream();
		List<Url> urls = Deencapsulation.invoke(indexableInternetHandler, "extractLinksFromContent", indexableInternet, inputStream);
		assertTrue("Expected more than " + expectedAtLeast + " and got : " + urls.size(), urls.size() > expectedAtLeast);
	}

}