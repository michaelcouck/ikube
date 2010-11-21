package ikube.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class DeleteTest extends BaseActionTest {

	private Delete delete = new Delete();

	@Test
	public void execute() throws Exception {
		String indexDirectoryPath = indexContext.getIndexDirectoryPath();
		indexContext.setIndexDirectoryPath("./somethingDifferent");

		File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		FileUtilities.deleteFile(baseIndexDirectory, 1);
		assertFalse(baseIndexDirectory.exists());
		baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		assertTrue(baseIndexDirectory.exists());

		// No indexes so far, nothing to delete
		boolean deleted = delete.execute(indexContext);
		assertFalse(deleted);
		/*******************************************/

		String contextIndexDirectoryPath = getContextIndexDirectoryPath(indexContext);
		File contextIndexDirectory = FileUtilities.getFile(contextIndexDirectoryPath, Boolean.TRUE);
		assertTrue(contextIndexDirectory.exists());

		// Only one directory so nothing to delete
		deleted = delete.execute(indexContext);
		assertFalse(deleted);
		assertEquals(1, baseIndexDirectory.listFiles().length);
		/**************************************************/

		contextIndexDirectoryPath = getContextIndexDirectoryPath(indexContext);
		File anotherContextIndexDirectory = FileUtilities.getFile(contextIndexDirectoryPath, Boolean.TRUE);
		assertEquals(2, baseIndexDirectory.listFiles().length);
		// Two directories so one should be gone
		deleted = delete.execute(indexContext);
		assertTrue(deleted);
		assertEquals(1, baseIndexDirectory.listFiles().length);

		/****************************/
		contextIndexDirectoryPath = getContextIndexDirectoryPath(indexContext);
		File andAnotherContextIndexDirectory = FileUtilities.getFile(contextIndexDirectoryPath, Boolean.TRUE);
		assertEquals(2, baseIndexDirectory.listFiles().length);

		File indexDirectory = createIndex(andAnotherContextIndexDirectory);
		Directory directory = FSDirectory.open(indexDirectory);
		Lock lock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
		lock.obtain(1000);
		assertTrue(IndexWriter.isLocked(directory));

		// Two directories, one locked there should be two left
		deleted = delete.execute(indexContext);
		assertTrue(deleted);
		assertEquals(2, baseIndexDirectory.listFiles().length);

		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);

		/*************************************/
		contextIndexDirectoryPath = getContextIndexDirectoryPath(indexContext);
		File andYetAnotherContextIndexDirectory = FileUtilities.getFile(contextIndexDirectoryPath, Boolean.TRUE);
		assertEquals(3, baseIndexDirectory.listFiles().length);

		indexDirectory = createIndex(andYetAnotherContextIndexDirectory);
		directory = FSDirectory.open(indexDirectory);
		lock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
		lock.obtain(1000);
		assertTrue(IndexWriter.isLocked(directory));

		// Three directories, one locked, one should be gone
		deleted = delete.execute(indexContext);
		assertTrue(deleted);
		assertEquals(2, baseIndexDirectory.listFiles().length);

		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);

		indexContext.setIndexDirectoryPath(indexDirectoryPath);

		FileUtilities.deleteFile(andYetAnotherContextIndexDirectory, 1);
		FileUtilities.deleteFile(andAnotherContextIndexDirectory, 1);
		FileUtilities.deleteFile(anotherContextIndexDirectory, 1);
		FileUtilities.deleteFile(contextIndexDirectory, 1);
		FileUtilities.deleteFile(baseIndexDirectory, 1);

		assertFalse(baseIndexDirectory.exists());
		assertFalse(contextIndexDirectory.exists());
		assertFalse(anotherContextIndexDirectory.exists());
		assertFalse(andAnotherContextIndexDirectory.exists());
		assertFalse(andYetAnotherContextIndexDirectory.exists());
	}

}