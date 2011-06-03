package ikube.index.handler.filesystem;

import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.index.handler.DocumentDelegate;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.List;

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableFileSystemHandlerTest extends ATest {

	private File filesDirectory;
	private IndexableFileSystem indexableFileSystem = new IndexableFileSystem();
	private IndexableFilesystemHandler indexableFileSystemHandler = new IndexableFilesystemHandler();

	public IndexableFileSystemHandlerTest() {
		super(IndexableFileSystemHandlerTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks();
		Mockit.setUpMocks(ApplicationContextManagerMock.class, IndexManagerMock.class);
		when(INDEX.getIndexWriter()).thenReturn(INDEX_WRITER);
		
		File file = FileUtilities.findFileRecursively(new File("."), "99.ppt");
		filesDirectory = file.getParentFile();
		
		indexableFileSystem.setPath(filesDirectory.getAbsolutePath());
		indexableFileSystem.setContentFieldName("contentFieldName");
		indexableFileSystem.setLastModifiedFieldName("lastModifiedFieldName");
		indexableFileSystem.setLengthFieldName("lengthFieldName");
		indexableFileSystem.setNameFieldName("nameFieldName");
		indexableFileSystem.setPathFieldName("pathFieldName");
		indexableFileSystem.setName(this.getClass().getSimpleName());
		indexableFileSystem.setExcludedPattern(".svn");
		
		indexableFileSystemHandler.setDocumentDelegate(new DocumentDelegate());
		indexableFileSystemHandler.setThreads(3);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ApplicationContextManagerMock.class, IndexManagerMock.class);
	}

	@Test
	public void handle() throws Exception {
		List<Thread> threads = indexableFileSystemHandler.handle(INDEX_CONTEXT, indexableFileSystem);
		ThreadUtilities.waitForThreads(threads);
		// Verify that there are some files indexed
	}

}
