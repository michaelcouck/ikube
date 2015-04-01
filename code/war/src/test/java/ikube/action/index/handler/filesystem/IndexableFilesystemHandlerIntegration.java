package ikube.action.index.handler.filesystem;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.action.Open;
import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
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
        String dataIndexFolderPath = FILE.cleanFilePath(new File(".").getAbsolutePath());
        IndexWriter indexWriter = IndexManager.openIndexWriter(desktop, System.currentTimeMillis(), UriUtilities.getIp());

        desktopFolder.setPath(dataIndexFolderPath);
        desktopFolder.setExcludedPattern(null);
        // This should be true for performance testing, however there is a problem with running this test
        // in Eclipse with the unpack to true, OpenJpa throws a stack over flow for some reason, I think because
        // the classes are not enhanced
        desktopFolder.setUnpackZips(Boolean.TRUE);
        desktop.setIndexWriters(indexWriter);
    }

    @After
    public void after() {
        FILE.deleteFile(new File(desktop.getIndexDirectoryPath()), 1);
    }

    @Test
    public void handleIndexable() throws Exception {
        try {
            THREAD.sleep(30000);
            ForkJoinTask<?> forkJoinTask = indexableFilesystemHandler.handleIndexableForked(desktop, desktopFolder);
            THREAD.executeForkJoinTasks(desktop.getName(), desktopFolder.getThreads(), forkJoinTask);
            THREAD.waitForFuture(forkJoinTask, Long.MAX_VALUE);

            // Verify that there are some documents in the index
            assertNotNull("The index writer should still be available : ", desktop.getIndexWriters());
            assertEquals("There should only be one index writer : ", 1, desktop.getIndexWriters().length);
            for (final IndexWriter indexWriter : desktop.getIndexWriters()) {
                assertTrue(indexWriter.numDocs() > 0);
            }

            search("AssignBusinessReference", "Interchange", "Business View", "Functionality", "Architecture", "COBAXT", "XMLReportQ");
        } finally {
            IndexManager.closeIndexWriters(desktop);
        }
    }

    private void search(final String... searchStrings) throws Exception {
        new Open().execute(desktop);
        IndexSearcher indexSearcher = desktop.getMultiSearcher();
        SearchComplex searchComplex = new SearchComplex(indexSearcher);

        searchComplex.setFirstResult(0);
        searchComplex.setMaxResults(10);
        searchComplex.setFragment(Boolean.TRUE);
        searchComplex.setOccurrenceFields("must");
        searchComplex.setSearchFields("contents");
        searchComplex.setTypeFields("string");

        outer: for (final String searchString : searchStrings) {
            searchComplex.setSearchStrings(searchString);
            ArrayList<HashMap<String, String>> results = searchComplex.execute();
            assertTrue("Must be some results : " + results.size(), results.size() > 2);
            for (final HashMap<String, String> result : results) {
                if ("docx.docx".equals(result.get(IConstants.NAME))) {
                    continue outer;
                }
            }
            assertTrue("Couldn't find the docx document in the results : ", Boolean.FALSE);
        }
    }

}