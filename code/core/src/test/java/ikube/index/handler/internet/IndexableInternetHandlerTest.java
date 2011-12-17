package ikube.index.handler.internet;

import ikube.ATest;
import ikube.model.IndexableInternet;
import ikube.toolkit.FileUtilities;

import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Before;
import org.junit.Ignore;
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
	@Ignore
	public void login() throws Exception {
		Mockito.when(indexableInternet.getLoginUrl()).thenReturn("http://localhost:8080/ikube/admin/login.html");
		Mockito.when(indexableInternet.getUserid()).thenReturn("guest");
		Mockito.when(indexableInternet.getPassword()).thenReturn("guest");

		HttpClient httpClient = new HttpClient();

		indexableInternetHandler.login(indexableInternet, httpClient);

		GetMethod get = new GetMethod("http://localhost:8080/ikube/admin/servers.html");
		httpClient.executeMethod(get);
		InputStream responseInputStream = get.getResponseBodyAsStream();
		String content = FileUtilities.getContents(responseInputStream, Integer.MAX_VALUE).toString();
		logger.info(content);
	}

}
