package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.mock.IndexManagerMock;
import ikube.model.IndexableSvn;
import mockit.Mockit;
import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.io.SVNRepository;

import java.util.Date;

import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 07-06-2014
 */
@RunWith(MockitoJUnitRunner.class)
public class SvnResourceProviderTest extends AbstractTest {

    private Document document;
    @org.mockito.Mock
    private SVNDirEntry dirEntry;
    @org.mockito.Mock
    private IndexableSvn indexableSvn;
    @org.mockito.Mock
    private SVNRepository repository;

    private SvnResourceProvider svnResourceProvider;

    @Before
    public void before() {
        document = new Document();
    }

    @After
    public void after() {
    }

    @Test
    @Ignore
    public void init() throws Exception {
        svnResourceProvider = new SvnResourceProvider(indexableSvn);
    }

}