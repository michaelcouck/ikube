package ikube.web;

import ikube.IConstants;
import ikube.security.WebServiceAuthentication;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.web.service.Searcher;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public abstract class Integration {

	protected static String LOCALHOST = "localhost";
	/** This client({@link HttpClient}) is for the web services. */
	protected static HttpClient HTTP_CLIENT = new HttpClient();
	protected static int SERVER_PORT = 9080;
	protected static String REST_USER_NAME = "user";
	protected static String REST_PASSWORD = "user";

	private static final Logger LOGGER = LoggerFactory.getLogger(Integration.class);

	/**
	 * Authentication for the web client.
	 * 
	 * @param client the client to authenticate with basic authentication
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		// We need to wait for the geospatial index to be created
		WebServiceAuthentication.authenticate(HTTP_CLIENT, LOCALHOST, SERVER_PORT, REST_USER_NAME, REST_PASSWORD);
		// We'll wait here for the integration indexes to be ready
		while (indexesInitialized()) {
			LOGGER.info("Waiting : ");
			// Thread.sleep(60000);
		}
		ThreadUtilities.initialize();
	}

	@SuppressWarnings("unchecked")
	private static final boolean indexesInitialized() throws Exception {
		// String, String, String, boolean, int, int
		String path = IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.SINGLE;
		String url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();

		String[] names = { IConstants.INDEX_NAME, IConstants.SEARCH_STRINGS, IConstants.SEARCH_FIELDS, IConstants.FRAGMENT,
				IConstants.FIRST_RESULT, IConstants.MAX_RESULTS };
		String[] values = { IConstants.GEOSPATIAL, "cape AND town AND university", IConstants.NAME, Boolean.TRUE.toString(), "0", "10" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		List<Map<String, String>> resultsList = (List<Map<String, String>>) SerializationUtilities.deserialize(actual);
		LOGGER.info("Result : " + result + ", " + resultsList.size());

		return resultsList.size() > 1;
	}

	protected static NameValuePair[] getNameValuePairs(String[] names, String[] values) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (int i = 0; i < names.length && i < values.length; i++) {
			NameValuePair nameValuePair = new NameValuePair(names[i], values[i]);
			nameValuePairs.add(nameValuePair);
		}
		return nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]);
	}

}