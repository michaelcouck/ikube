package ikube.action.index.handler.internet;

import ikube.IntegrationTest;
import ikube.action.Index;
import ikube.action.index.IndexManager;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;
import org.apache.lucene.index.IndexWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.net.InetAddress;
import java.util.concurrent.ForkJoinTask;

import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class IndexableInternetHandlerIntegration extends IntegrationTest {

    @Autowired
    private IDataBase dataBase;
    @Autowired
    @Qualifier("ikube")
    private IndexContext indexContext;
    @Autowired
    private IClusterManager clusterManager;
    @Autowired
    @Qualifier("ikube-internet")
    private IndexableInternet indexableInternet;
    @Autowired
    private IndexableInternetHandler indexableInternetHandler;

    @Before
    public void before() {
        clusterManager.startWorking(Index.class.getSimpleName(), indexContext.getName(), indexableInternet.getName());
    }

    @After
    public void after() {
        delete(dataBase, Url.class);
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    public void handleIndexableForked() throws Exception {
        IndexWriter indexWriter = null;
        try {
            indexContext.setStrategies(null);
            indexableInternet.setUrl("http://localhost:9090/ikube/system/dash.html");

            String ip = InetAddress.getLocalHost().getHostAddress();
            indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
            indexContext.setIndexWriters(indexWriter);

            ForkJoinTask<?> forkJoinTask = indexableInternetHandler.handleIndexableForked(indexContext, indexableInternet);
            ThreadUtilities.executeForkJoinTasks(indexContext.getName(), indexableInternet.getThreads(), forkJoinTask);
            ThreadUtilities.waitForFuture(forkJoinTask, Integer.MAX_VALUE);

            logger.info("Documents : " + indexWriter.numDocs());
            assertTrue("There must be some documents in the index : ", indexWriter.numDocs() > 0);
        } finally {
            IndexManager.closeIndexWriter(indexWriter);
        }

    }

    @Test
    public void terminateIndexableHandler() throws Exception {
        indexableInternet.setUrl("http://www.microsoft.com");
        // Start a thread that will terminate the job
        new Thread(new Runnable() {
            public void run() {
                ThreadUtilities.sleep(15000);
                logger.info("Terminating the internet job : " + indexContext.getName());
                ThreadUtilities.cancelForkJoinPool(indexContext.getName());
            }
        }).start();

        String ip = UriUtilities.getIp();
        IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
        indexContext.setIndexWriters(indexWriter);

        final ForkJoinTask<?> forkJoinTask = indexableInternetHandler.handleIndexableForked(indexContext, indexableInternet);

        // Start another thread to verify that the task completed
        new Thread(new Runnable() {
            public void run() {
                ThreadUtilities.sleep(30000);
                try {
                    assertTrue(forkJoinTask.isDone() || forkJoinTask.isCancelled());
                } finally {
                    logger.info("Should destroy the thread pools here : " + indexContext.getName());
                    ThreadUtilities.destroy();
                }
            }
        }).start();

        logger.info("Starting the job : " + indexContext.getName());
        ThreadUtilities.executeForkJoinTasks(indexContext.getName(), indexableInternet.getThreads(), forkJoinTask);
        logger.info("Waiting for the job : " + indexContext.getName());
        ThreadUtilities.waitForFuture(forkJoinTask, 60 * 10);
        // If this test does not work then we will never get here
    }

}