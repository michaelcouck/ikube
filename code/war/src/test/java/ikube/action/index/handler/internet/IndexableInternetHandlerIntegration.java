package ikube.action.index.handler.internet;

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
import org.apache.lucene.index.IndexWriter;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.util.concurrent.ForkJoinTask;

import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public class IndexableInternetHandlerIntegration extends IntegrationTest {

    private IndexContext<?> indexContext;
    private IndexableInternet indexableInternet;
    private IndexableInternetHandler indexableInternetHandler;

    @Before
    public void before() {
        String url = "http://www.hazelcast.com";
        indexContext = monitorService.getIndexContext("indexContext");
        indexableInternet = ApplicationContextManager.getBean("ikubeGoogleCode");
        indexableInternet.setUrl(url);
        indexableInternet.setBaseUrl(url);
        indexableInternet.setThreads(10);
        indexableInternet.setExcludedPattern("some-pattern");
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
        ThreadUtilities.sleep(5000);
        ThreadUtilities.cancellForkJoinPool(indexContext.getName());
        assertTrue("There must be some documents in the index : ", indexContext.getIndexWriters()[0].numDocs() > 0);
    }

}