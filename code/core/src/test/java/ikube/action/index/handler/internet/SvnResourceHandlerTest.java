package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.mock.IndexManagerMock;
import ikube.model.Indexable;
import ikube.model.IndexableSvn;
import mockit.Mockit;
import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;

import java.util.Date;

import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 07-06-2014
 */
@RunWith(MockitoJUnitRunner.class)
public class SvnResourceHandlerTest extends AbstractTest {

    private Document document;

    @Mock
    private SVNURL svnurl;
    @Mock
    private SVNDirEntry dirEntry;
    @Mock
    private IndexableSvn indexableSvn;
    @Mock
    private SVNRepository repository;

    @Spy
    private SvnResourceHandler svnResourceHandler;

    @Before
    public void before() {
        document = new Document();
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
        when(dirEntry.getURL()).thenReturn(svnurl);
        when(dirEntry.getRepositoryRoot()).thenReturn(svnurl);
        when(dirEntry.getAuthor()).thenReturn("author");
        when(dirEntry.getCommitMessage()).thenReturn("message");
        when(dirEntry.getDate()).thenReturn(new Date());

        when(indexableSvn.getName()).thenReturn(IConstants.NAME);
        when(indexableSvn.getContent()).thenReturn(IConstants.CONTENT);
        when(indexableSvn.getRawContent()).thenReturn(IConstants.CONTENT.getBytes());
        when(indexableSvn.getMaxReadLength()).thenReturn(Long.MAX_VALUE);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return repository;
            }
        }).when(svnResourceHandler).getSvnRepository(any(Indexable.class), any(String.class));

        svnResourceHandler.handleResource(indexContext, indexableSvn, document, dirEntry);

        verify(indexableSvn, atLeastOnce()).getContent();
        verify(indexableSvn, times(1)).setContent(anyString());
    }

}