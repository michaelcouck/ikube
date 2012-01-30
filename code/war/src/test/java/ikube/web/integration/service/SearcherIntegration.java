package ikube.web.integration.service;

import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.security.WebServiceAuthentication;
import ikube.web.service.Searcher;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearcherIntegration {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearcherIntegration.class);

	private static String LOCALHOST = "ikube.dyndns.org";
	/** This client({@link HttpClient}) is for the web services. */
	protected static HttpClient HTTP_CLIENT = new HttpClient();
	protected static int SERVER_PORT = 8080;
	protected static String REST_USER_NAME = "user";
	protected static String REST_PASSWORD = "user";

	/**
	 * Authentication for the web client.
	 * 
	 * @param client
	 *            the client to authenticate with basic authentication
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		WebServiceAuthentication.authenticate(HTTP_CLIENT, LOCALHOST, SERVER_PORT, REST_USER_NAME, REST_PASSWORD);
	}

	@Test
	public void searchSingle() throws Exception {
		// String, String, String, boolean, int, int
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.SINGLE;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
		LOGGER.info("Looking for url : " + url);

		String[] names = { IConstants.INDEX_NAME, IConstants.SEARCH_STRINGS, IConstants.SEARCH_FIELDS, IConstants.FRAGMENT,
				IConstants.FIRST_RESULT, IConstants.MAX_RESULTS };
		String[] values = { IConstants.GEOSPATIAL, "cape AND town AND university", IConstants.NAME, Boolean.TRUE.toString(), "0", "10" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		LOGGER.info("Result : " + actual);
		assertTrue("We should get something : " + result, actual.length() > 0);
	}

	@Test
	public void searchMulti() throws Exception {
		// String, String, String, boolean, int, int
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.MULTI;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
		LOGGER.info("Looking for url : " + url);

		String[] names = { IConstants.INDEX_NAME, IConstants.SEARCH_STRINGS, IConstants.SEARCH_FIELDS, IConstants.FRAGMENT,
				IConstants.FIRST_RESULT, IConstants.MAX_RESULTS };
		String[] values = { IConstants.GEOSPATIAL, "cape AND town AND university;south africa", IConstants.NAME + ";" + IConstants.COUNTRY,
				Boolean.TRUE.toString(), "0", "10" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		LOGGER.info("Result : " + actual);
		assertTrue("We should get something : " + result, actual.length() > 0);
	}

	@Test
	public void searchMultiAll() throws Exception {
		// String, String, boolean, int, int
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.MULTI_ALL;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
		LOGGER.info("Looking for url : " + url);

		String[] names = { IConstants.INDEX_NAME, IConstants.SEARCH_STRINGS, IConstants.FRAGMENT, IConstants.FIRST_RESULT,
				IConstants.MAX_RESULTS };
		String[] values = { IConstants.GEOSPATIAL, "cape AND town AND university;south africa", Boolean.TRUE.toString(), "0", "10" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		LOGGER.info("Result : " + actual);
		assertTrue("We should get something : " + result, actual.length() > 0);
	}

	@Test
	public void searchMultiSorted() throws Exception {
		// String, String, String, String, boolean, int, int
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.MULTI_SORTED;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
		LOGGER.info("Looking for url : " + url);

		String[] names = { IConstants.INDEX_NAME, IConstants.SEARCH_STRINGS, IConstants.SEARCH_FIELDS, IConstants.SORT_FIELDS,
				IConstants.FRAGMENT, IConstants.FIRST_RESULT, IConstants.MAX_RESULTS };
		String[] values = { IConstants.GEOSPATIAL, "cape AND town AND university;south africa", IConstants.NAME + ";" + IConstants.COUNTRY,
				IConstants.NAME + ";" + IConstants.COUNTRY, Boolean.TRUE.toString(), "0", "10" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		LOGGER.info("Result : " + actual);
		assertTrue("We should get something : " + result, actual.length() > 0);
	}

	@Test
	public void searchMultiSpacial() throws Exception {
		// String, String, String, boolean, int, int, int, String, String
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.MULTI_SPATIAL;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
		LOGGER.info("Looking for url : " + url);

		String[] names = { IConstants.INDEX_NAME, IConstants.SEARCH_STRINGS, IConstants.SEARCH_FIELDS, IConstants.FRAGMENT,
				IConstants.FIRST_RESULT, IConstants.MAX_RESULTS, IConstants.DISTANCE, IConstants.LATITUDE, IConstants.LONGITUDE };
		String[] values = { IConstants.GEOSPATIAL, "cape AND town AND university;south africa", IConstants.NAME + ";" + IConstants.COUNTRY,
				Boolean.TRUE.toString(), "0", "10", "50", "50.7930727874172", "4.36242219751376" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		LOGGER.info("Result : " + actual);
		assertTrue("We should get something : " + result, actual.length() > 0);
	}

	@Test
	public void searchMultiSpacialAll() throws Exception {
		// String, String, boolean, int, int, int, String, String
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.MULTI_SPATIAL;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
		LOGGER.info("Looking for url : " + url);

		String[] names = { IConstants.INDEX_NAME, IConstants.SEARCH_STRINGS, IConstants.FRAGMENT, IConstants.FIRST_RESULT,
				IConstants.MAX_RESULTS, IConstants.DISTANCE, IConstants.LATITUDE, IConstants.LONGITUDE };
		String[] values = { IConstants.GEOSPATIAL, "cape AND town AND university;south africa", Boolean.TRUE.toString(), "0", "10", "50",
				"50.7930727874172", "4.36242219751376" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		LOGGER.info("Result : " + actual);
		assertTrue("We should get something : " + result, actual.length() > 0);
	}

	private NameValuePair[] getNameValuePairs(String[] names, String[] values) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (int i = 0; i < names.length && i < values.length; i++) {
			NameValuePair nameValuePair = new NameValuePair(names[i], values[i]);
			nameValuePairs.add(nameValuePair);
		}
		return nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]);
	}

}