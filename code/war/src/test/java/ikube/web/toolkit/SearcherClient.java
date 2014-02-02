package ikube.web.toolkit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * This is an example in Java of how to access the rest web service to get results from the GeoSpatial index.
 * 
 * @author Michael Couck
 * @since 03.03.12
 * @version 01.00
 */
public class SearcherClient {

	public static void main(String[] args) throws Exception {
		String host = "localhost";
		int port = 8080;
		String path = "/ikube/service/search/xml/geospatial";
		String url = new URL("http", host, port , path).toString();

		String[] names = { "indexName", "searchStrings", "searchFields", "typeFields", "fragment", "firstResult", "maxResults", "distance", "latitude", "longitude" };
		String[] values = { "geospatial", "cape town", "name", "string", "true", "0", "10", "10", "-33.9693580", "18.4622110" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		HttpClient httpClient = new HttpClient();
		authenticate(httpClient, host, port, "guest", "guest");

		int result = httpClient.executeMethod(getMethod);
		String results = getMethod.getResponseBodyAsString();
		System.out.println("Result : " + result);
		System.out.println("Results : " + results);
	}

	public static void authenticate(final HttpClient httpClient, final String domain, final int port, final String userid, final String password) {
		List<String> authPrefs = new ArrayList<>(2);
		authPrefs.add(AuthPolicy.BASIC);
		authPrefs.add(AuthPolicy.DIGEST);
		httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
		httpClient.getParams().setAuthenticationPreemptive(true);
		AuthScope authScope = new AuthScope(domain, port, AuthScope.ANY_REALM);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userid, password);
		httpClient.getState().setCredentials(authScope, credentials);
	}

	private static NameValuePair[] getNameValuePairs(String[] names, String[] values) {
		List<NameValuePair> nameValuePairs = new ArrayList<>();
		for (int i = 0; i < names.length && i < values.length; i++) {
			NameValuePair nameValuePair = new NameValuePair(names[i], values[i]);
			nameValuePairs.add(nameValuePair);
		}
		return nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]);
	}

}