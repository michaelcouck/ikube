package ikube.scheduling.schedule;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.UriUtilities;
import mockit.Deencapsulation;
import mockit.Mockit;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21.06.13
 */
public class IndexCommitScheduleTest extends AbstractTest {

    /**
     * Class under test.
     */
    private IndexCommitSchedule indexCommitSchedule;

    @Before
    public void before() {
        indexCommitSchedule = new IndexCommitSchedule();
        Deencapsulation.setField(indexCommitSchedule, "monitorService", monitorService);
    }

    @After
    public void after() {
        Mockit.tearDownMocks();
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    public void run() throws Exception {
        IndexWriter[] indexWriters = null;
        try {
            indexWriters = IndexManager.openIndexWriterDelta(indexContext);

            when(indexContext.isDelta()).thenReturn(Boolean.TRUE);
            when(indexContext.getIndexWriters()).thenReturn(indexWriters);
            addDocuments(indexWriter, IConstants.CONTENTS, "Hello world");

            // There should be no segments file and the index doesn't exist yet
            File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
            File indexDirectory = new File(latestIndexDirectory, UriUtilities.getIp());
            Directory directory = FSDirectory.open(indexDirectory);
            assertFalse(DirectoryReader.indexExists(directory));

            indexCommitSchedule.run();
            // There should be a segments file and we should be able to open the reader
            assertTrue(DirectoryReader.indexExists(directory));
            indexReader = IndexReader.open(directory);
        } finally {
            if (indexWriters != null) {
                for (final IndexWriter indexWriter : indexWriters) {
                    IndexManager.closeIndexWriter(indexWriter);
                }
            }
        }

    }

}