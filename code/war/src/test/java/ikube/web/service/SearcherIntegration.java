package ikube.web.service;

import static ikube.toolkit.ObjectToolkit.populateFields;
import static org.junit.Assert.assertEquals;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.model.Search;

import java.io.IOException;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;

@Ignore
public abstract class SearcherIntegration extends BaseTest {

	public static final String SERVICE = "/service";
	protected static final String INDEX_NAME = "desktop";

	protected abstract String getUrl(String path) throws Exception;

	@Test
	public void searchSingle() throws Exception {
		String url = getUrl(SearcherXml.SINGLE);

		String[] names = { //
		IConstants.INDEX_NAME, //
				IConstants.SEARCH_STRINGS, //
				IConstants.SEARCH_FIELDS, //
				IConstants.FRAGMENT,//
				IConstants.FIRST_RESULT, //
				IConstants.MAX_RESULTS };
		String[] values = {//
		INDEX_NAME,//
				"cape AND town AND university",//
				IConstants.NAME, //
				Boolean.TRUE.toString(),//
				"0", //
				"10" };
		NameValuePair[] params = getNameValuePairs(names, values);
		verify(url, params);
	}

	@Test
	public void searchMulti() throws Exception {
		String url = getUrl(SearcherXml.SINGLE);

		String[] names = { //
		IConstants.INDEX_NAME, //
				IConstants.SEARCH_STRINGS, //
				IConstants.SEARCH_FIELDS, //
				IConstants.FRAGMENT,//
				IConstants.FIRST_RESULT, //
				IConstants.MAX_RESULTS };
		String[] values = { //
		INDEX_NAME, //
				"cape AND town AND university;south africa", //
				IConstants.NAME + ";" + IConstants.COUNTRY,//
				Boolean.TRUE.toString(), //
				"0", //
				"10" };
		NameValuePair[] params = getNameValuePairs(names, values);
		verify(url, params);
	}

	@Test
	public void searchMultiSorted() throws Exception {
		String url = getUrl(SearcherXml.SORTED);

		String[] names = { //
		IConstants.INDEX_NAME, //
				IConstants.SEARCH_STRINGS, //
				IConstants.SEARCH_FIELDS, //
				IConstants.SORT_FIELDS,//
				IConstants.FRAGMENT, //
				IConstants.FIRST_RESULT, //
				IConstants.MAX_RESULTS };
		String[] values = { //
		INDEX_NAME, //
				"cape AND town AND university;south africa", //
				IConstants.NAME + ";" + IConstants.COUNTRY,//
				IConstants.NAME + ";" + IConstants.COUNTRY, //
				Boolean.TRUE.toString(), //
				"0", //
				"10" };
		NameValuePair[] params = getNameValuePairs(names, values);
		verify(url, params);
	}

	@Test
	public void searchMultiSpacial() throws Exception {
		String url = getUrl(SearcherXml.GEOSPATIAL);

		String[] names = { //
		IConstants.INDEX_NAME, //
				IConstants.SEARCH_STRINGS, //
				IConstants.SEARCH_FIELDS, //
				IConstants.TYPE_FIELDS, //
				IConstants.FRAGMENT, //
				IConstants.FIRST_RESULT, //
				IConstants.MAX_RESULTS, //
				IConstants.DISTANCE, //
				IConstants.LATITUDE, //
				IConstants.LONGITUDE };
		String[] values = { //
		IConstants.GEOSPATIAL, //
				"cape town university|berlin", //
				"name|name", //
				"string|string", //
				Boolean.TRUE.toString(), //
				"0", //
				"10", //
				"20", //
				"-33.9693580", //
				"18.4622110" };
		NameValuePair[] params = getNameValuePairs(names, values);
		verify(url, params);
	}

	@Test
	public void searchComplex() throws Exception {
		String url = getUrl(SearcherXml.SORTED_TYPED);

		String[] names = { //
		IConstants.INDEX_NAME, //
				IConstants.SEARCH_STRINGS, //
				IConstants.SEARCH_FIELDS, //
				IConstants.TYPE_FIELDS, //
				IConstants.SORT_FIELDS, //
				IConstants.FRAGMENT, //
				IConstants.FIRST_RESULT,//
				IConstants.MAX_RESULTS };
		String[] values = { //
		INDEX_NAME,//
				"michael AND couck|123456789", //
				"contents|lastmodified", //
				"string|numeric", //
				"lastmodified", //
				Boolean.TRUE.toString(), //
				"0", //
				"10" };
		NameValuePair[] params = getNameValuePairs(names, values);
		verify(url, params);
	}

	@Test
	public void search() throws Exception {
		verify(getUrl(""));
	}

	@Test
	public void searchAll() throws Exception {
		verify(getUrl(Searcher.ALL));
	}

	protected void verify(final String url) throws Exception {
		PostMethod postMethod = new PostMethod(url);

		Search search = populateFields(new Search(), Boolean.TRUE, 10);
		search.setIndexName(INDEX_NAME);

		search.setSearchStrings(Arrays.asList("Michael Couck"));
		search.setSearchFields(Arrays.asList("contents"));
		search.setTypeFields(Arrays.asList("string"));
		search.setSortFields(Arrays.asList("lastmodified"));

		search.setFirstResult(0);
		search.setMaxResults(10);
		search.setFragment(Boolean.TRUE);

		Gson gson = new Gson();
		String content = gson.toJson(search);
		StringRequestEntity stringRequestEntity = new StringRequestEntity(content, MediaType.APPLICATION_JSON, IConstants.ENCODING);
		postMethod.setRequestEntity(stringRequestEntity);

		HTTP_CLIENT.executeMethod(postMethod);
		// logger.info("Response : " + postMethod.getResponseBodyAsString());
		assertEquals(200, postMethod.getStatusCode());
	}

	protected void verify(final String url, final NameValuePair[] params) throws HttpException, IOException {
		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		HTTP_CLIENT.executeMethod(getMethod);
		assertEquals(200, getMethod.getStatusCode());
	}

	void print(final String url, final GetMethod getMethod) {
		logger.info("Url : " + url);
		logger.info("Query : " + getMethod.getQueryString());
	}

}