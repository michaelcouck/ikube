package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.mock.IndexManagerMock;
import ikube.model.IndexableInternet;
import ikube.model.IndexableSvn;
import ikube.model.Url;
import ikube.toolkit.FileUtilities;
import junit.framework.Assert;
import mockit.Mockit;
import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.io.SVNRepository;

import java.io.File;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 07-06-2014
 */
@RunWith(MockitoJUnitRunner.class)
public class SvnResourceHandlerTest extends AbstractTest {

    private Document document;
    @org.mockito.Mock
    private SVNDirEntry dirEntry;
    @org.mockito.Mock
    private IndexableSvn indexableSvn;
    @org.mockito.Mock
    private SVNRepository repository;

    private SvnResourceHandler svnResourceHandler;

    @Before
    public void before() {
        document = new Document();
        svnResourceHandler = new SvnResourceHandler();
        Mockit.setUpMocks(IndexManagerMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(IndexManager.class);
    }

    @Test
    public void handleResource() throws Exception {
        when(dirEntry.getKind()).thenReturn(SVNNodeKind.FILE);
        when(dirEntry.getDate()).thenReturn(new Date());
        when(indexableSvn.getRepository()).thenReturn(repository);
        when(indexableSvn.getContent()).thenReturn(IConstants.CONTENT);
        svnResourceHandler.handleResource(indexContext, indexableSvn, document, dirEntry);
        Mockito.verify(indexableSvn, Mockito.atLeastOnce()).getContent();
    }

}