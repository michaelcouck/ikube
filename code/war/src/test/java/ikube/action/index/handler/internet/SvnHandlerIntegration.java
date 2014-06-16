package ikube.action.index.handler.internet;

import ikube.IntegrationTest;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.IndexableSvn;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.ByteArrayOutputStream;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 14-06-2014
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class SvnHandlerIntegration extends IntegrationTest {

    @Autowired
    @Qualifier("ikube")
    private IndexContext indexContext   ;
    @Autowired
    @Qualifier("ikube-svn-google-code")
    private IndexableSvn indexableSvn;
    @Autowired
    private SvnHandler svnHandler;
    @Autowired
    private IClusterManager clusterManager;
    @Autowired
    private IDataBase dataBase;

    @Test
    public void handleIndexableForked() throws Exception {
        // TODO: Complete this test
        // svnHandler.handleIndexableForked(indexContext, indexableSvn);
        IndexableSvn indexableSvn = new IndexableSvn();
        indexableSvn.setPassword("michael.couck@gmail.com");
        indexableSvn.setUrl("https://ikube.googlecode.com/svn");
        // indexableSvn.setFilePath("/trunk");
        indexableSvn.setPassword("Eh6gw2cY4WA2");

        SVNURL svnurl = SVNURL.parseURIEncoded(indexableSvn.getUrl());
        SVNRepository repository = SVNRepositoryFactory.create(svnurl);
        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(indexableSvn.getUsername(), indexableSvn.getPassword());
        repository.setAuthenticationManager(authManager);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        repository.getFile("/trunk/docs/gui/ikube.story", -1, null, byteArrayOutputStream);
        logger.info(new String(byteArrayOutputStream.toByteArray()));
    }

    @Test
    public void terminateIndexableHandler() throws Exception {
        // TODO: Implement this test
    }

}