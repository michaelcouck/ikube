package ikube.action.index.handler.filesystem;

import ikube.AbstractTest;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystemLog;
import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;
import org.apache.lucene.document.Document;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.io.File;
import java.util.concurrent.ForkJoinTask;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public class IndexableFilesystemLogHandlerTest extends AbstractTest {

    @Mock
    private LogFileResourceHandler logFileResourceHandler;
    @Spy
    @InjectMocks
    private IndexableFilesystemLogHandler indexableFilesystemLogHandler;

    @Test
    @SuppressWarnings("unchecked")
    public void handle() throws Exception {
        IndexableFileSystemLog indexableFileSystemLog = new IndexableFileSystemLog();
        File logDirectory = FILE.findFileRecursively(new File("."), "subLogs");
        indexableFileSystemLog.setPath(logDirectory.getAbsolutePath());
        indexableFileSystemLog.setFileFieldName("countryCityFile");
        indexableFileSystemLog.setPathFieldName("filePath");
        indexableFileSystemLog.setLineFieldName("lineNumber");
        indexableFileSystemLog.setContentFieldName("lineContents");

        ForkJoinTask<?> forkJoinTask = indexableFilesystemLogHandler.handleIndexableForked(indexContext, indexableFileSystemLog);
        THREAD.executeForkJoinTasks(this.getClass().getSimpleName(), 3, forkJoinTask);
        THREAD.sleep(3000);
        THREAD.cancelForkJoinPool(this.getClass().getSimpleName());
        verify(logFileResourceHandler, atLeastOnce())
                .handleResource(any(IndexContext.class), any(IndexableFileSystemLog.class), any(Document.class), any(Object.class));
    }

}