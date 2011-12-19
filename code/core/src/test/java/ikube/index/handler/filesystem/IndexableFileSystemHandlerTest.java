package ikube.index.handler.filesystem;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.index.IndexManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
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

	private File powerPointFile;
	private IndexableFileSystem indexableFileSystem;
	/** Class under test. */
	private IndexableFilesystemHandler indexableFileSystemHandler;

	public IndexableFileSystemHandlerTest() {
		super(IndexableFileSystemHandlerTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
		indexableFileSystem = mock(IndexableFileSystem.class);

		indexableFileSystemHandler = new IndexableFilesystemHandler();
		indexableFileSystemHandler.setThreads(1);

		powerPointFile = FileUtilities.findFileRecursively(new File("."), "pot.pot");
		when(indexableFileSystem.getPath()).thenReturn(powerPointFile.getParentFile().getAbsolutePath());
		when(indexableFileSystem.getBatchSize()).thenReturn(1000);

		when(indexableFileSystem.getContentFieldName()).thenReturn("contentFieldName");
		when(indexableFileSystem.getExcludedPattern()).thenReturn("svn");
		when(indexableFileSystem.getLastModifiedFieldName()).thenReturn("lastModifiedFieldName");
		when(indexableFileSystem.getLengthFieldName()).thenReturn("lengthFieldName");
		when(indexableFileSystem.getMaxReadLength()).thenReturn(Long.MAX_VALUE);
		when(indexableFileSystem.getNameFieldName()).thenReturn("nameFieldName");
		when(indexableFileSystem.getPathFieldName()).thenReturn("pathFieldName");
		ApplicationContextManagerMock.setDataBase(dataBase);
		Deencapsulation.setField(indexableFileSystemHandler, dataBase);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void handle() throws Exception {
		List<Future<?>> futures = indexableFileSystemHandler.handle(indexContext, indexableFileSystem);
		ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
		// Verify that the database is called to find
		verify(dataBase, Mockito.atLeastOnce()).find(any(Class.class), anyString(), anyMap(), anyInt(), anyInt());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getBatch() {
		Object result = Deencapsulation.invoke(indexableFileSystemHandler, "getBatch", dataBase, indexableFileSystem);
		logger.warn("Result : " + result);
		// Verify that the database is called to merge
		verify(dataBase, Mockito.atLeastOnce()).mergeBatch(anyList());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void iterateFileSystem() {
		indexableFileSystemHandler.iterateFileSystem(dataBase, indexContext, indexableFileSystem, new File("."), Pattern.compile(""),
				new TreeSet<File>());
		// Verify that the database was called to persist
		verify(dataBase, Mockito.atLeastOnce()).persistBatch(anyList());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void persistFilesBatch() {
		Set<File> batchedFiles = new TreeSet<File>();
		batchedFiles.add(new File("."));
		indexableFileSystemHandler.persistFilesBatch(dataBase, indexableFileSystem, batchedFiles);
		// Verify that the database was called to persist
		verify(dataBase, Mockito.atLeastOnce()).persistBatch(anyList());
	}

	@Test
	public void handleFile() throws Exception {
		ikube.model.File file = new ikube.model.File();
		file.setUrl(powerPointFile.getAbsolutePath());
		indexableFileSystemHandler.handleFile(indexContext, indexableFileSystem, file);
		IndexManager.closeIndexWriter(indexContext);
		// Verify that the file is in the index
		verify(indexContext, Mockito.atLeastOnce()).getIndex();
	}

	@Test
	public void getPattern() {
		String excluded = "excluded";
		Pattern pattern = indexableFileSystemHandler.getPattern(excluded);
		boolean isExcluded = pattern.matcher(excluded).matches();
		assertTrue("This pattern is excluded : ", isExcluded);
	}

	@Test
	public void isExcluded() {
		Pattern pattern = Pattern.compile("excluded");
		File file = new File("./excluded");
		boolean isExcluded = indexableFileSystemHandler.isExcluded(file, pattern);
		assertTrue("This pattern is excluded : ", isExcluded);
	}

}