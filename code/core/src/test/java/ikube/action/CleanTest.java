package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CleanTest extends ATest {

	public CleanTest() {
		super(CleanTest.class);
	}

	@Before
	public void before() {
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@After
	public void after() {
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

		Clean<IndexContext, Boolean> clean = new Clean<IndexContext, Boolean>();
		clean.execute(INDEX_CONTEXT);
		File serverIndexDirectory = new File(latestIndexDirectory, IP);

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
		assertFalse(serverIndexDirectory.exists());
	}

}