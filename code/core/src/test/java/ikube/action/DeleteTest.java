package ikube.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.mock.ApplicationContextManagerMock;
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

	private Delete	delete;

	public DeleteTest() {
		super(DeleteTest.class);
	}

	@Before
	public void before() {
		// , IndexSearcherMock.class, IndexReaderMock.class, MultiSearcherMock.class
		Mockit.setUpMocks(ApplicationContextManagerMock.class);
		when(indexContext.getIndexDirectoryPath()).thenReturn("./" + this.getClass().getSimpleName());
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		delete = new Delete();
		delete.setClusterManager(clusterManager);
	}

	@After
	public void after() throws Exception {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		when(indexContext.getIndexDirectoryPath()).thenReturn(indexDirectoryPath);
	}

	@Test
	public void execute() throws IOException {
		File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		FileUtilities.deleteFile(baseIndexDirectory, 1);
		baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		assertTrue("We should start with no directories : ", baseIndexDirectory.exists());

		// No indexes so far, nothing to delete
		boolean deleted = delete.execute(indexContext);
		assertFalse("There are not indexes to delete : ", deleted);
		/*******************************************/

		File latestIndexDirectory = createIndex(indexContext, "whatever");
		File serverIndexDirectory = new File(latestIndexDirectory, ip);
		assertTrue("Server index directory created : ", serverIndexDirectory.exists());

		// Only one directory so nothing to delete
		deleted = delete.execute(indexContext);
		assertFalse("The index should not have been deleted : ", deleted);
		assertEquals("There should be only one index : ", 1, serverIndexDirectory.getParentFile().listFiles().length);
		/**************************************************/

		latestIndexDirectory = createIndex(indexContext, "some more whatever");
		assertEquals(2, latestIndexDirectory.getParentFile().listFiles().length);
		// Two directories so one should be gone
		deleted = delete.execute(indexContext);
		assertTrue(deleted);
		assertEquals(1, latestIndexDirectory.getParentFile().listFiles().length);

		/****************************/
		latestIndexDirectory = createIndex(indexContext, "Tired of this?");
		assertEquals(2, latestIndexDirectory.getParentFile().listFiles().length);

		serverIndexDirectory = new File(latestIndexDirectory, ip);
		Directory directory = FSDirectory.open(serverIndexDirectory);
		Lock lock = getLock(directory, serverIndexDirectory);

		// Two directories, one locked there should be two left
		deleted = delete.execute(indexContext);
		assertFalse(deleted);
		assertEquals(2, latestIndexDirectory.getParentFile().listFiles().length);

		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);

		/*************************************/
		latestIndexDirectory = createIndex(indexContext);
		assertEquals(3, latestIndexDirectory.getParentFile().listFiles().length);

		serverIndexDirectory = new File(latestIndexDirectory, ip);
		directory = FSDirectory.open(serverIndexDirectory);
		lock = getLock(directory, serverIndexDirectory);
		assertTrue(IndexWriter.isLocked(directory));

		// Three directories, one locked, one should be gone
		deleted = delete.execute(indexContext);
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

}