package ikube.security;

import ikube.AbstractTest;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.junit.Before;
import org.junit.Test;

public class KerberosAuthenticationTest extends AbstractTest {

	@SuppressWarnings("unused")
	private HttpClient httpClient;

	@Before
	public void before() {
		httpClient = new AutoRetryHttpClient();
	}

	@Test
	public void authenticate() {
		// TODO For this test we have to upgrade to the 4.1 version from 3.1
		// new KerberosAuthentication().authenticate(httpClient, properties);
	}

}
