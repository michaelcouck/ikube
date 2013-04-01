package ikube.web.service;

import static org.junit.Assert.assertTrue;
import ikube.Base;
import ikube.IConstants;
import ikube.web.service.SearcherXml;

import java.net.URL;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;

public class SearcherXmlIntegration extends Base {

	public static final String SERVICE = "/service";

	@Test
	public void searchSingle() throws Exception {
		// String, String, String, boolean, int, int
		String path = IConstants.SEP + IConstants.IKUBE + SERVICE + SearcherXml.SEARCH + SearcherXml.SINGLE;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
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
		String path = IConstants.SEP + IConstants.IKUBE + SERVICE + SearcherXml.SEARCH + SearcherXml.MULTI;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
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
	public void searchMultiAll() throws Exception {
		// String, String, boolean, int, int
		String path = IConstants.SEP + IConstants.IKUBE + SERVICE + SearcherXml.SEARCH + SearcherXml.MULTI_ALL;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
		logger.info("Looking for url : " + url);

		String[] names = { //
		IConstants.INDEX_NAME, //
				IConstants.SEARCH_STRINGS, //
				IConstants.FRAGMENT, //
				IConstants.FIRST_RESULT,//
				IConstants.MAX_RESULTS };
		String[] values = { //
		IConstants.GEOSPATIAL, //
				"cape AND town AND university;south africa", //
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
		String path = IConstants.SEP + IConstants.IKUBE + SERVICE + SearcherXml.SEARCH + SearcherXml.MULTI_SORTED;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
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
		String path = IConstants.SEP + IConstants.IKUBE + SERVICE + SearcherXml.SEARCH + SearcherXml.MULTI_SPATIAL;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
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
	public void searchMultiSpacialAll() throws Exception {
		// String, String, boolean, int, int, int, String, String
		String path = IConstants.SEP + IConstants.IKUBE + SERVICE + SearcherXml.SEARCH + SearcherXml.MULTI_SPATIAL_ALL;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
		logger.info("Looking for url : " + url);

		String[] names = { //
		IConstants.INDEX_NAME, //
				IConstants.SEARCH_STRINGS, //
				IConstants.FRAGMENT, //
				IConstants.FIRST_RESULT,//
				IConstants.MAX_RESULTS, //
				IConstants.DISTANCE, //
				IConstants.LATITUDE, //
				IConstants.LONGITUDE };
		String[] values = { //
		IConstants.GEOSPATIAL, //
				"cape town university", //
				Boolean.TRUE.toString(), //
				"0", //
				"10", //
				"10", //
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
	public void searchMultiAdvancedAll() throws Exception {
		// String, String, boolean, int, int, int, String, String
		String path = IConstants.SEP + IConstants.IKUBE + SERVICE + SearcherXml.SEARCH + SearcherXml.MULTI_ADVANCED_ALL;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
		logger.info("Looking for url : " + url);

		String[] names = { //
		IConstants.INDEX_NAME, //
				IConstants.SEARCH_STRINGS, //
				IConstants.SEARCH_FIELDS, //
				IConstants.FRAGMENT, //
				IConstants.FIRST_RESULT,//
				IConstants.MAX_RESULTS };
		String[] values = { //
		IConstants.GEOSPATIAL, //
				"cape town university", //
				IConstants.NAME, //
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
	public void searchComplex() throws Exception {
		String path = IConstants.SEP + IConstants.IKUBE + SERVICE + SearcherXml.SEARCH + SearcherXml.COMPLEX;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
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

}