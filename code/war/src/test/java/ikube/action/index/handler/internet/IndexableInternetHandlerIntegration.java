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
import org.apache.commons.lang.StringUtils;
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
        // ikube-google-code, ikube-internet
        indexContext = ApplicationContextManager.getBean("ikube");
        indexableInternet = ApplicationContextManager.getBean("ikube-google-code");

        indexableInternetHandler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
        IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);

        clusterManager.startWorking(Index.class.getSimpleName(), indexContext.getName(), indexableInternet.getName());
        delete(ApplicationContextManager.getBean(IDataBase.class), Url.class);
    }

    @After
    public void after() {
        if (StringUtils.isNotEmpty(indexableInternet.getName())) {
            FileUtilities.deleteFile(new File("./" + indexableInternet.getName()));
        }
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    public void handleIndexableForked() throws Exception {
        String ip = InetAddress.getLocalHost().getHostAddress();
        IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
        indexContext.setIndexWriters(indexWriter);

        ForkJoinTask<?> forkJoinTask = indexableInternetHandler.handleIndexableForked(indexContext, indexableInternet);
        ThreadUtilities.executeForkJoinTasks(indexContext.getName(), indexableInternet.getThreads(), forkJoinTask);
        ThreadUtilities.waitForFuture(forkJoinTask, Integer.MAX_VALUE);
        logger.info("Documents : " + indexContext.getIndexWriters()[0].numDocs());
        assertTrue("There must be some documents in the index : ", indexContext.getIndexWriters()[0].numDocs() > 10);
    }

}