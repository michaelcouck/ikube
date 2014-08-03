package ikube.action.index.handler.strategy;

import static junit.framework.Assert.assertEquals;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 15.11.2013
 * @version 01.00
 */
public class ContentTypeStrategyTest extends AbstractTest {

	private ContentTypeStrategy contentTypeStrategy;

	@Before
	public void before() {
		contentTypeStrategy = new ContentTypeStrategy();
	}

	@Test
	public void aroundProcess() throws Exception {
		Document document = new Document();
		String resource = "http://www.ikube/site/index.html";
		contentTypeStrategy.preProcess(indexContext, indexableColumn, document, resource);
		assertEquals("text/html", document.get(IConstants.MIME_TYPE));

		document = new Document();
		resource = "/a/folder/perhaps";
		contentTypeStrategy.preProcess(indexContext, indexableColumn, document, resource);
		assertEquals(null, document.get(IConstants.MIME_TYPE));

		// Read a word document, no file type and see
		File file = FileUtilities.findFileRecursively(new File("."), "pdf.pdf");
		byte[] content = FileUtilities.getContents(file, Integer.MAX_VALUE).toByteArray();
		Mockito.when(indexableColumn.getRawContent()).thenReturn(content);
		document = new Document();
		resource = "/a/folder/perhaps";
		contentTypeStrategy.preProcess(indexContext, indexableColumn, document, resource);
		assertEquals("application/pdf", document.get(IConstants.MIME_TYPE));
	}

}