package ikube.action.index.handler.internet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.content.ByteOutputStream;
import ikube.action.index.content.IContentProvider;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

import mockit.Deencapsulation;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableInternetHandlerTest extends AbstractTest {

	private String url = "http://www.google.com";
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
		indexableInternet.setMaxReadLength(Integer.MAX_VALUE);

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
		url.setContentType("text/html");
		List<Url> urls = indexableInternetHandler.handleResource(indexContext, indexableInternet, url);
		logger.info("Urls : " + urls);
		assertNotNull(urls);
		assertTrue(urls.size() > 0);
	}

	@Test
	public void getContentFromUrl() {
		indexableInternet.setUrl(this.url);
		Url url = new Url();
		url.setUrl(this.url);
		ByteOutputStream content = indexableInternetHandler.getContentFromUrl((IContentProvider<IndexableInternet>) null, indexableInternet, url);
		assertTrue(!StringUtils.isEmpty(content.toString()));
	}

}