package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.ATest;
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

	private String testFolder = "./test";
	private File indexDir;

	@Before
	public void before() {
		String indexName = "indexName";
		String indexDirPath = testFolder + "/indexes";
		when(INDEX_CONTEXT.getIndexName()).thenReturn(indexName);
		when(INDEX_CONTEXT.getIndexDirectoryPath()).thenReturn(indexDirPath);
		indexDir = FileUtilities.getFile(getServerIndexDirectoryPath(INDEX_CONTEXT), Boolean.TRUE);
		createIndex(indexDir);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(testFolder), 1);
	}

	@Test
	public void execute() throws Exception {
		Clean clean = new Clean();
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
