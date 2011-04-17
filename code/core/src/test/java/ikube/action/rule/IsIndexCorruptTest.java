package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;

import mockit.Mockit;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.Before;
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
	public void evaluate() throws IOException {
		// Create an index
		File latestIndexDirectory = createIndex(INDEX_CONTEXT, "a little text for good will");
		File serverIndexDirectory = new File(latestIndexDirectory, IP);
		Directory directory = FSDirectory.open(serverIndexDirectory);
		// Lock the index
		Lock lock = getLock(directory, serverIndexDirectory);
		if (!lock.isLocked()) {
			logger.warn("Couldn't get lock on index : " + lock);
		}
		boolean isCorrupt = isIndexCorrupt.evaluate(INDEX_CONTEXT);
		// It should not be corrupt
		assertFalse(isCorrupt);

		// Unlock the index
		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);
		// Delete the segments files
		FileUtilities.deleteFiles(serverIndexDirectory, "segments");
		// The index should be corrupt
		isCorrupt = isIndexCorrupt.evaluate(INDEX_CONTEXT);
		assertTrue(isCorrupt);
	}

}