package ikube.index.handler.filesystem;

import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
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

	private String filePath = "./" + this.getClass().getSimpleName();
	private File filesDir = FileUtilities.getFile(filePath, Boolean.TRUE);
	private IndexableFileSystem indexableFileSystem = new IndexableFileSystem();
	private IndexableFilesystemHandler indexableFileSystemHandler = new IndexableFilesystemHandler();

	public IndexableFileSystemHandlerTest() {
		super(IndexableFileSystemHandlerTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, IndexManagerMock.class);
		indexableFileSystem.setPath(filePath);
		indexableFileSystem.setContentFieldName("contentFieldName");
		indexableFileSystem.setLastModifiedFieldName("lastModifiedFieldName");
		indexableFileSystem.setLengthFieldName("lengthFieldName");
		indexableFileSystem.setNameFieldName("nameFieldName");
		indexableFileSystem.setPathFieldName("pathFieldName");
		when(INDEX.getIndexWriter()).thenReturn(INDEX_WRITER);

		indexableFileSystem.setName(this.getClass().getSimpleName());
		List<File> files = FileUtilities.findFilesRecursively(new File("."), new ArrayList<File>(), "doc.doc", "pdf.pdf", "xml.xml");
		for (File file : files) {
			if (file.getName().contains("svn")) {
				continue;
			}
			FileUtilities.copyFile(file, new File(filesDir, file.getName()));
		}
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ApplicationContextManagerMock.class, IndexManagerMock.class);
		FileUtilities.deleteFile(filesDir, 1);
	}

	@Test
	public void handle() throws Exception {
		indexableFileSystemHandler.handle(INDEX_CONTEXT, indexableFileSystem);
	}

}
