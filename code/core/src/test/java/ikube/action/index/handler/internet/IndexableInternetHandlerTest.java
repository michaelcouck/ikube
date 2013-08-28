package ikube.action.index.handler.internet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

import mockit.Deencapsulation;

import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableInternetHandlerTest extends AbstractTest {

	// "http://code.google.com/p/ikube/"
	private String url = "http://www.hazelcast.com";
	private List<Document> documents;
	private IndexableInternet indexableInternet;
	private IndexableInternetHandler indexableInternetHandler;
	private InternetResourceHandler resourceHandler;

	@Before
	public void before() {
		ThreadUtilities.initialize();
		documents = new ArrayList<Document>();

		indexableInternet = new IndexableInternet();
		indexableInternet.setIdFieldName(IConstants.ID);
		indexableInternet.setTitleFieldName(IConstants.TITLE);
		indexableInternet.setContentFieldName(IConstants.CONTENT);

		indexableInternet.setThreads(3);
		indexableInternet.setUrl(url);
		indexableInternet.setBaseUrl(url);

		indexableInternetHandler = new IndexableInternetHandler();
		indexableInternetHandler.initialize();

		resourceHandler = new InternetResourceHandler() {
			public Document handleResource(IndexContext<?> indexContext, IndexableInternet indexable, Document document, Object resource) throws Exception {
				documents.add(document);
				return document;
			}
		};
		Deencapsulation.setField(indexableInternetHandler, resourceHandler);
	}

	@Test
	public void handleIndexable() throws Exception {
		ForkJoinTask<?> forkJoinTask = indexableInternetHandler.handleIndexableForked(indexContext, indexableInternet);
		assertNotNull(forkJoinTask);
	}

	@Test
	public void handleResource() {
		Url url = new Url();
		url.setUrl(this.url);
		String title = "The title";
		String content = "<html><head><title>" + title + "</title></head><body>Hello world</body></html>";
		url.setContentType("text/html");
		url.setRawContent(content.getBytes());
		url.setParsedContent("Hello world");

		List<Url> urls = indexableInternetHandler.handleResource(indexContext, indexableInternet, url);
		logger.info("Urls : " + urls);

		assertNotNull(urls);
		assertTrue(urls.size() > 0);
	}

}