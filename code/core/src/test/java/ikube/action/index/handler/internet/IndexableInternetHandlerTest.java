package ikube.action.index.handler.internet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.handler.ResourceHandlerBase;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.List;

import mockit.Deencapsulation;

import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableInternetHandlerTest extends AbstractTest {

	private List<Document> documents;
	private IndexableInternet indexableInternet;
	private IndexableInternetHandler indexableInternetHandler;
	private ResourceHandlerBase<?> resourceBaseHandler;

	@Before
	public void before() {
		ThreadUtilities.initialize();
		documents = new ArrayList<Document>();

		indexableInternet = new IndexableInternet();
		indexableInternet.setIdFieldName(IConstants.ID);
		indexableInternet.setTitleFieldName(IConstants.TITLE);
		indexableInternet.setContentFieldName(IConstants.CONTENT);

		indexableInternetHandler = new IndexableInternetHandler();

		resourceBaseHandler = new ResourceHandlerBase<IndexableInternet>() {
			public Document handleResource(IndexContext<?> indexContext, IndexableInternet indexable, Document document, Object resource) throws Exception {
				documents.add(document);
				return document;
			}
		};
		Deencapsulation.setField(indexableInternetHandler, resourceBaseHandler);
	}

	@After
	public void after() {
		ThreadUtilities.destroy();
	}

	@Test
	public void handleIndexable() throws Exception {
		indexableInternet.setThreads(3);
		indexableInternet.setUrl("http://www.ikube.be/site/");
		indexableInternetHandler.handleIndexable(indexContext, indexableInternet);
		logger.info("Documents size : " + documents.size());
		assertTrue(documents.size() > 0);
	}

	@Test
	public void handleResource() {
		Url url = new Url();
		String title = "The title";
		String content = "<html><head><title>" + title + "</title></head><body>Hello world</body></html>";
		url.setContentType("text/html");
		url.setRawContent(content.getBytes());
		url.setParsedContent("Hello world");

		indexableInternetHandler.handleResource(indexContext, indexableInternet, new Document(), url);
		assertNotNull(url.getTitle());
		assertEquals(title, url.getTitle());
		assertTrue(documents.size() > 0);
		for (final Document document : documents) {
			String fieldContent = document.get(IConstants.CONTENT);
			assertNotNull(fieldContent);
		}
	}

}