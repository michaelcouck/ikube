package ikube.action.index.handler.internet;

import ikube.IntegrationTest;
import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.IndexableSvn;
import ikube.scheduling.Scheduler;
import ikube.toolkit.THREAD;
import org.apache.lucene.index.IndexWriter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.net.InetAddress;
import java.util.concurrent.ForkJoinTask;

import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 14-06-2014
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class SvnHandlerIntegration extends IntegrationTest {

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private SvnHandler svnHandler;
    @Autowired
    @Qualifier("ikube")
    private IndexContext indexContext;
    @Autowired
    @Qualifier("ikube-svn-google-code")
    private IndexableSvn indexableSvn;

    @Before
    public void before() {
        scheduler.shutdown();
    }

    @Test
    public void handleIndexableForked() throws Exception {
        IndexWriter indexWriter = null;
        try {
            indexableSvn.setUrl("https://ikube-client.googlecode.com/svn");
            indexableSvn.setFilePath("/trunk");
            String ip = InetAddress.getLocalHost().getHostAddress();
            indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
            indexContext.setIndexWriters(indexWriter);

            ForkJoinTask forkJoinTask = svnHandler.handleIndexableForked(indexContext, indexableSvn);
            THREAD.executeForkJoinTasks(indexContext.getName(), indexableSvn.getThreads(), forkJoinTask);
            THREAD.waitForFuture(forkJoinTask, Integer.MAX_VALUE);

            LOGGER.info("Documents : " + indexWriter.numDocs());
            assertTrue("There must be some documents in the index : ", indexWriter.numDocs() > 0);
        } finally {
            IndexManager.closeIndexWriter(indexWriter);
        }
    }

    public void terminateIndexableHandler() throws Exception {
        // TODO: Implement this test
    }

    /**
     * Example to get a resource content, and the parameters required.
     */
    @SuppressWarnings("UnusedDeclaration")
    private void connectAndReadOneFile() {
        // SVNURL svnurl = SVNURL.parseURIEncoded(indexableSvn.getUrl());
        // SVNRepository repository = SVNRepositoryFactory.create(svnurl);
        // ISVNAuthenticationManager authManager = SVNWCUtil
        //        .createDefaultAuthenticationManager(indexableSvn.getUsername(), indexableSvn.getPassword());
        // repository.setAuthenticationManager(authManager);

        // ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // repository.getFile("/trunk/docs/gui/ikube.story", -1, null, byteArrayOutputStream);
        // logger.info(new String(byteArrayOutputStream.toByteArray()));
    }

}