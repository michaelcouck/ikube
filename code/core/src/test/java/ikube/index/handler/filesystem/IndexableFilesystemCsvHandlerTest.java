package ikube.index.handler.filesystem;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import ikube.ATest;
import ikube.IConstants;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.ThreadUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.lucene.document.Document;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
		// File file = new File("/home/michael/Desktop/TEST_VAT.csv"); // CP1252
		indexableFileSystem.setEncoding(IConstants.ENCODING);
		// when(indexContext.getThrottle()).thenReturn(1000l);
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

	@Test
	@Ignore
	public void copyLines() throws Exception {
		File file = new File("/home/michael/Desktop/TEST_VAT.csv");
		InputStream inputStream = new FileInputStream(file);
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "CP1252");
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		for (int i = 0; i > 10; i++) {
			String line = bufferedReader.readLine();
			System.out.println(line);
		}

		byte[] bytes = new byte[1024 * 10];
		inputStream.read(bytes);
		System.out.println(new String(bytes, "CP1252"));
		inputStream.close();
	}

}