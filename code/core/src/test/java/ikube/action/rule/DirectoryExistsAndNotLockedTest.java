package ikube.action.rule;

import ikube.AbstractTest;
import ikube.action.index.IndexManager;
import ikube.toolkit.UriUtilities;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
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
public class DirectoryExistsAndNotLockedTest extends AbstractTest {

	private DirectoryExistsAndNotLocked existsAndNotLocked;

	@Before
	public void before() {
		existsAndNotLocked = new DirectoryExistsAndNotLocked();
	}

//	@After
//	public void afterClass() {
//		Mockit.tearDownMocks();
//	}

	@Test
	public void evaluate() throws Exception {
		boolean existsAndNotLockedResult = existsAndNotLocked.evaluate(new File(indexContext.getIndexDirectoryPath()));
		assertFalse(existsAndNotLockedResult);

		createIndexFileSystem(indexContext, "Hello world");

		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
		File indexDirectory = new File(latestIndexDirectory, UriUtilities.getIp());
		existsAndNotLockedResult = existsAndNotLocked.evaluate(indexDirectory);
		assertTrue(existsAndNotLockedResult);

		Lock lock = null;
		try {
			lock = getLock(FSDirectory.open(indexDirectory), indexDirectory);
			existsAndNotLockedResult = existsAndNotLocked.evaluate(indexDirectory);
			assertFalse(existsAndNotLockedResult);
		} finally {
			lock.release();
		}
	}

}