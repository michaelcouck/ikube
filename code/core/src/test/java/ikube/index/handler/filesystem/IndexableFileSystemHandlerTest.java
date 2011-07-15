package ikube.index.handler.filesystem;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.database.IDataBase;
import ikube.index.handler.IDocumentDelegate;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO verify all the test methods!
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableFileSystemHandlerTest extends ATest {

	private File powerPointFile;
	private IDataBase dataBase;
	private IDocumentDelegate documentDelegate;
	private IndexableFileSystem indexableFileSystem;
	/** Class under test. */
	private IndexableFilesystemHandler indexableFileSystemHandler;

	public IndexableFileSystemHandlerTest() {
		super(IndexableFileSystemHandlerTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
		dataBase = mock(IDataBase.class);
		indexableFileSystem = mock(IndexableFileSystem.class);
		documentDelegate = mock(IDocumentDelegate.class);

		indexableFileSystemHandler = new IndexableFilesystemHandler();
		indexableFileSystemHandler.setDocumentDelegate(documentDelegate);

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
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void handle() throws Exception {
		List<Thread> threads = indexableFileSystemHandler.handle(INDEX_CONTEXT, indexableFileSystem);
		ThreadUtilities.waitForThreads(threads);
		// TODO Verify that there are some files indexed
	}

	@Test
	public void getBatch() {
		// IDataBase, IndexableFileSystem
		// TODO Implement me
		indexableFileSystemHandler.getBatch(dataBase, indexableFileSystem);
	}

	@Test
	public void iterateFileSystem() {
		// IDataBase, IndexContext<?>, IndexableFileSystem, File, Pattern, Set<File>
		// TODO Implement me
		indexableFileSystemHandler.iterateFileSystem(dataBase, INDEX_CONTEXT, indexableFileSystem, new File("."), Pattern.compile(""),
				new TreeSet<File>());
	}

	@Test
	public void persistFilesBatch() {
		// IDataBase, IndexableFileSystem, Set<File>
		// TODO Implement me
		Set<File> batchedFiles = new TreeSet<File>();
		batchedFiles.add(new File("."));
		indexableFileSystemHandler.persistFilesBatch(dataBase, indexableFileSystem, batchedFiles);
	}

	@Test
	public void handleFile() {
		// IndexContext<?>, IndexableFileSystem, File
		// TODO Implement me
		ikube.model.File file = new ikube.model.File();
		file.setUrl(powerPointFile.getAbsolutePath());
		indexableFileSystemHandler.handleFile(INDEX_CONTEXT, indexableFileSystem, file);
	}

	@Test
	public void getPattern() {
		// String
		// TODO Implement me
		indexableFileSystemHandler.getPattern("");
	}

	@Test
	public void isExcluded() {
		// File, Pattern
		Pattern pattern = Pattern.compile("excluded");
		File file = new File("./excluded");
		boolean isExcluded = indexableFileSystemHandler.isExcluded(file, pattern);
		assertTrue("This pattern is excluded : ", isExcluded);
	}

}