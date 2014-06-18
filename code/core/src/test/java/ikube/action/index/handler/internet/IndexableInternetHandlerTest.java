package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.security.WebServiceAuthentication;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import mockit.Deencapsulation;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinTask;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public class IndexableInternetHandlerTest extends AbstractTest {

	private String url = "http://www.ikube.be/ikube";
	private IndexableInternet indexableInternet;
	private IndexableInternetHandler indexableInternetHandler;

	@Before
	public void before() {
		ThreadUtilities.initialize();

		indexableInternet = new IndexableInternet();
		indexableInternet.setName("indexable-internet");
		indexableInternet.setIdFieldName(IConstants.ID);
		indexableInternet.setTitleFieldName(IConstants.TITLE);
		indexableInternet.setContentFieldName(IConstants.CONTENT);

		indexableInternet.setThreads(3);
		indexableInternet.setUrl(url);
		indexableInternet.setBaseUrl(url);
		indexableInternet.setMaxReadLength(Integer.MAX_VALUE);
		indexableInternet.setExcludedPattern("some-pattern");
		indexableInternet.setParent(indexContext);

		indexableInternetHandler = new IndexableInternetHandler();

		InternetResourceHandler resourceHandler = new InternetResourceHandler() {
			public Document handleResource(
			  final IndexContext indexContext,
			  final IndexableInternet indexable,
			  final Document document,
			  final Object resource)
			  throws Exception {
				return document;
			}
		};
		Deencapsulation.setField(indexableInternetHandler, dataBase);
		Deencapsulation.setField(indexableInternetHandler, resourceHandler);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
	}

	@Test
	public void handleIndexable() throws Exception {
		ForkJoinTask<?> forkJoinTask = indexableInternetHandler.handleIndexableForked(indexContext, indexableInternet);
		ThreadUtilities.waitForFuture(forkJoinTask, 10);
		assertNotNull(forkJoinTask);
	}

	@Test
	@Ignore
	/**
	 * TODO: This test and the logic needs to be completed!!!
	 */
	public void login() throws Exception {

		/*System.setProperty("http.proxyUser", "id837406");
		System.setProperty("http.proxyPassword", "xxx");
		System.setProperty("http.proxyPort", "8080");
		System.setProperty("http.proxyHost", "wwwintproxy.bgc.net");

		new ProxyAuthenticator().initialize();*/

		url = "http://el1710.bc:18080/svn/";
		// url = "http://www.google.com";

		HttpClient httpClient = new AutoRetryHttpClient();
		new WebServiceAuthentication().authenticate(httpClient, url, 18080, "id837406", "xxx");

		HttpGet httpGet = new HttpGet("http://www.google.com");
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
			@Override
			public String handleResponse(final HttpResponse response) {
				try {
					return FileUtilities.getContents(response.getEntity().getContent(), Long.MAX_VALUE).toString();
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
		String response = httpClient.execute(httpGet, responseHandler);
		logger.info("Response : " + response);

		/*indexableInternet.setUrl(url);
		indexableInternet.setBaseUrl(url);
		indexableInternet.setUserid("id8370406");
		indexableInternet.setPassword("xxx");
		handleIndexable();*/
	}

	@Test
	public void handleResource() {
		Url url = mock(Url.class);

		when(url.getUrl()).thenReturn(this.url);
		when(url.getParsedContent()).thenReturn("text/html");
		when(url.getRawContent()).thenReturn("hello world".getBytes());

		indexableInternetHandler.handleResource(indexContext, indexableInternet, url);
		verify(url, atLeastOnce()).setParsedContent(anyString());
	}

}