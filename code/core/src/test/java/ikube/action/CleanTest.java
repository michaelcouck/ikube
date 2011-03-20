package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
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

	private String indexDirPath;

	public CleanTest() {
		super(CleanTest.class);
	}

	@Before
	public void before() {
		when(INDEX_CONTEXT.getIndexDirectoryPath()).thenReturn("./" + this.getClass().getSimpleName());
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		indexDirPath = getServerIndexDirectoryPath(INDEX_CONTEXT);
		createIndex(FileUtilities.getFile(indexDirPath, Boolean.TRUE));
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@Test
	public void execute() throws Exception {
		File indexDir = FileUtilities.getFile(indexDirPath, Boolean.TRUE);
		for (File file : indexDir.listFiles()) {
			logger.info("File : " + file);
		}

		Clean<IndexContext, Boolean> clean = new Clean<IndexContext, Boolean>();
		clean.execute(INDEX_CONTEXT);
		Directory directory = FSDirectory.open(indexDir);
		try {
			assertTrue(IndexReader.indexExists(directory));
		} finally {
			directory.close();
		}

		List<File> files = FileUtilities.findFilesRecursively(indexDir, new String[] { "segments" }, new ArrayList<File>());
		for (File file : files) {
			FileUtilities.deleteFile(file, 1);
		}

		clean.execute(INDEX_CONTEXT);
		assertFalse(indexDir.exists());
	}

}
