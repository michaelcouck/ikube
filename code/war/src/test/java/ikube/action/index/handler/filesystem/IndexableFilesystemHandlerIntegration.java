package ikube.action.index.handler.filesystem;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.action.Open;
import ikube.action.Optimizer;
import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.search.Search;
import ikube.search.SearchComplex;
import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;
import ikube.toolkit.UriUtilities;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ForkJoinTask;

import static ikube.action.index.IndexManager.openIndexWriter;
import static org.junit.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2012
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class IndexableFilesystemHandlerIntegration extends IntegrationTest {

    @Autowired
    @Qualifier("desktop")
    private IndexContext desktop;
    @Autowired
    @Qualifier("desktopFolder")
    private IndexableFileSystem desktopFolder;
    @Autowired
    private IndexableFileSystemHandler indexableFilesystemHandler;

    @Before
    public void before() {
        File resources = FILE.findDirectoryRecursively(new File("."), "resources");
        String dataIndexFolderPath = FILE.cleanFilePath(resources.getAbsolutePath());

        desktopFolder.setExcludedPattern(null);
        // This should be true for performance testing, however there is a problem with running this test
        // in Eclipse with the unpack to true, OpenJpa throws a stack over flow for some reason, I think because
        // the classes are not enhanced
        desktopFolder.setUnpackZips(Boolean.TRUE);
        desktopFolder.setPath(dataIndexFolderPath);
        desktop.setIndexDirectoryPath("./indexes");

        FILE.deleteFile(new File(desktop.getIndexDirectoryPath()));

        IndexWriter indexWriter = openIndexWriter(desktop, System.currentTimeMillis(), UriUtilities.getIp());
        desktop.setIndexWriters(indexWriter);
    }

    @After
    public void after() {
        FILE.deleteFile(new File(desktop.getIndexDirectoryPath()));
    }

    @Test
    public void handleIndexable() throws Exception {
        try {
            ForkJoinTask<?> forkJoinTask = indexableFilesystemHandler.handleIndexableForked(desktop, desktopFolder);
            THREAD.executeForkJoinTasks(desktop.getName(), desktopFolder.getThreads(), forkJoinTask);
            THREAD.waitForFuture(forkJoinTask, Long.MAX_VALUE);

            // Verify that there are some documents in the index
            assertNotNull("The index writer should still be available : ", desktop.getIndexWriters());
            assertEquals("There should only be one index writer : ", 1, desktop.getIndexWriters().length);
            for (final IndexWriter indexWriter : desktop.getIndexWriters()) {
                assertTrue("There must be some documents in the index : ", indexWriter.numDocs() > 0);
            }
        } finally {
            IndexManager.closeIndexWriters(desktop);
        }
        THREAD.sleep(15000);
        new Optimizer().execute(desktop);
        new Open().execute(desktop);
        THREAD.sleep(15000);

        // "Vivamus"
        search("TimeoutHandlerQ", "Ikokoon", "français", "consectetur");
        // TODO: These can't be found! Why?
        //"Hibernate", "Application", "Humble", "Autosuggested", "Reporting", "encrypted", "assumptions"
    }

    private void search(final String... searchStrings) throws Exception {
        IndexSearcher indexSearcher = desktop.getMultiSearcher();
        SearchComplex searchComplex = new SearchComplex(indexSearcher);

        // printIndex(indexSearcher.getIndexReader(), 10000);

        searchComplex.setFirstResult(0);
        searchComplex.setMaxResults(10);
        searchComplex.setFragment(Boolean.TRUE);
        searchComplex.setOccurrenceFields(IConstants.MUST);
        searchComplex.setSearchFields(IConstants.CONTENTS);
        searchComplex.setTypeFields(Search.TypeField.STRING.toString());

        for (final String searchString : searchStrings) {
            searchComplex.setSearchStrings(searchString);
            ArrayList<HashMap<String, String>> results = searchComplex.execute();
            if (results.size() <= 1) {
                logger.warn("Couldn't find : " + searchString);
                printResults(results);
            }
            assertTrue("Must be some results : " + results.size(), results.size() > 1);
        }
    }

}