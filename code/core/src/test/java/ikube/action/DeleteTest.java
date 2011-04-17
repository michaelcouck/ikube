package ikube.action;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class DeleteTest extends ATest {

	private Delete delete;

	public DeleteTest() {
		super(DeleteTest.class);
	}

	@Before
	public void before() {
		Mockit.tearDownMocks();
		when(INDEX_CONTEXT.getIndexDirectoryPath()).thenReturn("./" + this.getClass().getSimpleName());
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		delete = new Delete();
	}

	@After
	public void after() throws Exception {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		when(INDEX_CONTEXT.getIndexDirectoryPath()).thenReturn(indexDirectoryPath);
	}

	@Test
	public void execute() throws IOException {
		 File baseIndexDirectory = FileUtilities.getFile(INDEX_CONTEXT.getIndexDirectoryPath(), Boolean.TRUE);
		 FileUtilities.deleteFile(baseIndexDirectory, 1);
		 baseIndexDirectory = FileUtilities.getFile(INDEX_CONTEXT.getIndexDirectoryPath(), Boolean.TRUE);
		 assertTrue("We should start with no directories : ", baseIndexDirectory.exists());

		// No indexes so far, nothing to delete
		boolean deleted = delete.execute(INDEX_CONTEXT);
		assertFalse("There are not indexes to delete : ", deleted);
		/*******************************************/

		File latestIndexDirectory = createIndex(INDEX_CONTEXT, "whatever");
		File serverIndexDirectory = new File(latestIndexDirectory, IP);
		assertTrue("Server index directory created : ", serverIndexDirectory.exists());

		// Only one directory so nothing to delete
		deleted = delete.execute(INDEX_CONTEXT);
		assertFalse("The index should not have been deleted : ", deleted);
		assertEquals("There should be only one index : ", 1, serverIndexDirectory.getParentFile().listFiles().length);
		/**************************************************/

		latestIndexDirectory = createIndex(INDEX_CONTEXT, "some more whatever");
		assertEquals(2, latestIndexDirectory.getParentFile().listFiles().length);
		// Two directories so one should be gone
		deleted = delete.execute(INDEX_CONTEXT);
		assertTrue(deleted);
		assertEquals(1, latestIndexDirectory.getParentFile().listFiles().length);

		/****************************/
		latestIndexDirectory = createIndex(INDEX_CONTEXT, "Tired of this?");
		assertEquals(2, latestIndexDirectory.getParentFile().listFiles().length);

		serverIndexDirectory = new File(latestIndexDirectory, IP);
		Directory directory = FSDirectory.open(serverIndexDirectory);
		Lock lock = getLock(directory, serverIndexDirectory);

		// Two directories, one locked there should be two left
		deleted = delete.execute(INDEX_CONTEXT);
		assertFalse(deleted);
		assertEquals(2, latestIndexDirectory.getParentFile().listFiles().length);

		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);

		/*************************************/
		latestIndexDirectory = createIndex(INDEX_CONTEXT);
		assertEquals(3, latestIndexDirectory.getParentFile().listFiles().length);

		serverIndexDirectory = new File(latestIndexDirectory, IP);
		directory = FSDirectory.open(serverIndexDirectory);
		lock = getLock(directory, serverIndexDirectory);
		assertTrue(IndexWriter.isLocked(directory));

		// Three directories, one locked, one should be gone
		deleted = delete.execute(INDEX_CONTEXT);
		assertTrue(deleted);
		assertEquals(2, latestIndexDirectory.getParentFile().listFiles().length);

		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);

		FileUtilities.deleteFile(latestIndexDirectory, 1);
		FileUtilities.deleteFile(serverIndexDirectory, 1);
		// FileUtilities.deleteFile(baseIndexDirectory, 1);

		// assertFalse(baseIndexDirectory.exists());
		assertFalse(serverIndexDirectory.exists());
		assertFalse(latestIndexDirectory.exists());
	}

	private Lock getLock(Directory directory, File serverIndexDirectory) throws IOException {
		logger.info("Is locked : " + IndexWriter.isLocked(directory));
		Lock lock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
		boolean gotLock = lock.obtain(Lock.LOCK_OBTAIN_WAIT_FOREVER);
		logger.info("Got lock : " + gotLock + ", is locked : " + lock.isLocked());
		if (!gotLock) {
			// If the lock is not created then we have to create it. Sometimes
			// this fails to create a lock for some unknown reason, similar to the index writer
			// not really creating the index in ATest, strange!!
			FileUtilities.getFile(new File(serverIndexDirectory, IndexWriter.WRITE_LOCK_NAME).getAbsolutePath(), Boolean.FALSE);
		} else {
			assertTrue(IndexWriter.isLocked(directory));
		}
		logger.info("Is now locked : " + IndexWriter.isLocked(directory));
		return lock;
	}

}