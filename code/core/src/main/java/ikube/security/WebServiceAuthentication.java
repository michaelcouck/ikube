package ikube.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;

public class WebServiceAuthentication {

	public static void authenticate(final HttpClient httpClient, final String domain, final int port, final String userid,
			final String password) {
		List<String> authPrefs = new ArrayList<String>(2);
		authPrefs.add(AuthPolicy.BASIC);
		authPrefs.add(AuthPolicy.DIGEST);
		httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
		httpClient.getParams().setAuthenticationPreemptive(true);
		AuthScope authScope = new AuthScope(domain, port, AuthScope.ANY_REALM);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userid, password);
		httpClient.getState().setCredentials(authScope, credentials);
	}

}
