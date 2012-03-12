package ikube.web.integration.service;

import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.web.Integration;
import ikube.web.service.Searcher;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearcherIntegration extends Integration {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearcherIntegration.class);

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
		LOGGER.info("Query string : " + getMethod.getQueryString());
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
		LOGGER.info("Query string : " + getMethod.getQueryString());
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
		LOGGER.info("Query string : " + getMethod.getQueryString());
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
		LOGGER.info("Query string : " + getMethod.getQueryString());
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
				Boolean.TRUE.toString(), "0", "10", "50", "18.46082", "-33.95796" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		LOGGER.info("Query string : " + getMethod.getQueryString());
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		LOGGER.info("Result : " + actual);
		assertTrue("We should get something : " + result, actual.length() > 0);
	}

	@Test
	public void searchMultiSpacialAll() throws Exception {
		// String, String, boolean, int, int, int, String, String
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.MULTI_SPATIAL_ALL;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
		LOGGER.info("Looking for url : " + url);

		String[] names = { IConstants.INDEX_NAME, IConstants.SEARCH_STRINGS, IConstants.FRAGMENT, IConstants.FIRST_RESULT,
				IConstants.MAX_RESULTS, IConstants.DISTANCE, IConstants.LATITUDE, IConstants.LONGITUDE };
		String[] values = { IConstants.GEOSPATIAL, "cape town university", Boolean.TRUE.toString(), "0", "10", "10", "18.46082",
				"-33.95796" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		LOGGER.info("Query string : " + getMethod.getQueryString());
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		LOGGER.info("Result : " + actual);
		assertTrue("We should get something : " + result, actual.length() > 0);
	}
	
	

	@Test
	public void adHoc() throws Exception {
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.MULTI_ALL;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
		LOGGER.info("Looking for url : " + url);

		String[] names = { IConstants.INDEX_NAME, IConstants.SEARCH_STRINGS, IConstants.FRAGMENT, IConstants.FIRST_RESULT,
				IConstants.MAX_RESULTS };
		String[] values = { "wikiContext", "Небесные создания", Boolean.TRUE.toString(), "0", "10" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		LOGGER.info("Query string : " + getMethod.getQueryString());
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