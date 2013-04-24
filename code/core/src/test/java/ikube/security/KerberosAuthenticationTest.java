package ikube.security;

import ikube.AbstractTest;

import org.apache.commons.httpclient.HttpClient;
import org.junit.Before;
import org.junit.Test;

public class KerberosAuthenticationTest extends AbstractTest {

	@SuppressWarnings("unused")
	private HttpClient httpClient;

	public KerberosAuthenticationTest() {
		super(KerberosAuthenticationTest.class);
	}

	@Before
	public void before() {
		httpClient = new HttpClient();
	}

	@Test
	public void authenticate() {
		// TODO For this test we have to upgrade to the 4.1 version from 3.1
		// new KerberosAuthentication().authenticate(httpClient, properties);
	}

}
