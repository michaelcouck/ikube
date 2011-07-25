package ikube.action;

import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.*;

import ikube.ATest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mockit.Mockit;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CleanTest extends ATest {

	public CleanTest() {
		super(CleanTest.class);
	}

	@Before
	public void before() {
		when(INDEX_CONTEXT.getIndexDirectoryPath()).thenReturn("./" + this.getClass().getSimpleName());
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	/**
	 * In this test we create one index. Then run the clean and nothing should happen, i.e. the index should still remain, nothing should be
	 * deleted. Then we delete one of the index files, i.e. making the index corrupt and run the clean again, this time the clean should
	 * delete the corrupt index.
	 * 
	 * @throws Exception
	 */
	@Test
	public void execute() throws Exception {
		File latestIndexDirectory = createIndex(INDEX_CONTEXT, "some words to index");
		File serverIndexDirectory = new File(latestIndexDirectory, IP);

		// Running the clean the locked index directory should be un-locked
		Clean<IndexContext<?>, Boolean> clean = new Clean<IndexContext<?>, Boolean>();
		clean.execute(INDEX_CONTEXT);

		Directory directory = FSDirectory.open(serverIndexDirectory);
		try {
			assertTrue(IndexReader.indexExists(directory));
		} finally {
			directory.close();
		}

		List<File> files = FileUtilities.findFilesRecursively(latestIndexDirectory, new ArrayList<File>(), "segments");
		for (File file : files) {
			FileUtilities.deleteFile(file, 1);
		}

		clean.execute(INDEX_CONTEXT);
		assertFalse("The index directory should have been deleted because it is corrupt : ", serverIndexDirectory.exists());

		latestIndexDirectory = createIndex(INDEX_CONTEXT, "some words to index");
		serverIndexDirectory = new File(latestIndexDirectory, IP);
		Lock lock = getLock(FSDirectory.open(serverIndexDirectory), serverIndexDirectory);
		boolean isLocked = lock.isLocked();
		assertTrue("We should be able to get the lock from the index directory : ", isLocked);

		clean.execute(INDEX_CONTEXT);

		directory = FSDirectory.open(serverIndexDirectory);
		try {
			assertTrue("This index exists, but is locked : ", IndexWriter.isLocked(directory));
			assertTrue("This index exists, but is locked : ", IndexReader.indexExists(directory));
		} finally {
			directory.close();
		}
		clean.execute(INDEX_CONTEXT);
		lock.release();
	}

}