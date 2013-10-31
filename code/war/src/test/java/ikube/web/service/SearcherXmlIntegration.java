package ikube.web.service;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.IConstants;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;

public class SearcherXmlIntegration extends BaseTest {

	public static final String SERVICE = "/service";

	@Test
	public void searchSingle() throws Exception {
		// String, String, String, boolean, int, int
		String url = getUrl(SearcherXml.SINGLE);
		logger.info("Looking for url : " + url);

		String[] names = { //
		IConstants.INDEX_NAME, //
				IConstants.SEARCH_STRINGS, //
				IConstants.SEARCH_FIELDS, //
				IConstants.FRAGMENT,//
				IConstants.FIRST_RESULT, //
				IConstants.MAX_RESULTS };
		String[] values = {//
		IConstants.GEOSPATIAL,//
				"cape AND town AND university",//
				IConstants.NAME, //
				Boolean.TRUE.toString(),//
				"0", //
				"10" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		logger.info("Query string : " + getMethod.getQueryString());
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		logger.info("Results : " + actual);
		assertTrue("We should get something : " + result + ", " + actual, actual.length() > 0);
	}

	@Test
	public void searchMulti() throws Exception {
		// String, String, String, boolean, int, int
		String url = getUrl(SearcherXml.SINGLE);
		logger.info("Looking for url : " + url);

		String[] names = { //
		IConstants.INDEX_NAME, //
				IConstants.SEARCH_STRINGS, //
				IConstants.SEARCH_FIELDS, //
				IConstants.FRAGMENT,//
				IConstants.FIRST_RESULT, //
				IConstants.MAX_RESULTS };
		String[] values = { //
		IConstants.GEOSPATIAL, //
				"cape AND town AND university;south africa", //
				IConstants.NAME + ";" + IConstants.COUNTRY,//
				Boolean.TRUE.toString(), //
				"0", //
				"10" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		logger.info("Query string : " + getMethod.getQueryString());
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		logger.info("Results : " + actual);
		assertTrue("We should get something : " + result + ", " + actual, actual.length() > 0);
	}

	@Test
	public void searchMultiSorted() throws Exception {
		// String, String, String, String, boolean, int, int
		String url = getUrl(SearcherXml.SORTED);
		logger.info("Looking for url : " + url);

		String[] names = { //
		IConstants.INDEX_NAME, //
				IConstants.SEARCH_STRINGS, //
				IConstants.SEARCH_FIELDS, //
				IConstants.SORT_FIELDS,//
				IConstants.FRAGMENT, //
				IConstants.FIRST_RESULT, //
				IConstants.MAX_RESULTS };
		String[] values = { //
		IConstants.GEOSPATIAL, //
				"cape AND town AND university;south africa", //
				IConstants.NAME + ";" + IConstants.COUNTRY,//
				IConstants.NAME + ";" + IConstants.COUNTRY, //
				Boolean.TRUE.toString(), //
				"0", //
				"10" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		logger.info("Query string : " + getMethod.getQueryString());
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		logger.info("Results : " + actual);
		assertTrue("We should get something : " + result + ", " + actual, actual.length() > 0);
	}

	@Test
	public void searchMultiSpacial() throws Exception {
		// String, String, String, boolean, int, int, int, String, String
		String url = getUrl(SearcherXml.GEOSPATIAL);
		logger.info("Looking for url : " + url);

		String[] names = { //
		IConstants.INDEX_NAME, //
				IConstants.SEARCH_STRINGS, //
				IConstants.SEARCH_FIELDS, //
				IConstants.FRAGMENT, //
				IConstants.FIRST_RESULT, //
				IConstants.MAX_RESULTS, //
				IConstants.DISTANCE, //
				IConstants.LATITUDE, //
				IConstants.LONGITUDE };
		String[] values = { //
		IConstants.GEOSPATIAL, //
				"cape AND town AND university", //
				IConstants.NAME, //
				Boolean.TRUE.toString(), //
				"0", //
				"10", //
				"20", //
				"-33.9693580", //
				"18.4622110" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		logger.info("Query string : " + getMethod.getQueryString());
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		logger.info("Results : " + actual);
		assertTrue("We should get something : " + result + ", " + actual, actual.length() > 0);
	}

	@Test
	public void searchComplex() throws Exception {
		String url = getUrl(SearcherXml.SORTED_TYPED);
		logger.info("Looking for url : " + url);

		String[] names = { //
		IConstants.INDEX_NAME, //
				IConstants.SEARCH_STRINGS, //
				IConstants.SEARCH_FIELDS, //
				IConstants.TYPE_FIELDS, //
				IConstants.FRAGMENT, //
				IConstants.FIRST_RESULT,//
				IConstants.MAX_RESULTS };
		String[] values = { //
		"wikiContext",//
				"string|string", //
				IConstants.NAME + "|" + IConstants.NAME, //
				"string|string", //
				Boolean.TRUE.toString(), //
				"0", //
				"10" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		logger.info("Query string : " + getMethod.getQueryString());
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		logger.info("Results : " + actual);
		assertTrue("We should get something : " + result + ", " + actual, actual.length() > 0);
	}

	private String getUrl(String path) throws MalformedURLException {
		StringBuilder builder = new StringBuilder();
		builder.append(IConstants.SEP);
		builder.append(IConstants.IKUBE);
		builder.append(SERVICE);
		builder.append(SearcherXml.SEARCH);
		builder.append(SearcherXml.XML);
		builder.append(path);
		return new URL("http", LOCALHOST, SERVER_PORT, builder.toString()).toString();
	}

}