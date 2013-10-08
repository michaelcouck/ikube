package ikube.security;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will set the authenticator for the system to get through a proxy if necessary. This requires that the system properties for the proxy are set
 * before this init method is called.
 * 
 * @author Michael Couck
 * @since 27.09.13
 * @version 01.00
 */
public class ProxyAuthenticator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProxyAuthenticator.class);

	/**
	 * Sets the default proxy settings for the system.
	 */
	public void initialize() {
		final String proxyUser = System.getProperty("http.proxyUser");
		final String proxyPassword = System.getProperty("http.proxyPassword");
		final String proxyPort = System.getProperty("http.proxyPort");
		final String proxyHost = System.getProperty("http.proxyHost");
		LOGGER.info("Proxy user : " + proxyUser + ", proxy port : " + proxyPort + ", proxy host : " + proxyHost);
		if (proxyUser != null && proxyPassword != null) {
			Authenticator.setDefault(new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
				}
			});
		}
	}

}
