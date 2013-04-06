package ikube.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.toolkit.FileUtilities;

import java.io.File;

import mockit.Deencapsulation;
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
public class DeleteTest extends AbstractTest {

	private Delete delete;

	public DeleteTest() {
		super(DeleteTest.class);
	}

	@Before
	public void before() {
		delete = new Delete();
		Deencapsulation.setField(delete, clusterManager);

		Mockit.setUpMocks(ApplicationContextManagerMock.class);
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@After
	public void after() throws Exception {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void execute() throws Exception {
		File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		FileUtilities.deleteFile(baseIndexDirectory, 1);
		baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		assertTrue("We should start with no directories : ", baseIndexDirectory.exists());

		// 1) No indexes so far, nothing to delete
		boolean deleted = delete.execute(indexContext);
		assertFalse("There are not indexes to delete : ", deleted);

		File latestIndexDirectory = createIndex(indexContext, "whatever");
		assertTrue("Server index directory created : ", latestIndexDirectory.exists());

		// 2) Only one directory so nothing to delete
		deleted = delete.execute(indexContext);
		assertFalse("The index should not have been deleted : ", deleted);
		assertEquals("There should be only one index : ", 1, latestIndexDirectory.getParentFile().listFiles().length);

		latestIndexDirectory = createIndex(indexContext, "some more whatever");
		assertEquals(2, latestIndexDirectory.getParentFile().getParentFile().listFiles().length);
		// 3) Two directories so both should stay
		deleted = delete.execute(indexContext);
		assertFalse(deleted);
		assertEquals(2, latestIndexDirectory.getParentFile().getParentFile().listFiles().length);

		latestIndexDirectory = createIndex(indexContext, "Tired of this?");
		assertEquals(3, latestIndexDirectory.getParentFile().getParentFile().listFiles().length);

		Directory directory = FSDirectory.open(latestIndexDirectory);
		Lock lock = getLock(directory, latestIndexDirectory);

		// 4) Three directories, one locked there should be two left
		deleted = delete.execute(indexContext);
		assertTrue(deleted);
		assertEquals(2, latestIndexDirectory.getParentFile().getParentFile().listFiles().length);

		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);

		latestIndexDirectory = createIndex(indexContext, "some strings");
		assertEquals(3, latestIndexDirectory.getParentFile().getParentFile().listFiles().length);

		directory = FSDirectory.open(latestIndexDirectory);
		lock = getLock(directory, latestIndexDirectory);
		assertTrue(IndexWriter.isLocked(directory));

		// 4) Three directories, one locked, one should be gone
		deleted = delete.execute(indexContext);
		assertTrue(deleted);
		assertEquals(2, latestIndexDirectory.getParentFile().getParentFile().listFiles().length);

		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);

		FileUtilities.deleteFile(latestIndexDirectory, 1);

		assertFalse(latestIndexDirectory.exists());
	}

}