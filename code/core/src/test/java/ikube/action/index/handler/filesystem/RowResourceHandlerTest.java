package ikube.action.index.handler.filesystem;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexableColumn;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.FILE;
import org.apache.lucene.document.Document;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 26-01-2013
 */
public class RowResourceHandlerTest extends AbstractTest {

    @Spy
    @InjectMocks
    private RowResourceHandler rowResourceHandler;
    @Mock
    private IndexableColumn indexableColumn;
    @Mock
    private IndexableFileSystemCsv indexableFileSystemCsv;

    @Test
    @SuppressWarnings("unchecked")
    public void handleResource() throws Exception {
        File file = FILE.findFileRecursively(new File("."), ".csv");
        List children = Arrays.asList(indexableColumn);
        when(indexableFileSystemCsv.getChildren()).thenReturn(children);
        when(indexableFileSystemCsv.getFile()).thenReturn(file);
        when(indexableFileSystemCsv.getLineNumber()).thenReturn(0);
        when(indexableColumn.getContent()).thenReturn(IConstants.CONTENTS);

        rowResourceHandler.handleResource(indexContext, indexableFileSystemCsv, new Document(), null);
        verify(indexableColumn, atLeast(1)).setContent(null);
    }

}