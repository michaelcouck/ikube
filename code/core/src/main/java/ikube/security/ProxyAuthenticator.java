package ikube.security;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * This class will set the authenticator for the system to get through a proxy if necessary. This requires that the system properties for the proxy are set
 * before this init method is called.
 * 
 * @author Michael Couck
 * @since 27.09.13
 * @version 01.00
 */
public class ProxyAuthenticator {

	/**
	 * Sets the default proxy settings for the system.
	 */
	public void initialize() {
		final String proxyUser = System.getProperty("http.proxyUser");
		final String proxyPassword = System.getProperty("http.proxyPassword");
		if (proxyUser != null && proxyPassword != null) {
			Authenticator.setDefault(new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
				}
			});
		}
	}

}
