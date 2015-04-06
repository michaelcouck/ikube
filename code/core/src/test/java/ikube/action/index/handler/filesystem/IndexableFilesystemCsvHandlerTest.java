package ikube.action.index.handler.filesystem;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.FILE;
import ikube.toolkit.PERFORMANCE;
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
 * @since 08-02-2011
 */
public class IndexableFilesystemCsvHandlerTest extends AbstractTest {

    private RowResourceHandler rowResourceHandler;
    private IndexableFileSystemCsv indexableFileSystemCsv;
    /**
     * Class under test.
     */
    private IndexableFilesystemCsvHandler filesystemCsvHandler;

    @Before
    public void before() throws Exception {
        indexableFileSystemCsv = new IndexableFileSystemCsv();
        indexableFileSystemCsv.setSeparator(",");
        indexableFileSystemCsv.setAllColumns(Boolean.TRUE);
        indexableFileSystemCsv.setMaxLines(Integer.MAX_VALUE);
        indexableFileSystemCsv.setEncoding(IConstants.ENCODING);

        rowResourceHandler = Mockito.mock(RowResourceHandler.class);

        filesystemCsvHandler = new IndexableFilesystemCsvHandler();
        Deencapsulation.setField(filesystemCsvHandler, "rowResourceHandler", rowResourceHandler);
    }

    @Test
    public void handleFile() throws Exception {
        File file = FILE.findFileRecursively(new File("."), "csv.csv");

        when(indexableColumn.getFieldName()).thenReturn("field-name");
        when(indexableColumn.getContent()).thenReturn("field-content");

        List<Indexable> children = new ArrayList<Indexable>(Arrays.asList(indexableColumn));
        indexableFileSystemCsv.setChildren(children);
        filesystemCsvHandler.handleFile(indexContext, indexableFileSystemCsv, file);
        verify(rowResourceHandler, Mockito.atLeastOnce()).handleResource(any(IndexContext.class),
                any(IndexableFileSystemCsv.class), any(Document.class),
                any(Object.class));

        double executionsPerSecond = PERFORMANCE.execute(new PERFORMANCE.APerform() {
            public void execute() throws Throwable {
                File file = FILE.findFileRecursively(new File("."), "csv-large.csv");
                filesystemCsvHandler.handleFile(indexContext, indexableFileSystemCsv, file);
            }
        }, "Csv file reader performance : ", 1, Boolean.TRUE);
        double linesPerSecond = executionsPerSecond * 100000;
        logger.info("Per second : " + linesPerSecond);
        assertTrue(linesPerSecond > 1000);
    }

    @Test
    public void getIndexableColumns() {
        String[] columns = new String[]{"NAME", "ADDRESS", "CD_LATITUDE", "CD_LONGITUDE"};
        // IndexableFileSystemCsv indexableFileSystemCsv = new IndexableFileSystemCsv();
        List<Indexable> children = new ArrayList<>();
        IndexableColumn indexableColumn = new IndexableColumn();
        indexableColumn.setFieldName(columns[3]);
        children.add(indexableColumn);
        indexableFileSystemCsv.setChildren(children);
        indexableFileSystemCsv.setAllColumns(Boolean.TRUE);
        List<Indexable> sortedChildren = filesystemCsvHandler.getIndexableColumns(indexableFileSystemCsv, columns);
        assertEquals("There must be four columns, the existing one plus the three from the file : ", 4,
                sortedChildren.size());
        for (int i = 0; i < columns.length; i++) {
            IndexableColumn child = (IndexableColumn) sortedChildren.get(i);
            assertTrue("The columns must be sorted according to the columns in the file : ",
                    child.getFieldName().equals(columns[i]));
        }
    }
}