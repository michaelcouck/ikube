package ikube.action.index.handler.filesystem;

import static junit.framework.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.junit.Before;
import org.junit.Test;

public class FileResourceHandlerTest extends AbstractTest {

	private FileResourceHandler fileResourceHandler;

	@Before
	public void before() {
		fileResourceHandler = new FileResourceHandler();
	}

	@Test
	public void handleResource() throws Exception {
		IndexableFileSystem indexableFileSystem = new IndexableFileSystem();
		indexableFileSystem.setMaxReadLength(Integer.MAX_VALUE);
		indexableFileSystem.setStored(Boolean.TRUE);
		indexableFileSystem.setAnalyzed(Boolean.TRUE);
		indexableFileSystem.setVectored(Boolean.TRUE);
		indexableFileSystem.setPathFieldName("pathFieldName");
		indexableFileSystem.setNameFieldName("nameFieldName");
		indexableFileSystem.setLastModifiedFieldName("lastModifiedFieldName");
		indexableFileSystem.setLengthFieldName("lengthFieldName");
		indexableFileSystem.setContentFieldName("contentFieldName");

		Document document = new Document();
		File resource = FileUtilities.findFileRecursively(new File("."), "words.txt");
		document = fileResourceHandler.handleResource(indexContext, indexableFileSystem, document, resource);

		Fieldable fieldable = document.getFieldable(indexableFileSystem.getContentFieldName());
		assertTrue(fieldable.isIndexed());
		assertTrue(fieldable.isStored());
		assertTrue(fieldable.isTokenized());
		String content = fieldable.stringValue();
		assertTrue(content.contains("abashed"));
	}

}
