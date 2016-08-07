package ikube.action.index.handler.filesystem;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.FILE;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class FileSystemCsvResourceProviderTest extends AbstractTest {

    private IndexableFileSystemCsv indexableFileSystemCsv;
    private FileSystemCsvResourceProvider fileSystemCsvResourceProvider;

    @Before
    @SuppressWarnings("ConstantConditions")
    public void before() throws IOException {
        indexableFileSystemCsv = new IndexableFileSystemCsv();
        indexableFileSystemCsv.setEncoding(IConstants.ENCODING);
        indexableFileSystemCsv.setSeparator(",");
        indexableFileSystemCsv.setMaxLines(Integer.MAX_VALUE);

        File file = FILE.findFileRecursively(new File("."), "csv.csv");
        indexableFileSystemCsv.setPath(FILE.cleanFilePath(file.getCanonicalPath()));
        fileSystemCsvResourceProvider = new FileSystemCsvResourceProvider(indexableFileSystemCsv);
    }

    @Test
    public void getResource() {
        List<IndexableColumn> indexableColumns = fileSystemCsvResourceProvider.getResource();
        assertNotNull(indexableColumns);
    }

    @Test
    public void getIndexableColumns() {
        String[] columns = new String[]{"NAME", "ADDRESS", "CD_LATITUDE", "CD_LONGITUDE"};
        List<Indexable> children = new ArrayList<>();
        IndexableColumn indexableColumn = new IndexableColumn();
        indexableColumn.setFieldName(columns[3]);
        children.add(indexableColumn);
        indexableFileSystemCsv.setChildren(children);
        indexableFileSystemCsv.setAllColumns(Boolean.TRUE);
        List<Indexable> sortedChildren = fileSystemCsvResourceProvider.getIndexableColumns(indexableFileSystemCsv, columns);
        assertEquals("There must be four columns, the existing one plus the three from the file : ", 4,
                sortedChildren.size());
        for (int i = 0; i < columns.length; i++) {
            IndexableColumn child = (IndexableColumn) sortedChildren.get(i);
            assertTrue("The columns must be sorted according to the columns in the file : ",
                    child.getFieldName().equals(columns[i]));
        }
    }

}
