package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.model.IndexableSvn;
import ikube.toolkit.THREAD;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 07-06-2014
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("UnusedDeclaration")
public class SvnResourceProviderTest extends AbstractTest {

    @MockClass(realClass = SVNURL.class)
    public static class SVNURLMock {
        @Mock
        public static SVNURL parseURIEncoded(final String url) throws SVNException {
            return null;
        }
    }

    @MockClass(realClass = SVNRepositoryFactory.class)
    public static class SVNRepositoryFactoryMock {
        private static SVNRepository repository;

        @Mock
        public static SVNRepository create(final SVNURL url) throws SVNException {
            if (repository == null) {
                repository = Mockito.mock(SVNRepository.class);
            }
            return repository;
        }
    }

    @MockClass(realClass = SVNWCUtil.class)
    public static class SVNWCUtilMock {
        @Mock
        public static ISVNAuthenticationManager createDefaultAuthenticationManager(final String userName, final String password) {
            return Mockito.mock(ISVNAuthenticationManager.class);
        }
    }

    @MockClass(realClass = SVNRepository.class)
    public static class SVNRepositoryMock {
        @Mock
        public static SVNProperties wrap(final Map map) {
            return null;
        }
    }

    private SVNDirEntry dirEntry;
    @org.mockito.Mock
    private IndexableSvn indexableSvn;
    @org.mockito.Mock
    private SVNRepository repository;
    /**
     * Class under test.
     */
    private SvnResourceProvider svnResourceProvider;

    @Before
    public void before() throws Exception {
        Mockit.setUpMocks(SVNURLMock.class, SVNRepositoryFactoryMock.class, SVNWCUtilMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(SVNURLMock.class, SVNRepositoryFactoryMock.class, SVNWCUtilMock.class);
    }

    @Test
    public void init() throws Exception {
        svnResourceProvider = new SvnResourceProvider(indexableSvn);
        verify(indexableSvn, atLeastOnce()).getFilePath();
    }

    @Test
    public void getResource() throws Exception {
        svnResourceProvider = new SvnResourceProvider(indexableSvn);
        Stack<SVNDirEntry> svnDirEntries = Deencapsulation.getField(svnResourceProvider, "svnDirEntries");
        SVNDirEntry svnDirEntry = mock(SVNDirEntry.class);
        svnDirEntries.push(svnDirEntry);
        SVNDirEntry svnDirEntryCache = svnResourceProvider.getResource();
        assertEquals(svnDirEntry, svnDirEntryCache);

        THREAD.initialize();
        svnDirEntries.push(svnDirEntry);
        Future future = THREAD.submit(this.getClass().getSimpleName(), new Runnable() {
            public void run() {
                SVNDirEntry svnDirEntryCache = svnResourceProvider.getResource();
                assertNotNull(svnDirEntryCache);
            }
        });
        THREAD.waitForFuture(future, 3000);
    }

    @Test
    public void walkRepository() throws Exception {
        svnResourceProvider = new SvnResourceProvider(indexableSvn);
        SVNDirEntry svnDirEntry = mock(SVNDirEntry.class);
        when(svnDirEntry.getKind()).thenReturn(SVNNodeKind.FILE);
        SVNDirEntry svnDirEntryFolder = mock(SVNDirEntry.class);
        when(svnDirEntryFolder.getKind()).thenReturn(SVNNodeKind.DIR);

        Collection<SVNDirEntry> svnDirEntriesLevelOne = Arrays.asList(svnDirEntry, svnDirEntryFolder);
        Collection<SVNDirEntry> svnDirEntriesLevelTwo = Arrays.asList(svnDirEntry);
        Collection<SVNDirEntry> svnDirEntriesLevelThree = Arrays.asList();

        when(repository.getDir(anyString(), anyLong(), any(SVNProperties.class), anyCollection()))
                .thenReturn(svnDirEntriesLevelOne, svnDirEntriesLevelTwo, svnDirEntriesLevelThree);

        svnResourceProvider.walkRepository(repository, null);

        Stack<SVNDirEntry> svnDirEntries = Deencapsulation.getField(svnResourceProvider, "svnDirEntries");
        assertEquals("There should be two entries in the list : ", 2, svnDirEntries.size());
        assertEquals("And both of them should be the Mockito defined one : ", svnDirEntry, svnDirEntries.pop());
        assertEquals("And both of them should be the Mockito defined one : ", svnDirEntry, svnDirEntries.pop());
    }

}