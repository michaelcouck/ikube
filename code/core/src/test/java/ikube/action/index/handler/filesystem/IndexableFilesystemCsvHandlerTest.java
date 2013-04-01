package ikube.action.index.handler.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.IConstants;
import ikube.action.index.handler.filesystem.IndexableFilesystemCsvHandler;
import ikube.action.index.handler.filesystem.ResourceRowHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mockit.Deencapsulation;

import org.apache.lucene.document.Document;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

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
		ThreadUtilities.initialize();
	}

	@AfterClass
	public static void afterClass() {
		ThreadUtilities.destroy();
	}

	public IndexableFilesystemCsvHandlerTest() {
		super(IndexableFilesystemCsvHandlerTest.class);
	}

	@Before
	public void before() throws Exception {
		indexableFileSystem = new IndexableFileSystemCsv();
		filesystemCsvHandler = new IndexableFilesystemCsvHandler();
	}

	@Test
	public void handleFile() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), Boolean.FALSE, "csv.csv");
		indexableFileSystem.setEncoding(IConstants.ENCODING);
		indexableFileSystem.setSeparator(",");

		ResourceRowHandler resourceRowHandler = Mockito.mock(ResourceRowHandler.class);
		Deencapsulation.setField(filesystemCsvHandler, "resourceRowHandler", resourceRowHandler);

		when(indexableColumn.getFieldName()).thenReturn("field-name");
		when(indexableColumn.getContent()).thenReturn("field-content");

		List<Indexable<?>> children = new ArrayList<Indexable<?>>(Arrays.asList(indexableColumn));
		indexableFileSystem.setChildren(children);
		filesystemCsvHandler.handleFile(indexContext, indexableFileSystem, file);
		verify(resourceRowHandler, Mockito.atLeastOnce()).handleResource(any(IndexContext.class), any(IndexableFileSystemCsv.class),
				any(Document.class), any(Object.class));

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
	public void getIndexableColumns() {
		String[] columns = new String[] { "NAME", "ADDRESS", "CD_LATITUDE", "CD_LONGITUDE" };
		Indexable<?> indexable = new IndexableFileSystemCsv();
		List<Indexable<?>> children = new ArrayList<Indexable<?>>();
		IndexableColumn indexableColumn = new IndexableColumn();
		indexableColumn.setFieldName(columns[3]);
		children.add(indexableColumn);
		indexable.setChildren(children);
		List<Indexable<?>> sortedChildren = filesystemCsvHandler.getIndexableColumns(indexable, columns);
		assertEquals("There must be four columns, the existing one plus the three from the file : ", 4, sortedChildren.size());
		for (int i = 0; i < columns.length; i++) {
			IndexableColumn child = (IndexableColumn) sortedChildren.get(i);
			assertTrue("The columns must be sorted according to the columns in the file : ", child.getFieldName().equals(columns[i]));
		}
	}
}