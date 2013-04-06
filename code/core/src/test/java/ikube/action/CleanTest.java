package ikube.action;

import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.*;

import ikube.AbstractTest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 16.01.12
 * @version 01.00
 */
public class CleanTest extends AbstractTest {

	public CleanTest() {
		super(CleanTest.class);
	}

	@Before
	public void before() {
		when(indexContext.getIndexDirectoryPath()).thenReturn("./" + this.getClass().getSimpleName());
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
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
		File latestIndexDirectory = createIndex(indexContext, "some words to index");

		// Running the clean the locked index directory should be un-locked
		Clean<IndexContext<?>, Boolean> clean = new Clean<IndexContext<?>, Boolean>();
		Deencapsulation.setField(clean, clusterManager);
		clean.execute(indexContext);

		Directory directory = FSDirectory.open(latestIndexDirectory);
		try {
			assertTrue(IndexReader.indexExists(directory));
		} finally {
			directory.close();
		}

		List<File> files = FileUtilities.findFilesRecursively(latestIndexDirectory, new ArrayList<File>(), "segments");
		for (File file : files) {
			FileUtilities.deleteFile(file, 1);
		}

		clean.execute(indexContext);
		assertFalse("The index directory should have been deleted because it is corrupt : ", latestIndexDirectory.exists());

		latestIndexDirectory = createIndex(indexContext, "some words to index");
		Lock lock = getLock(FSDirectory.open(latestIndexDirectory), latestIndexDirectory);
		boolean isLocked = lock.isLocked();
		assertTrue("We should be able to get the lock from the index directory : ", isLocked);

		clean.execute(indexContext);

		directory = FSDirectory.open(latestIndexDirectory);
		try {
			assertTrue("This index exists, but is locked : ", IndexWriter.isLocked(directory));
			assertTrue("This index exists, but is locked : ", IndexReader.indexExists(directory));
		} finally {
			directory.close();
		}
		clean.execute(indexContext);
		lock.release();
	}

}