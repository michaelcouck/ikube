package ikube.web;

import ikube.security.WebServiceAuthentication;

import org.apache.commons.httpclient.HttpClient;
import org.junit.BeforeClass;
import org.junit.Ignore;

@Ignore
public class Integration {

	protected static String LOCALHOST = "ikube.dyndns.org";
	/** This client({@link HttpClient}) is for the web services. */
	protected static HttpClient HTTP_CLIENT = new HttpClient();
	protected static int SERVER_PORT = 8080;
	protected static String REST_USER_NAME = "user";
	protected static String REST_PASSWORD = "user";

	/**
	 * Authentication for the web client.
	 * 
	 * @param client the client to authenticate with basic authentication
	 */
	@BeforeClass
	public static void beforeClass() {
		// We need to wait for the geospatial index to be created
		WebServiceAuthentication.authenticate(HTTP_CLIENT, LOCALHOST, SERVER_PORT, REST_USER_NAME, REST_PASSWORD);
	}

}
