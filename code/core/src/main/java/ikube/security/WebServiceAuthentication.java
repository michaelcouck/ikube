package ikube.security;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;

/**
 * This class will add basic and digest authentication schemes to
 * the http headers of the client for logging in to the web application.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-12-2012
 */
public class WebServiceAuthentication implements IAuthentication {

	@Override
	public void authenticate(final HttpClient httpClient, final String url, final int port, final String username, final String password) {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
				new AuthScope(url, port),
				new UsernamePasswordCredentials(username, password));
		((AbstractHttpClient) httpClient).setCredentialsProvider(credsProvider);
	}

}