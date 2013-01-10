package ikube.index.handler.filesystem;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.ThreadUtilities;

import java.io.File;

import org.apache.lucene.document.Document;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 08.02.2011
 * @version 01.00
 */
public class IndexableFilesystemCsvHandlerTest extends ATest {

	private IndexableFileSystemCsv indexableFileSystem;
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
		indexableFileSystem = new IndexableFileSystemCsv();
		filesystemCsvHandler = new IndexableFilesystemCsvHandler();
	}

	@Test
	public void handleFile() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), Boolean.FALSE, "csv.csv");
		// File file = new File("E:/TEST_VAT.csv");
		indexableFileSystem.setEncoding("Windows-1252");
		when(indexContext.getThrottle()).thenReturn(1000l);
		filesystemCsvHandler.handleFile(indexContext, indexableFileSystem, file);
		verify(indexWriter, times(1)).addDocument(any(Document.class));

		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				File file = FileUtilities.findFileRecursively(new File("."), Boolean.FALSE, "csv-large.csv");
				filesystemCsvHandler.handleFile(indexContext, indexableFileSystem, file);
			}
		}, "Csv file reader performance : ", 1, Boolean.TRUE);
		double linesPerSecond = executionsPerSecond * 100000;
		logger.info("Per second : " + linesPerSecond);
		assertTrue(linesPerSecond > 1000);
	}

}
