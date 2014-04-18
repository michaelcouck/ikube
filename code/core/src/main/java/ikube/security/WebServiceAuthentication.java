package ikube.security;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;

import java.util.ArrayList;
import java.util.List;

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
	public void authenticate(final HttpClient httpClient, final String... properties) {
		List<String> authPrefs = new ArrayList<>(2);
		authPrefs.add(AuthPolicy.BASIC);
		authPrefs.add(AuthPolicy.DIGEST);
		httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
		httpClient.getParams().setAuthenticationPreemptive(true);
		AuthScope authScope = new AuthScope(properties[0], Integer.valueOf(properties[1]), AuthScope.ANY_REALM);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(properties[2], properties[3]);
		httpClient.getState().setCredentials(authScope, credentials);
	}

}