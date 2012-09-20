package ikube.index.handler.filesystem;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.index.IndexManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

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
public class IndexableFileSystemHandlerWorkerTest extends ATest {

	private File powerPointFile;
	private IndexableFileSystem indexableFilesystem;
	private IndexableFilesystemHandler indexableFilesystemHandler;
	/** Class under test. */
	private IndexableFilesystemHandlerWorker indexableFileSystemHandlerWorker;

	public IndexableFileSystemHandlerWorkerTest() {
		super(IndexableFileSystemHandlerWorkerTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
		indexableFilesystem = mock(IndexableFileSystem.class);
		indexableFilesystemHandler = new IndexableFilesystemHandler();
		Stack<File> directories = new Stack<File>();
		indexableFileSystemHandlerWorker = new IndexableFilesystemHandlerWorker(indexableFilesystemHandler, indexContext,
				indexableFilesystem, directories);

		powerPointFile = FileUtilities.findFileRecursively(new File("."), "pot.pot");
		when(indexableFilesystem.getPath()).thenReturn("./");
		when(indexableFilesystem.getBatchSize()).thenReturn(1000);

		when(indexableFilesystem.getContentFieldName()).thenReturn("contentFieldName");
		when(indexableFilesystem.getExcludedPattern())
				.thenReturn(
						".*(.svn).*|.*(.db).*|.*(.gif).*|.*(.svg).*|.*(.jpg).*|.*(.exe).*|.*(.dll).*|.*(password).*|.*(enwiki-latest-pages-articles).*|.*(RSA).*|.*(MANIFEST).*|.*(lib).*");
		when(indexableFilesystem.getLastModifiedFieldName()).thenReturn("lastModifiedFieldName");
		when(indexableFilesystem.getLengthFieldName()).thenReturn("lengthFieldName");
		when(indexableFilesystem.getMaxReadLength()).thenReturn(1000000l);
		when(indexableFilesystem.getNameFieldName()).thenReturn("nameFieldName");
		when(indexableFilesystem.getPathFieldName()).thenReturn("pathFieldName");
		when(indexableFilesystem.isUnpackZips()).thenReturn(Boolean.TRUE);
		ApplicationContextManagerMock.setDataBase(dataBase);
		// Deencapsulation.setField(indexableFilesystemHandler, "dataBase", dataBase);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void getBatch() {
		Stack<File> directories = new Stack<File>();
		directories.push(new File(indexableFilesystem.getPath()));
		List<File> files = indexableFileSystemHandlerWorker.getBatch(indexableFilesystem, directories);
		logger.warn("Files : " + files);
		assertTrue("There must be some files in the dot folder : ", files.size() > 0);
	}

	@Test
	public void handleFile() throws Exception {
		ikube.model.File file = new ikube.model.File();
		file.setUrl(powerPointFile.getAbsolutePath());
		indexableFileSystemHandlerWorker.handleFile(indexContext, indexableFilesystem, new File(file.getUrl()));
		IndexManager.closeIndexWriter(indexContext);
		// Verify that the file is in the index
		verify(indexContext, Mockito.atLeastOnce()).getIndexWriter();
	}

	@Test
	public void getPattern() {
		String excluded = "excluded";
		Pattern pattern = indexableFileSystemHandlerWorker.getPattern(excluded);
		boolean isExcluded = pattern.matcher(excluded).matches();
		assertTrue("This pattern is excluded : ", isExcluded);
	}

	@Test
	public void isExcluded() {
		Pattern pattern = Pattern.compile("excluded");
		File file = new File("./excluded");
		boolean isExcluded = indexableFileSystemHandlerWorker.isExcluded(file, pattern);
		assertTrue("This pattern is excluded : ", isExcluded);
	}

	@Test
	public void handleZip() throws Exception {
		File zipFile = FileUtilities.findFileRecursively(new File("."), Boolean.FALSE, "zip\\.zip");
		boolean unzipped = indexableFileSystemHandlerWorker.handleZip(indexableFilesystem, zipFile);
		assertTrue("The file is fine to unzip : ", unzipped);
		verify(indexContext, Mockito.atLeastOnce()).getIndexWriter();
	}

}