package ikube.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;

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
public class DeleteTest extends BaseTest {

	private transient final Delete delete = new Delete();

	@Test
	public void execute() throws IOException {
		String indexDirectoryPath = indexContext.getIndexDirectoryPath();
		indexContext.setIndexDirectoryPath("./deleteTest");
		File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		FileUtilities.deleteFile(baseIndexDirectory, 1);
		baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		assertTrue("We should start with no directories : ", baseIndexDirectory.exists());

		// No indexes so far, nothing to delete
		boolean deleted = delete.execute(indexContext);
		assertFalse("The index should have been deleted : ", deleted);
		/*******************************************/

		String serverIndexDirectoryPath = getServerIndexDirectoryPath(indexContext);
		File serverIndexDirectory = FileUtilities.getFile(serverIndexDirectoryPath, Boolean.TRUE);
		createIndex(serverIndexDirectory);
		assertTrue("The index should have been deleted : ", serverIndexDirectory.exists());

		// Only one directory so nothing to delete
		deleted = delete.execute(indexContext);
		assertFalse("The index should not have been deleted : ", deleted);
		assertEquals("There should be only one index : ", 1, serverIndexDirectory.getParentFile().listFiles().length);
		/**************************************************/

		String anotherServerIndexDirectoryPath = getServerIndexDirectoryPath(indexContext);
		File anotherServerIndexDirectory = FileUtilities.getFile(anotherServerIndexDirectoryPath, Boolean.TRUE);
		createIndex(anotherServerIndexDirectory);
		assertEquals(2, anotherServerIndexDirectory.getParentFile().getParentFile().listFiles().length);
		// Two directories so one should be gone
		deleted = delete.execute(indexContext);
		assertTrue(deleted);
		assertEquals(1, anotherServerIndexDirectory.getParentFile().getParentFile().listFiles().length);

		/****************************/
		String yetAnotherServerIndexDirectoryPath = getServerIndexDirectoryPath(indexContext);
		File yetAnotherServerIndexDirectory = FileUtilities.getFile(yetAnotherServerIndexDirectoryPath, Boolean.TRUE);
		createIndex(yetAnotherServerIndexDirectory);
		assertEquals(2, yetAnotherServerIndexDirectory.getParentFile().getParentFile().listFiles().length);

		Directory directory = FSDirectory.open(yetAnotherServerIndexDirectory);
		Lock lock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
		lock.obtain(1000);
		assertTrue(IndexWriter.isLocked(directory));

		// Two directories, one locked there should be two left
		deleted = delete.execute(indexContext);
		assertFalse(deleted);
		assertEquals(2, yetAnotherServerIndexDirectory.getParentFile().getParentFile().listFiles().length);

		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);

		/*************************************/
		String andYetAnotherServerIndexDirectoryPath = getServerIndexDirectoryPath(indexContext);
		File andYetAnotherServerIndexDirectory = FileUtilities.getFile(andYetAnotherServerIndexDirectoryPath, Boolean.TRUE);
		createIndex(andYetAnotherServerIndexDirectory);
		assertEquals(3, andYetAnotherServerIndexDirectory.getParentFile().getParentFile().listFiles().length);

		directory = FSDirectory.open(andYetAnotherServerIndexDirectory);
		lock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
		lock.obtain(1000);
		assertTrue(IndexWriter.isLocked(directory));

		// Three directories, one locked, one should be gone
		deleted = delete.execute(indexContext);
		assertTrue(deleted);
		assertEquals(2, andYetAnotherServerIndexDirectory.getParentFile().getParentFile().listFiles().length);

		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);

		FileUtilities.deleteFile(andYetAnotherServerIndexDirectory, 1);
		FileUtilities.deleteFile(yetAnotherServerIndexDirectory, 1);
		FileUtilities.deleteFile(anotherServerIndexDirectory, 1);
		FileUtilities.deleteFile(serverIndexDirectory, 1);
		FileUtilities.deleteFile(baseIndexDirectory, 1);

		assertFalse(baseIndexDirectory.exists());
		assertFalse(serverIndexDirectory.exists());
		assertFalse(anotherServerIndexDirectory.exists());
		assertFalse(yetAnotherServerIndexDirectory.exists());
		assertFalse(andYetAnotherServerIndexDirectory.exists());

		indexContext.setIndexDirectoryPath(indexDirectoryPath);
	}

}