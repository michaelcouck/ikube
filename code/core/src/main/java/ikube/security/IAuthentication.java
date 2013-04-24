package ikube.security;

import org.apache.commons.httpclient.HttpClient;

/**
 * This interface is for the various kinds of authentication that can be implemented by services, to mention a few basic, Kerberos, etc...
 * 
 * @author Michael Couck
 * @since 12.04.2013
 * @version 01.00
 */
public interface IAuthentication {

	/**
	 * This method will add headers to the client appropriate to the type of authentication.
	 * 
	 * @param httpClient the client to add the headers to
	 * @param properties the properties for the authentication. In the case of Kerberos for example one of the properties will be the token
	 */
	void authenticate(final HttpClient httpClient, final String... properties);

}
