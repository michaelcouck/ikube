package ikube.index.handler.internet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.ATest;
import ikube.model.IndexableInternet;
import ikube.model.Url;

import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class IndexableInternetHandlerTest extends ATest {

	private IndexableInternet indexableInternet;
	private IndexableInternetHandler indexableInternetHandler;

	public IndexableInternetHandlerTest() {
		super(IndexableInternetHandlerTest.class);
	}

	@Before
	public void before() {
		indexableInternet = Mockito.mock(IndexableInternet.class);
		indexableInternetHandler = new IndexableInternetHandler();
	}

	@Test
	public void addDocument() {
		Url url = new Url();
		String title = "The title";
		String content = "<html><head><title>" + title + "</title></head></html>";
		url.setContentType("text/html");
		url.setRawContent(content.getBytes());
		indexableInternetHandler.handleResource(indexContext, indexableInternet, new Document(), url);
		assertNotNull(url.getTitle());
		assertEquals(title, url.getTitle());
	}

}
