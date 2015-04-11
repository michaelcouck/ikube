package ikube.security;

import org.apache.http.client.HttpClient;
import org.springframework.stereotype.Service;

/**
 * This interface is for the various kinds of authentication that
 * can be implemented by services, to mention a few basic, Kerberos, etc...
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-04-2013
 */
@Service
public interface IAuthentication {

	/**
	 * This method will add headers to the client appropriate to the type of authentication.
	 *
	 * @param httpClient the client to add the headers to
	 * @param username   the name of the authorised user
	 * @param password the password of the user
	 */
	void authenticate(final HttpClient httpClient, final String url, final int port, final String username, final String password);

}
