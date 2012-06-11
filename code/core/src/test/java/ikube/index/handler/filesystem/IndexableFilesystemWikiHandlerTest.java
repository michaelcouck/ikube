package ikube.index.handler.filesystem;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import ikube.ATest;
import ikube.model.IndexableFileSystemWiki;
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
		IndexableFileSystemWiki indexableFileSystem = new IndexableFileSystemWiki();
		indexableFileSystem.setLastModifiedFieldName("lastModifiedFieldName");
		indexableFileSystem.setNameFieldName("nameFieldName");
		indexableFileSystem.setLengthFieldName("lengthFieldName");
		indexableFileSystem.setMaxReadLength(Integer.MAX_VALUE);
		indexableFileSystem.setPathFieldName("pathFieldName");
		indexableFileSystem.setContentFieldName("contentFieldName");
		indexableFileSystem.setMaxRevisions(Integer.MAX_VALUE);
		File file = FileUtilities.findFileRecursively(new File("."), "enwiki-revisions.bz2");
		indexableFileSystem.setPath(file.getAbsolutePath());
		indexableFilesystemWikiHandler.handleFile(indexContext, indexableFileSystem, file);

		verify(indexContext, atLeastOnce()).getIndex();
		verify(index, atLeastOnce()).getIndexWriter();
	}

}