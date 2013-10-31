package ikube.web.service;

import ikube.IConstants;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;

/**
 * This is an example in Java of how to access the rest web service to get results from the GeoSpatial index.
 * 
 * @author Michael Couck
 * @since 03.03.12
 * @version 01.00
 */
public class SearcherClientIntegration {

	@Test
	public void main() throws Exception {

		StringBuilder builder = new StringBuilder();
		// http://localhost:9090/ikube/service/search/xml/geospatial
		builder.append("/ikube");
		builder.append("/service");
		builder.append("/search");
		builder.append("/xml");
		builder.append("/geospatial");
		URL url = new URL("http", "localhost", 9090, builder.toString());
		System.out.println(url);

		String[] names = { //
		"indexName", //
				"searchStrings", //
				"searchFields", //
				"typeFields", //
				"fragment", //
				"firstResult", //
				"maxResults", //
				"distance", //
				"latitude", //
				"longitude" };
		String[] values = { //
		IConstants.GEOSPATIAL, //
				"saur kraut|berlin", //
				"name|name", //
				"string|string", //
				"true", //
				"0", //
				"10", //
				"20", //
				"-33.9693580", //
				"18.4622110" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url.toString());
		getMethod.setQueryString(params);
		HttpClient httpClient = new HttpClient();
		authenticate(httpClient, url.getHost(), url.getPort(), "guest", "guest");

		int result = httpClient.executeMethod(getMethod);
		getMethod.getResponseBodyAsString();
		assert result == 200;
		assert getMethod.getStatusCode() == 200;
	}

	public void authenticate(final HttpClient httpClient, final String domain, final int port, final String userid, final String password) {
		List<String> authPrefs = new ArrayList<String>(2);
		authPrefs.add(AuthPolicy.BASIC);
		authPrefs.add(AuthPolicy.DIGEST);
		httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
		httpClient.getParams().setAuthenticationPreemptive(true);
		AuthScope authScope = new AuthScope(domain, port, AuthScope.ANY_REALM);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userid, password);
		httpClient.getState().setCredentials(authScope, credentials);
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