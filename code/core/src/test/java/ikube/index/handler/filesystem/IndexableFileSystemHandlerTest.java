package ikube.index.handler.filesystem;

import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;

import java.io.File;

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

	private File filesDirectory = FileUtilities.findFileRecursively(new File("."), "data");
	private IndexableFileSystem indexableFileSystem = new IndexableFileSystem();
	private IndexableFilesystemHandler indexableFileSystemHandler = new IndexableFilesystemHandler();

	public IndexableFileSystemHandlerTest() {
		super(IndexableFileSystemHandlerTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, IndexManagerMock.class);
		indexableFileSystem.setPath(filesDirectory.getAbsolutePath());
		indexableFileSystem.setContentFieldName("contentFieldName");
		indexableFileSystem.setLastModifiedFieldName("lastModifiedFieldName");
		indexableFileSystem.setLengthFieldName("lengthFieldName");
		indexableFileSystem.setNameFieldName("nameFieldName");
		indexableFileSystem.setPathFieldName("pathFieldName");
		when(INDEX.getIndexWriter()).thenReturn(INDEX_WRITER);

		indexableFileSystem.setName(this.getClass().getSimpleName());
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ApplicationContextManagerMock.class, IndexManagerMock.class);
	}

	@Test
	public void handle() throws Exception {
		indexableFileSystemHandler.handle(INDEX_CONTEXT, indexableFileSystem);
	}

}
