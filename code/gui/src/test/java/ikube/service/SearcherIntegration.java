package ikube.service;

import static org.junit.Assert.assertTrue;
import ikube.Base;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

public class SearcherIntegration extends Base {

	@Test
	public void searchSingle() throws Exception {
		// String, String, String, boolean, int, int
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.SINGLE;
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
		assertTrue("We should get something : " + result + ", " + actual, actual.length() > 0);
	}

	@Test
	public void searchMulti() throws Exception {
		// String, String, String, boolean, int, int
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.MULTI;
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
		assertTrue("We should get something : " + result + ", " + actual, actual.length() > 0);
	}

	@Test
	public void searchMultiAll() throws Exception {
		// String, String, boolean, int, int
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.MULTI_ALL;
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
		assertTrue("We should get something : " + result + ", " + actual, actual.length() > 0);
	}

	@Test
	public void searchMultiSorted() throws Exception {
		// String, String, String, String, boolean, int, int
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.MULTI_SORTED;
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
		assertTrue("We should get something : " + result + ", " + actual, actual.length() > 0);
	}

	@Test
	public void searchMultiSpacial() throws Exception {
		// String, String, String, boolean, int, int, int, String, String
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.MULTI_SPATIAL;
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
		assertTrue("We should get something : " + result + ", " + actual, actual.length() > 0);
	}

	@Test
	public void searchMultiSpacialAll() throws Exception {
		// String, String, boolean, int, int, int, String, String
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.MULTI_SPATIAL_ALL;
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
		assertTrue("We should get something : " + result + ", " + actual, actual.length() > 0);
	}

	@Test
	public void adHoc() throws Exception {
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.MULTI_ALL;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();
		logger.info("Looking for url : " + url);

		String[] names = { //
		IConstants.INDEX_NAME, //
				IConstants.SEARCH_STRINGS, //
				IConstants.FRAGMENT, //
				IConstants.FIRST_RESULT,//
				IConstants.MAX_RESULTS };
		String[] values = { //
		"wikiContext",//
				"Небесные создания", //
				Boolean.TRUE.toString(), //
				"0", //
				"10" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		logger.info("Query string : " + getMethod.getQueryString());
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		assertTrue("We should get something : " + result + ", " + actual, actual.length() > 0);
	}

	@Test
	@Ignore
	@Deprecated
	@SuppressWarnings("unused")
	public void formatToHtmlTable() throws SAXException, IOException {
		Searcher searcher = new Searcher();
		File file = FileUtilities.findFileRecursively(new File("."), "geospatial.results.xml");
		String xml = FileUtilities.getContents(file, Integer.MAX_VALUE).toString();
		String excluded = "score:countrycode:featureclass:modification:admin1code:asciiname:gtopo30:geonameid:featurecode:alternatenames";
//		String html = searcher.formatToHtmlTable(xml, excluded);
//		logger.info(html);
//
//		assertTrue(html.contains("<td>geoname id 18782822 18782822</td>"));
	}

}