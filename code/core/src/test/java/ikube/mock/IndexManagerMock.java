package ikube.mock;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import mockit.Mock;
import mockit.MockClass;
import org.apache.lucene.index.IndexWriter;
import org.mockito.Mockito;

import java.io.File;

/**
 * This mock is for the index manager that opens indexes on the file system, which we want to avoid.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 20-03-2011
 */
@MockClass(realClass = IndexManager.class)
public class IndexManagerMock {

    public static IndexWriter INDEX_WRITER;
    private static File LATEST_INDEX_DIRECTORY;

    @Mock()
    public static synchronized IndexWriter openIndexWriter(final IndexContext indexContext, final long time, final String ip) {
        if (INDEX_WRITER == null) {
            return Mockito.mock(IndexWriter.class);
        }
        return INDEX_WRITER;
    }

    @Mock()
    public static synchronized File getLatestIndexDirectory(final String baseIndexDirectoryPath) {
        if (LATEST_INDEX_DIRECTORY == null) {
            return Mockito.mock(File.class);
        }
        return LATEST_INDEX_DIRECTORY;
    }

    @Mock()
    public static synchronized IndexWriter[] openIndexWriterDelta(final IndexContext indexContext) throws Exception {
        return new IndexWriter[]{INDEX_WRITER};
    }

    public static void setIndexWriter(final IndexWriter indexWriter) {
        INDEX_WRITER = indexWriter;
    }

    public static void setLatestIndexDirectory(final File latestIndexDirectory) {
        LATEST_INDEX_DIRECTORY = latestIndexDirectory;
    }

}