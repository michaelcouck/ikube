package ikube.mock;

import ikube.index.IndexManager;
import ikube.model.IndexContext;

import java.io.File;

import mockit.Mock;
import mockit.MockClass;

import org.apache.lucene.index.IndexWriter;

/**
 * This mock is for the index manager that opens indexes on the file system, which we want to avoid.
 * 
 * @author Michael Couck
 * @since 20.03.11
 * @version 01.00
 */
@MockClass(realClass = IndexManager.class)
public class IndexManagerMock {

	public static IndexWriter INDEX_WRITER;
	private static File LATEST_INDEX_DIRECTORY;

	@Mock()
	public static synchronized IndexWriter openIndexWriter(final IndexContext<?> indexContext, final long time, final String ip) {
		return INDEX_WRITER;
	}

	public static void setIndexWriter(IndexWriter indexWriter) {
		IndexManagerMock.INDEX_WRITER = indexWriter;
	}

	@Mock()
	public static synchronized File getLatestIndexDirectory(final String baseIndexDirectoryPath) {
		return LATEST_INDEX_DIRECTORY;
	}

	public static void setLatestIndexDirectory(File latestIndexDirectory) {
		LATEST_INDEX_DIRECTORY = latestIndexDirectory;
	}

}
