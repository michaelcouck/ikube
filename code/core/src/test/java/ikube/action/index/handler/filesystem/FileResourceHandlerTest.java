package ikube.action.index.handler.filesystem;

import ikube.AbstractTest;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FILE;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

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
        indexableFileSystem.setTokenized(Boolean.FALSE);
		indexableFileSystem.setPathFieldName("pathFieldName");
		indexableFileSystem.setNameFieldName("nameFieldName");
		indexableFileSystem.setLastModifiedFieldName("lastModifiedFieldName");
		indexableFileSystem.setLengthFieldName("lengthFieldName");
		indexableFileSystem.setContentFieldName("contentFieldName");

		Document document = new Document();
		File resource = FILE.findFileRecursively(new File("."), "words.txt");
		document = fileResourceHandler.handleResource(indexContext, indexableFileSystem, document, resource);

		IndexableField fieldable = document.getField(indexableFileSystem.getContentFieldName());
		assertTrue(fieldable.fieldType().indexed());
		assertTrue(fieldable.fieldType().stored());
		assertFalse(fieldable.fieldType().tokenized());
		String content = fieldable.stringValue();
		assertTrue(content.contains("abashed"));
	}

}
