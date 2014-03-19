package ikube.action.index.handler.filesystem;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;
import mockit.Deencapsulation;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08.02.2011
 */
public class IndexableFilesystemCsvHandlerTest extends AbstractTest {

    private RowResourceHandler rowResourceHandler;
    private IndexableFileSystemCsv indexableFileSystem;
    /**
     * Class under test.
     */
    private IndexableFilesystemCsvHandler filesystemCsvHandler;

    @Before
    public void before() throws Exception {
        indexableFileSystem = new IndexableFileSystemCsv();
        indexableFileSystem.setEncoding(IConstants.ENCODING);
        indexableFileSystem.setSeparator(",");
        indexableFileSystem.setMaxLines(Integer.MAX_VALUE);
        rowResourceHandler = Mockito.mock(RowResourceHandler.class);

        filesystemCsvHandler = new IndexableFilesystemCsvHandler();
        Deencapsulation.setField(filesystemCsvHandler, "rowResourceHandler", rowResourceHandler);
    }

    @Test
    public void handleFile() throws Exception {
        File file = FileUtilities.findFileRecursively(new File("."), "csv.csv");

        when(indexableColumn.getFieldName()).thenReturn("field-name");
        when(indexableColumn.getContent()).thenReturn("field-content");

        List<Indexable> children = new ArrayList<Indexable>(Arrays.asList(indexableColumn));
        indexableFileSystem.setChildren(children);
        filesystemCsvHandler.handleFile(indexContext, indexableFileSystem, file);
        verify(rowResourceHandler, Mockito.atLeastOnce()).handleResource(any(IndexContext.class),
                any(IndexableFileSystemCsv.class), any(Document.class),
                any(Object.class));

        double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
            public void execute() throws Throwable {
                File file = FileUtilities.findFileRecursively(new File("."), "csv-large.csv");
                filesystemCsvHandler.handleFile(indexContext, indexableFileSystem, file);
            }
        }, "Csv file reader performance : ", 1, Boolean.TRUE);
        double linesPerSecond = executionsPerSecond * 100000;
        logger.info("Per second : " + linesPerSecond);
        assertTrue(linesPerSecond > 1000);
    }

    @Test
    public void handleAutoCompleteFiles() throws Exception {
        indexableFileSystem.setAllColumns(Boolean.TRUE);
        indexableFileSystem.setMaxReadLength(Integer.MAX_VALUE);
        indexableFileSystem.setMaxLines(Integer.MAX_VALUE);
        List<File> files = FileUtilities.findFilesRecursively(new File("."), 2, new ArrayList<File>(), "english.csv");
        filesystemCsvHandler.handleFile(indexContext, indexableFileSystem, files.get(0));
    }

    @Test
    public void getIndexableColumns() {
        String[] columns = new String[]{"NAME", "ADDRESS", "CD_LATITUDE", "CD_LONGITUDE"};
        // IndexableFileSystemCsv indexableFileSystem = new IndexableFileSystemCsv();
        List<Indexable> children = new ArrayList<>();
        IndexableColumn indexableColumn = new IndexableColumn();
        indexableColumn.setFieldName(columns[3]);
        children.add(indexableColumn);
        indexableFileSystem.setChildren(children);
        indexableFileSystem.setAllColumns(Boolean.TRUE);
        List<Indexable> sortedChildren = filesystemCsvHandler.getIndexableColumns(indexableFileSystem, columns);
        assertEquals("There must be four columns, the existing one plus the three from the file : ", 4,
                sortedChildren.size());
        for (int i = 0; i < columns.length; i++) {
            IndexableColumn child = (IndexableColumn) sortedChildren.get(i);
            assertTrue("The columns must be sorted according to the columns in the file : ",
                    child.getFieldName().equals(columns[i]));
        }
    }
}