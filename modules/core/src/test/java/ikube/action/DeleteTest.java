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

public class DeleteTest extends BaseActionTest {

	private Delete delete = new Delete();

	@Test
	public void execute() throws Exception {
		File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		FileUtilities.deleteFile(baseIndexDirectory, 1);
		assertFalse(baseIndexDirectory.exists());
		baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		assertTrue(baseIndexDirectory.exists());

		// No indexes so far, nothing to delete
		boolean deleted = delete.execute(indexContext);
		assertFalse(deleted);
		/*******************************************/

		String filePath = baseIndexDirectory.getAbsolutePath() + File.separator + System.currentTimeMillis();
		File latestIndexDirectory = FileUtilities.getFile(filePath, Boolean.TRUE);
		assertTrue(latestIndexDirectory.exists());
		File serverIndexDirectory = createIndex(latestIndexDirectory, indexContext.getServerName());
		assertTrue(serverIndexDirectory.exists());

		// Only one directory so nothing to delete
		deleted = delete.execute(indexContext);
		assertFalse(deleted);
		assertEquals(1, baseIndexDirectory.listFiles().length);
		/**************************************************/

		filePath = baseIndexDirectory.getAbsolutePath() + File.separator + System.currentTimeMillis();
		latestIndexDirectory = FileUtilities.getFile(filePath, Boolean.TRUE);
		assertEquals(2, baseIndexDirectory.listFiles().length);
		// Two directories so one should be gone
		deleted = delete.execute(indexContext);
		assertTrue(deleted);
		assertEquals(1, baseIndexDirectory.listFiles().length);

		/****************************/
		filePath = baseIndexDirectory.getAbsolutePath() + File.separator + System.currentTimeMillis();
		latestIndexDirectory = FileUtilities.getFile(filePath, Boolean.TRUE);
		assertEquals(2, baseIndexDirectory.listFiles().length);

		serverIndexDirectory = createIndex(latestIndexDirectory, indexContext.getServerName());
		Directory directory = FSDirectory.open(serverIndexDirectory);
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
		filePath = baseIndexDirectory.getAbsolutePath() + File.separator + System.currentTimeMillis();
		latestIndexDirectory = FileUtilities.getFile(filePath, Boolean.TRUE);
		assertEquals(3, baseIndexDirectory.listFiles().length);

		serverIndexDirectory = createIndex(latestIndexDirectory, indexContext.getServerName());
		directory = FSDirectory.open(serverIndexDirectory);
		lock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
		lock.obtain(1000);
		assertTrue(IndexWriter.isLocked(directory));

		// Three directories, one locked, one should be gone
		deleted = delete.execute(indexContext);
		assertTrue(deleted);
		assertEquals(2, baseIndexDirectory.listFiles().length);

		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);

		FileUtilities.deleteFile(baseIndexDirectory, 1);
	}

}