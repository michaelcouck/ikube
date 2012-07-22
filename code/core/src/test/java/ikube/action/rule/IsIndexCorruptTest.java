package ikube.action.rule;

import static org.mockito.Mockito.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;

import mockit.Mockit;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 29.03.2011
 * @version 01.00
 */
public class IsIndexCorruptTest extends ATest {

	public IsIndexCorruptTest() {
		super(IsIndexCorruptTest.class);
	}

	/** Class under test. */
	private IsIndexCorrupt isIndexCorrupt;

	@Before
	public void before() {
		Mockit.tearDownMocks();
		isIndexCorrupt = new IsIndexCorrupt();
	}

	@Test
	@Ignore
	public void adHoc() throws Exception {
		IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(
				"/media/nas/xfs/indexes/desktop/1342991361949/192.168.1.14.8001")));
		indexReader.close();
	}

	@Test
	public void evaluate() throws IOException {
		// Create an index
		File latestIndexDirectory = createIndex(indexContext, "a little text for good will");
		Directory directory = FSDirectory.open(latestIndexDirectory);
		// Lock the index
		Lock lock = getLock(directory, latestIndexDirectory);
		if (!lock.isLocked()) {
			logger.warn("Couldn't get lock on index : " + lock);
		}
		// when(indexContext.getIndexDirectoryPath()).thenReturn("/media/nas/xfs/indexes");
		// when(indexContext.getIndexName()).thenReturn("desktop");
		boolean isCorrupt = isIndexCorrupt.evaluate(indexContext);
		// It should not be corrupt
		assertFalse(isCorrupt);

		// Unlock the index
		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);
		// Delete the segments files
		FileUtilities.deleteFiles(latestIndexDirectory, "segments");
		// The index should be corrupt
		isCorrupt = isIndexCorrupt.evaluate(indexContext);
		assertTrue(isCorrupt);
	}

}