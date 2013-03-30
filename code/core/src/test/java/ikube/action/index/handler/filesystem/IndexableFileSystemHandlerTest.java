package ikube.action.index.handler.filesystem;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.action.index.handler.filesystem.IndexableFilesystemHandler;
import ikube.action.index.handler.filesystem.ResourceFileHandler;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests the general functionality of the file system handler. There are no specific checks on the data that is indexed as the sub
 * components are tested separately and the integration tests verify that the data is collected.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableFileSystemHandlerTest extends ATest {

	/** Class under test. */
	private IndexableFilesystemHandler indexableFileSystemHandler;
	@Cascading
	private ResourceFileHandler resourceHandler;

	@BeforeClass
	public static void beforeClass() {
		new ThreadUtilities().initialize();
		Mockit.setUpMocks();
	}

	@AfterClass
	public static void afterClass() {
		new ThreadUtilities().destroy();
		Mockit.tearDownMocks();
	}

	public IndexableFileSystemHandlerTest() {
		super(IndexableFileSystemHandlerTest.class);
	}

	@Before
	public void before() {
		indexableFileSystemHandler = new IndexableFilesystemHandler();
		indexableFileSystemHandler.setThreads(1);
	}

	@Test
	public void handle() throws Exception {
		logger.info("Resource handler : " + resourceHandler);
		Deencapsulation.setField(indexableFileSystemHandler, "resourceHandler", resourceHandler);
		IndexableFileSystem indexableFileSystem = getIndexableFileSystem(".");
		indexableFileSystem.setUnpackZips(Boolean.FALSE);
		List<Future<?>> futures = indexableFileSystemHandler.handleIndexable(indexContext, indexableFileSystem);
		ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
		assertTrue("There should be some nulls in the futures as the executer is shut down : ", futures.size() > 0);
	}

	@Test
	public void handleLargeGzip() throws Exception {
		Deencapsulation.setField(indexableFileSystemHandler, "resourceHandler", resourceHandler);
		IndexableFileSystem indexableFileSystem = getIndexableFileSystem("/tmp/compressed");
		List<Future<?>> futures = indexableFileSystemHandler.handleIndexable(indexContext, indexableFileSystem);
		ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
	}

	@Test
	public void isExcluded() throws Exception {
		File file = Mockito.mock(File.class);
		Mockito.when(file.getName()).thenReturn("image.png");
		Mockito.when(file.getAbsolutePath()).thenReturn("/tmp/image.png");
		Pattern pattern = Pattern.compile(".*(png).*");
		boolean isExcluded = indexableFileSystemHandler.isExcluded(file, pattern);
		assertTrue(isExcluded);

		File folder = null;
		File symlinkFile = null;
		Path symlink = null;
		try {
			folder = FileUtilities.getFile("/tmp/folder", Boolean.TRUE);
			symlinkFile = new File("/tmp/symlink");
			symlink = Files.createSymbolicLink(symlinkFile.toPath(), folder.toPath());

			isExcluded = indexableFileSystemHandler.isExcluded(symlinkFile, pattern);
			assertTrue(isExcluded);
		} finally {
			FileUtilities.deleteFile(folder, 1);
			Files.deleteIfExists(symlink);
		}
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