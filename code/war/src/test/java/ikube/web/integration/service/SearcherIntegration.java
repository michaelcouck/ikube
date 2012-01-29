package ikube.web.integration.service;

import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.security.WebServiceAuthentication;
import ikube.web.service.Searcher;

import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearcherIntegration {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearcherIntegration.class);

	private static String LOCALHOST = "localhost";
	/** This client({@link HttpClient}) is for the web services. */
	protected static HttpClient HTTP_CLIENT = new HttpClient();
	protected static int SERVER_PORT = 9080;
	protected static String REST_USER_NAME = "user";
	protected static String REST_PASSWORD = "user";

	/**
	 * Authentication for the web client.
	 * 
	 * @param client the client to authenticate with basic authentication
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		WebServiceAuthentication.authenticate(HTTP_CLIENT, LOCALHOST, SERVER_PORT, REST_USER_NAME, REST_PASSWORD);
	}

	@Test
	public void search() throws Exception {
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.SINGLE;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
		LOGGER.info("Looking for url : " + url);

		NameValuePair indexName = new NameValuePair(IConstants.INDEX_NAME, IConstants.GEOSPATIAL);
		NameValuePair searchStrings = new NameValuePair(IConstants.SEARCH_STRINGS, "cape AND town AND university");
		NameValuePair searchFields = new NameValuePair(IConstants.SEARCH_FIELDS, IConstants.NAME);
		NameValuePair fragment = new NameValuePair(IConstants.FRAGMENT, Boolean.TRUE.toString());
		NameValuePair firstResult = new NameValuePair(IConstants.FIRST_RESULT, "0");
		NameValuePair maxResults = new NameValuePair(IConstants.MAX_RESULTS, "10");

		GetMethod getMethod = new GetMethod(url);
		NameValuePair[] params = new NameValuePair[] { indexName, searchStrings, searchFields, fragment, firstResult, maxResults };
		getMethod.setQueryString(params);
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		LOGGER.info("Result : " + actual);
		assertTrue("We should get something : " + result, actual.length() > 0);
	}

}