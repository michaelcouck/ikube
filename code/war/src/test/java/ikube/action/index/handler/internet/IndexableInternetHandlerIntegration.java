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
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import org.apache.lucene.index.IndexWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
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
        indexContext = ApplicationContextManager.getBean("ikube");
        indexableInternet = ApplicationContextManager.getBean("ikube-internet");

        /*indexableInternet.setAnalyzed(Boolean.TRUE);
        indexableInternet.setOmitNorms(Boolean.TRUE);
        indexableInternet.setStored(Boolean.TRUE);
        indexableInternet.setVectored(Boolean.FALSE);
        indexableInternet.setTokenized(Boolean.TRUE);*/

        indexableInternetHandler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
        IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);

        clusterManager.startWorking(Index.class.getSimpleName(), indexContext.getName(), indexableInternet.getName());
        delete(ApplicationContextManager.getBean(IDataBase.class), Url.class);
    }

    @After
    public void after() {
        // FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    public void handleIndexableForked() throws Exception {
        indexContext.setStrategies(null);
        indexableInternet.setUrl("http://www.ikube.be/site/index.html");

        String ip = InetAddress.getLocalHost().getHostAddress();
        IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
        indexContext.setIndexWriters(indexWriter);

        ForkJoinTask<?> forkJoinTask = indexableInternetHandler.handleIndexableForked(indexContext, indexableInternet);
        ThreadUtilities.executeForkJoinTasks(indexContext.getName(), indexableInternet.getThreads(), forkJoinTask);
        ThreadUtilities.waitForFuture(forkJoinTask, Integer.MAX_VALUE);

        logger.info("Documents : " + indexWriter.numDocs());
        assertTrue("There must be some documents in the index : ", indexWriter.numDocs() > 10);

        IndexManager.closeIndexWriter(indexWriter);
    }

    @Test
    public void terminateIndexableHandler() throws Exception {
        indexableInternet.setUrl("http://www.microsoft.com");
        // Start a thread that will terminate the job
        new Thread(new Runnable() {
            public void run() {
                ThreadUtilities.sleep(60000);
                logger.info("Terminating the job : " + indexContext.getName());
                ThreadUtilities.cancelForkJoinPool(indexContext.getName());
            }
        }).start();

        String ip = InetAddress.getLocalHost().getHostAddress();
        IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
        indexContext.setIndexWriters(indexWriter);

        final ForkJoinTask<?> forkJoinTask = indexableInternetHandler.handleIndexableForked(indexContext, indexableInternet);

        // Start another thread to verify that the task completed
        new Thread(new Runnable() {
            public void run() {
                ThreadUtilities.sleep(120000);
                try {
                    assertTrue(forkJoinTask.isDone() || forkJoinTask.isCancelled());
                } finally {
                    ThreadUtilities.destroy();
                }
            }
        }).start();

        ThreadUtilities.executeForkJoinTasks(indexContext.getName(), indexableInternet.getThreads(), forkJoinTask);
        ThreadUtilities.waitForFuture(forkJoinTask, Long.MAX_VALUE);
        // If this test does not work then we will never get here
    }

}