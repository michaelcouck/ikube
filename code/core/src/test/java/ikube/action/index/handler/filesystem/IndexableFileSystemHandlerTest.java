package ikube.action.index.handler.filesystem;

import ikube.AbstractTest;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the general functionality of the file system handler. There are no specific checks on the data that
 * is indexed as the sub components are tested separately and the integration tests verify that the data is collected.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public class IndexableFileSystemHandlerTest extends AbstractTest {

    private String analyticsFolderPath;
    @Mock
    private FileResourceHandler resourceHandler;
    @Spy
    @InjectMocks
    private IndexableFileSystemHandler indexableFileSystemHandler;

    @Before
    public void before() {
        File analyticsFolder = FILE.findDirectoryRecursively(new File("."), "analytics");
        analyticsFolderPath = FILE.cleanFilePath(analyticsFolder.getAbsolutePath());
    }

    @Test
    public void handleIndexableForked() throws Exception {
        IndexableFileSystem indexableFileSystem = getIndexableFileSystem(analyticsFolderPath);
        indexableFileSystem.setUnpackZips(Boolean.FALSE);
        ForkJoinTask<?> forkJoinTask = indexableFileSystemHandler.handleIndexableForked(indexContext, indexableFileSystem);
        THREAD.executeForkJoinTasks(this.getClass().getSimpleName(), 3, forkJoinTask);
        THREAD.waitForFuture(forkJoinTask, Integer.MAX_VALUE);
        verify(resourceHandler, atLeastOnce()).handleResource(any(IndexContext.class), any(IndexableFileSystem.class), any(Document.class), any(File.class));
    }

    @Test
    public void handleFile() throws Exception {
        File file = FILE.findFileRecursively(new File("."), 1, "apache-tomcat-7.0.33.zip");
        IndexableFileSystem indexableFileSystem = getIndexableFileSystem(analyticsFolderPath);
        indexableFileSystem.setUnpackZips(Boolean.TRUE);
        indexableFileSystemHandler.handleFile(indexContext, indexableFileSystem, file);
        verify(resourceHandler, atLeast(100)).handleResource(any(IndexContext.class), any(IndexableFileSystem.class),
                any(Document.class), any(File.class));
    }

    /**
     * Note that this test is ONLY for Linux!
     */
    @Test
    public void isExcluded() throws Exception {
        File file = Mockito.mock(File.class);
        Mockito.when(file.getName()).thenReturn("image.png");
        Mockito.when(file.getAbsolutePath()).thenReturn("/tmp/image.png");
        Pattern pattern = Pattern.compile(".*(png).*");
        boolean isExcluded = FILE.isExcluded(file, pattern);
        assertTrue(isExcluded);

        File symlinkFile;
        File folder = null;
        Path symlink = null;
        try {
            folder = FILE.getFile("/tmp/folder", Boolean.TRUE);
            symlinkFile = new File("/tmp/symlink");
            if (folder != null) {
                symlink = Files.createSymbolicLink(symlinkFile.toPath(), folder.toPath());
                isExcluded = FILE.isExcluded(symlinkFile, pattern);
                assertTrue(isExcluded);
            }
        } finally {
            FILE.deleteFile(folder, 1);
            if (symlink != null) {
                Files.deleteIfExists(symlink);
            }
        }
    }

    @Test
    public void interrupt() throws Exception {
        when(indexContext.getThrottle()).thenReturn(60000l);

        IndexableFileSystem indexableFileSystem = getIndexableFileSystem(analyticsFolderPath);
        indexableFileSystem.setUnpackZips(Boolean.FALSE);
        final ForkJoinTask<?> forkJoinTask = indexableFileSystemHandler.handleIndexableForked(indexContext, indexableFileSystem);
        final ForkJoinPool forkJoinPool = THREAD.getForkJoinPool(indexContext.getName(), indexableFileSystem.getThreads());

        THREAD.submit(null, new Runnable() {
            public void run() {
                forkJoinPool.invoke(forkJoinTask);
            }
        });

        THREAD.sleep(1000);
        THREAD.submit("interrupt-test", new Runnable() {
            public void run() {
                THREAD.cancelForkJoinPool(indexContext.getName());
            }
        });
        THREAD.sleep(1000);
        assertTrue("The future must be cancelled or done : ", forkJoinPool.isTerminated() || forkJoinPool.isTerminating());
    }

    private IndexableFileSystem getIndexableFileSystem(final String folderPath) {
        IndexableFileSystem indexableFileSystem = new IndexableFileSystem();
        indexableFileSystem.setPath(folderPath);
        indexableFileSystem.setBatchSize(10);
        indexableFileSystem.setContentFieldName("content");
        indexableFileSystem.setExcludedPattern(".*(couck).*");
        indexableFileSystem.setIncludedPattern("everything");
        indexableFileSystem.setLastModifiedFieldName("last-modified");
        indexableFileSystem.setLengthFieldName("length");
        indexableFileSystem.setMaxExceptions(100l);
        indexableFileSystem.setMaxReadLength(100000l);
        indexableFileSystem.setName("name");
        indexableFileSystem.setNameFieldName("name");
        indexableFileSystem.setPathFieldName("path");
        indexableFileSystem.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return indexableFileSystem;
    }

}