package ikube.action.rule;

import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29.03.2011
 */
public class IsIndexCorruptTest extends AbstractTest {

	/**
	 * Class under test.
	 */
	private IsIndexCorrupt isIndexCorrupt;

	@Before
	public void before() {
		// Mockit.tearDownMocks();
		isIndexCorrupt = new IsIndexCorrupt();
	}

	@Test
	public void evaluate() throws IOException {
		// Create an index
		File latestIndexDirectory = createIndexFileSystem(indexContext, "a little text for good will");
		Directory directory = FSDirectory.open(latestIndexDirectory);
		// Lock the index
		Lock lock = getLock(directory, latestIndexDirectory);
		if (!lock.isLocked()) {
			logger.warn("Couldn't get lock on index : " + lock);
		}
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