package ikube.index.handler.filesystem;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import ikube.ATest;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;

import org.apache.lucene.document.Document;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IndexableFilesystemCsvHandlerTest extends ATest {

	/** Class under test. */
	private IndexableFilesystemCsvHandler filesystemCsvHandler;

	@BeforeClass
	public static void beforeClass() {
		new ThreadUtilities().initialize();
	}

	@AfterClass
	public static void afterClass() {
		new ThreadUtilities().destroy();
	}

	public IndexableFilesystemCsvHandlerTest() {
		super(IndexableFilesystemCsvHandlerTest.class);
	}

	@Before
	public void before() {
		filesystemCsvHandler = new IndexableFilesystemCsvHandler();
	}

	@Test
	public void handleFile() throws Exception {
		// final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final File file
		IndexableFileSystemCsv indexableFileSystem = new IndexableFileSystemCsv();
		File file = FileUtilities.findFileRecursively(new File("."), Boolean.FALSE, "csv.csv");
		filesystemCsvHandler.handleFile(indexContext, indexableFileSystem, file);
		verify(indexWriter, atLeastOnce()).addDocument(any(Document.class));
	}

}
