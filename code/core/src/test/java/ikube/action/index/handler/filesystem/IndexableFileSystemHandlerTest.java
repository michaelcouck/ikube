package ikube.action.index.handler.filesystem;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.regex.Pattern;

import mockit.Deencapsulation;

import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests the general functionality of the file system handler. There are no specific checks on the data that is indexed as the sub components are tested
 * separately and the integration tests verify that the data is collected.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableFileSystemHandlerTest extends AbstractTest {

	private File analyticsFolder;
	private String analyticsFolderPath;
	private FileResourceHandler resourceHandler;
	/** Class under test. */
	private IndexableFileSystemHandler indexableFileSystemHandler;

	@Before
	public void before() {
		analyticsFolder = FileUtilities.findDirectoryRecursively(new File("."), "analytics");
		analyticsFolderPath = FileUtilities.cleanFilePath(analyticsFolder.getAbsolutePath());
		resourceHandler = mock(FileResourceHandler.class);
		indexableFileSystemHandler = new IndexableFileSystemHandler();
		Deencapsulation.setField(indexableFileSystemHandler, "resourceHandler", resourceHandler);
	}

	@Test
	public void handleIndexableForked() throws Exception {
		IndexableFileSystem indexableFileSystem = getIndexableFileSystem(analyticsFolderPath);
		indexableFileSystem.setUnpackZips(Boolean.FALSE);
		ForkJoinTask<?> forkJoinTask = indexableFileSystemHandler.handleIndexableForked(indexContext, indexableFileSystem);
		ThreadUtilities.executeForkJoinTasks(this.getClass().getSimpleName(), 3, forkJoinTask);
		ThreadUtilities.sleep(3000);
		verify(resourceHandler, atLeastOnce()).handleResource(any(IndexContext.class), any(IndexableFileSystem.class), any(Document.class), any(File.class));
	}

	@Test
	public void handleLargeGzip() throws Exception {
		File compressedFileDirectory = FileUtilities.findFileRecursively(new File("."), "enwiki-revisions.bz2").getParentFile();
		String compressedFilePath = FileUtilities.cleanFilePath(compressedFileDirectory.getAbsolutePath());
		IndexableFileSystem indexableFileSystem = getIndexableFileSystem(compressedFilePath);
		final ForkJoinTask<?> forkJoinTask = indexableFileSystemHandler.handleIndexableForked(indexContext, indexableFileSystem);
		ThreadUtilities.executeForkJoinTasks(this.getClass().getSimpleName(), 3, forkJoinTask);
		ThreadUtilities.sleep(3000);
		verify(resourceHandler, atLeastOnce()).handleResource(any(IndexContext.class), any(IndexableFileSystem.class), any(Document.class), any(File.class));
	}

	/** Note that this test is ONLY for Linux! */
	@Test
	public void isExcluded() throws Exception {
		File file = Mockito.mock(File.class);
		Mockito.when(file.getName()).thenReturn("image.png");
		Mockito.when(file.getAbsolutePath()).thenReturn("/tmp/image.png");
		Pattern pattern = Pattern.compile(".*(png).*");
		boolean isExcluded = FileUtilities.isExcluded(file, pattern);
		assertTrue(isExcluded);

		File folder = null;
		File symlinkFile = null;
		Path symlink = null;
		try {
			folder = FileUtilities.getFile("/tmp/folder", Boolean.TRUE);
			symlinkFile = new File("/tmp/symlink");
			if (symlinkFile != null && folder != null) {
				symlink = Files.createSymbolicLink(symlinkFile.toPath(), folder.toPath());
				isExcluded = FileUtilities.isExcluded(symlinkFile, pattern);
				assertTrue(isExcluded);
			}
		} finally {
			FileUtilities.deleteFile(folder, 1);
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
		final ForkJoinPool forkJoinPool = ThreadUtilities.getForkJoinPool(indexContext.getName(), indexableFileSystem.getThreads());

		ThreadUtilities.submit(null, new Runnable() {
			public void run() {
				forkJoinPool.invoke(forkJoinTask);
			}
		});

		ThreadUtilities.sleep(1000);
		ThreadUtilities.submit("interrupt-test", new Runnable() {
			public void run() {
				ThreadUtilities.cancellForkJoinPool(indexContext.getName());
			}
		});
		ThreadUtilities.sleep(1000);
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
		indexableFileSystem.setMaxReadLength(1000000l);
		indexableFileSystem.setName("name");
		indexableFileSystem.setNameFieldName("name");
		indexableFileSystem.setPathFieldName("path");
		indexableFileSystem.setTimestamp(new Timestamp(System.currentTimeMillis()));
		return indexableFileSystem;
	}

}