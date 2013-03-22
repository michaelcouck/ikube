package ikube.index.handler.internet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.index.handler.ResourceBaseHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import mockit.Deencapsulation;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableInternetHandlerTest extends ATest {

	private List<Document> documents;
	private IndexableInternet indexableInternet;
	private IndexableInternetHandler indexableInternetHandler;
	private ResourceBaseHandler<?> resourceBaseHandler;

	public IndexableInternetHandlerTest() {
		super(IndexableInternetHandlerTest.class);
	}

	@Before
	public void before() {
		new ThreadUtilities().initialize();
		documents = new ArrayList<Document>();

		indexableInternet = new IndexableInternet();
		indexableInternet.setIdFieldName(IConstants.ID);
		indexableInternet.setTitleFieldName(IConstants.TITLE);
		indexableInternet.setContentFieldName(IConstants.CONTENT);
		indexableInternet.setUrl("http://www.ikube.be/site");

		indexableInternetHandler = new IndexableInternetHandler();
		indexableInternetHandler.setThreads(1);

		resourceBaseHandler = new ResourceBaseHandler<IndexableInternet>() {
			public Document handleResource(IndexContext<?> indexContext, IndexableInternet indexable, Document document, Object resource)
					throws Exception {
				documents.add(document);
				return document;
			}
		};
		Deencapsulation.setField(indexableInternetHandler, resourceBaseHandler);
	}

	@After
	public void after() {
		new ThreadUtilities().destroy();
	}

	@Test
	public void handleIndexable() throws Exception {
		List<Future<?>> futures = indexableInternetHandler.handleIndexable(indexContext, indexableInternet);
		ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
		for (final Document document : documents) {
			List<Fieldable> fieldables = document.getFields();
			for (final Fieldable fieldable : fieldables) {
				Field field = (Field) fieldable;
				if (field.isStored()) {
					assertNotNull(field.stringValue());
				}
			}
		}
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