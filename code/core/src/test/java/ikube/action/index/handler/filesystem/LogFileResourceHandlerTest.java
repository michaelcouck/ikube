package ikube.action.index.handler.filesystem;

import ikube.AbstractTest;
import ikube.model.IndexableFileSystemLog;
import ikube.toolkit.FileUtilities;
import org.apache.lucene.document.Document;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-08-2014
 */
public class LogFileResourceHandlerTest extends AbstractTest {

    @Spy
    @InjectMocks
    private LogFileResourceHandler logFileResourceHandler;
    @Mock
    private IndexableFileSystemLog indexableFileSystemLog;

    @Test
    public void handleResource() throws Exception {
        File file = FileUtilities.findFileRecursively(new File("."), ".log");
        logFileResourceHandler.handleResource(indexContext, indexableFileSystemLog, new Document(), file);
        verify(indexWriter, atLeast(1)).addDocument(any(Document.class));
    }

}