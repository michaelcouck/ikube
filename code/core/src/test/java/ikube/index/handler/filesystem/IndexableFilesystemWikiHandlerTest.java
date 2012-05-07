package ikube.index.handler.filesystem;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import ikube.ATest;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class IndexableFilesystemWikiHandlerTest extends ATest {

	private IndexableFilesystemWikiHandler indexableFilesystemWikiHandler;

	public IndexableFilesystemWikiHandlerTest() {
		super(IndexableFilesystemWikiHandlerTest.class);
	}

	@Before
	public void before() {
		indexableFilesystemWikiHandler = new IndexableFilesystemWikiHandler();
	}

	@Test
	public void handle() throws Exception {
		ThreadUtilities.destroy();
		IndexableFileSystem indexableFileSystem = new IndexableFileSystem();
		indexableFileSystem.setLastModifiedFieldName("lastModifiedFieldName");
		indexableFileSystem.setNameFieldName("nameFieldName");
		indexableFileSystem.setLengthFieldName("lengthFieldName");
		indexableFileSystem.setMaxReadLength(Integer.MAX_VALUE);
		indexableFileSystem.setPathFieldName("pathFieldName");
		indexableFileSystem.setContentFieldName("contentFieldName");
		File file = FileUtilities.findFileRecursively(new File("."), "enwiki-revisions.bz2");
		indexableFileSystem.setPath(file.getAbsolutePath());
		indexableFilesystemWikiHandler.handleFile(indexContext, indexableFileSystem, file);

		verify(indexContext, atLeastOnce()).getIndex();
		verify(index, atLeastOnce()).getIndexWriter();
	}

}