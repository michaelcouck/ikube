package ikube;

import ikube.toolkit.Logging;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Couck
 * @since 18.06.13
 * @version 01.00
 */
@Ignore
public abstract class AbstractTest {

	static {
		Logging.configure();
		System.setProperty("username", "username");
		System.setProperty("password", "password");

		System.setProperty("http.proxyHost", "proxy.post.bpgnet.net");
		System.setProperty("http.proxyPort", "8080");
		System.setProperty("http.proxyUser", "proxyUser");
		System.setProperty("http.proxyPassword", "proxyPassword");

		System.setProperty("https.proxyHost", "proxy.post.bpgnet.net");
		System.setProperty("https.proxyPort", "8080");
		System.setProperty("https.proxyUser", "proxyUser");
		System.setProperty("https.proxyPassword", "proxyPassword");
	}

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

}