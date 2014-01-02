package ikube.action.rule;

import ikube.AbstractTest;
import ikube.action.index.IndexManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.UriUtilities;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29.03.2011
 */
public class DirectoryExistsAndIsLockedTest extends AbstractTest {

	private DirectoryExistsAndIsLocked existsAndIsLocked;

	@Before
	public void before() {
		existsAndIsLocked = new DirectoryExistsAndIsLocked();
	}

	@After
	public void after() {
		// Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
	}

	@Test
	public void evaluate() throws Exception {
		boolean existsAndIsLockedResult = existsAndIsLocked.evaluate(new File(indexContext.getIndexDirectoryPath()));
		assertFalse(existsAndIsLockedResult);

		createIndexFileSystem(indexContext, "Hello world");

		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
		File indexDirectory = new File(latestIndexDirectory, UriUtilities.getIp());
		existsAndIsLockedResult = existsAndIsLocked.evaluate(indexDirectory);
		assertFalse(existsAndIsLockedResult);

		Lock lock = null;
		try {
			lock = getLock(FSDirectory.open(indexDirectory), indexDirectory);
			existsAndIsLockedResult = existsAndIsLocked.evaluate(indexDirectory);
			assertTrue(existsAndIsLockedResult);
		} finally {
			lock.release();
		}

	}

}