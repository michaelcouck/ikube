package ikube.toolkit;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

/**
 * Generates spnego/kerberos tokens.
 * 
 * You need to specify the following system properties to generate tokens:
 * 
 * -Djava.security.krb5.realm=POST.BPGNET.NET
 * -Djava.security.krb5.kdc=swpr052.post.bpgnet.net
 * 
 * In addition, a JAAS configuration is needed.
 * 
 * To use this class in a standalone java app, provide a system property:
 * -Djava.security.auth.login.config=jaas.conf
 * 
 * where the jaas.conf file contains your jaas configuration
 * 
 * To use this class in jboss, provide in login-config.xml:
 * 
 * <application-policy name="KerberosClient"> 
 * 	<authentication> 
 * 		<login-module code="com.sun.security.auth.module.Krb5LoginModule" flag="required">
 * 			<module-option ... > </module-option>
 * 			...
 * 		</login-module>
 * 	</authentication>
 * </application-policy>
 * 
 * Have a look at different jaas configurations provided in the javadoc of the generateToken(...) methods.
 * 
 * KerberosClient is the default name, you can use another if needed.
 * 
 */
public class SpnegoTokenGenerator {
	private static final Logger LOGGER = Logger
			.getLogger(SpnegoTokenGenerator.class);

	/** Spnego oid, available since java 6 */
	private static final String SPNEGO_OID = "1.3.6.1.5.5.2";

	/** Jaas configuration name, default is "KerberosClient" */
	private String jaasConfigurationName = "KerberosClient";

	/** Default true, reusing jaas login context */
	private boolean reuseableJaasLoginContext = true;

	/**
	 * GSSContext options
	 */
	private boolean mutualAuthentication = false;
	private boolean credentialDelegation = false;
	private boolean confidentiality = false;
	private boolean integrety = false;

	/**
	 * Jaas login context.
	 */
	private LoginContext jaasLoginContext;

	/** Default constructor - login context is not reused */
	public SpnegoTokenGenerator() {
	}

	/**
	 * Creates a SpnegoTokenGenerator. If only 1 userName is used throughout the
	 * entire application, you can reuse the login context. 
	 * 
	 * @param reuseableLoginContext default is true
	 */
	public SpnegoTokenGenerator(boolean reuseableJaasLoginContext) {
		this.reuseableJaasLoginContext = reuseableJaasLoginContext;
	}

	/**
	 * Creates a spnego token for the given server principal name. 
	 * This method can be used with a jaas configuration:
	 * 1) if the user credentials come from a ticket cache. (e.g.: windows logon credentials).  
	 *
	 *     KrbTicketCache {
   	 *	com.sun.security.auth.module.Krb5LoginModule required 
    	 *		useTicketCache="true" 
	 *	};
	 *
	 * 2) If you use a keytab file with a client principal (as user account, 'u342223@post.bpgnet.net').  
	 *
	 *     KrbPrincipal {
   	 *	com.sun.security.auth.module.Krb5LoginModule required 
   	 *		storeKey="true" 
   	 *		useKeyTab="true" 
   	 *		principal="u342223@POST.BPGNET.NET" 
	 *		keyTab="C:/winnt/jaas.krb"
	 *		doNotPrompt="true"
	 *	};
	 *
	 * 3) If you use a keytab file with a client principal (as service principal, 'HTTP/aliasw229984.post.bpgnet.net').
	 * 
	 * 	KrbServicePrincipal {
   	 *	 com.sun.security.auth.module.Krb5LoginModule required 
   	 *		storeKey="true" 
   	 *		useKeyTab="true" 
   	 *		isInitiator="false"
   	 *		principal="HTTP/aliasw229984.post.bpgnet.net" 
	 *		keyTab="C:/winnt/jaas.krb"
	 *		doNotPrompt="true"
	 *	};
	 * 
	 * @param serverPrincipalName
	 *            the server principal name e.g.: HTTP/myserver.post.bpgnet.net
	 * @return SpnegoTokenGeneratorResponse
	 * @throws GSSException
	 * @throws LoginException
	 * @throws PrivilegedActionException
	 */
	public SpnegoTokenGeneratorResponse generateToken(String serverPrincipalName)
			throws SpnegoTokenGeneratorException {

		checkSPNParameter(serverPrincipalName);

		return performTokenGeneration(serverPrincipalName, null, null);
	}

	/**
	 * Creates a spnego token for the given server principal name and client principal name.
	 * This method can be used with a jaas configuration:
	 * 1) if you only know the clientPrincipalName (as user account, 'u342223@post.bpgnet.net') at runtime
	 *
	 * 	KrbNoPrincipal {
   	 *	 com.sun.security.auth.module.Krb5LoginModule required 
   	 *		storeKey="true" 
   	 *		useKeyTab="true" 
	 *		keyTab="C:/winnt/jaas.krb"
	 *		doNotPrompt="false"
	 *	};
	 *
	 * 2) If you only know the clientPrincipalName (as service principal, 'HTTP/aliasw229984.post.bpgnet.net') at runtime	
	 *
	 *	KrbNoServicePrincipal {
   	 *	 com.sun.security.auth.module.Krb5LoginModule required 
   	 *		storeKey="true" 
   	 *		useKeyTab="true" 
   	 *		isInitiator="false"
	 *		keyTab="C:/winnt/jaas.krb"
	 *		doNotPrompt="false"
	 *	};
	 * 
	 * Remark:
	 * If you have multiple client principal names at runtime, please set the reuseableJaasLoginContext property of this class to false.  
	 *
	 * @param serverPrincipalName
	 *            the server principal name e.g.: HTTP/myserver.post.bpgnet.net
	 * @param clientPrincipalName
	 *            the client principal name e.g.: u123456@POST.BPGNET.NET
	 * @return SpnegoTokenGeneratorResponse
	 * @throws GSSException
	 * @throws LoginException
	 * @throws PrivilegedActionException
	 */
	public SpnegoTokenGeneratorResponse generateToken(String serverPrincipalName,
			String clientPrincipalName) throws SpnegoTokenGeneratorException {

		checkParameters(serverPrincipalName, clientPrincipalName);

		return performTokenGeneration(serverPrincipalName, clientPrincipalName, null);
	}
	
	/**
	 * Creates a spnego token for the given server principal name, client principal name and client principal password.  
	 * This method can be used with a jaas configuration:
	 * 
	 * 1) If you only know the clientPrincipalName (as user account, 'u342223@post.bpgnet.net') and clientPrincipalPassword at runtime 
	 *
	 * 	KrbClientNoPrincipalNoPassword {
   	 *	 com.sun.security.auth.module.Krb5LoginModule required 
   	 *		storeKey="true" 
	 *		doNotPrompt="false"
	 *	};
	 *
	 * 2) If you only know the clientPrincipalName (as service principal, 'HTTP/aliasw229984.post.bpgnet.net') and clientPrincipalPassword at runtime
	 * 
	 * 	KrbClientNoServicePrincipalNoPassword {
   	 *    	 com.sun.security.auth.module.Krb5LoginModule required 
   	 *		storeKey="true" 
   	 *		isInitiator="false"
	 *		doNotPrompt="false"
	 *	};
	 * 
	 * Remark:
	 * If you have multiple client principal names at runtime, please set the reuseableJaasLoginContext property of this class to false.  
	 * 
	 * @param serverPrincipalName
	 *            the service principal e.g.: HTTP/myserver.post.bpgnet.net
	 * @param clientPrincipalName
	 *            the userName e.g.: u123456@POST.BPGNET.NET
	 * @return spnego token
	 * @throws GSSException
	 * @throws LoginException
	 * @throws PrivilegedActionException
	 */
	public SpnegoTokenGeneratorResponse generateToken(String serverPrincipalName,
			String clientPrincipalName, char[] clientPrincipalPassword) throws SpnegoTokenGeneratorException {

		checkParameters(serverPrincipalName, clientPrincipalName);

		return performTokenGeneration(serverPrincipalName, clientPrincipalName, clientPrincipalPassword);
	}



	private SpnegoTokenGeneratorResponse performTokenGeneration(
			String servicePrincipal, String userName, char[] password) {
		byte[] spnegotoken = null;
		GSSContext context = null;

		try {
			performJaasLogin(userName, password);
			context = createGssContext(servicePrincipal);

			final byte[] token = new byte[0];
			spnegotoken = (byte[]) Subject.doAs(jaasLoginContext.getSubject(),
					new CreateServiceTicketAction(context, token));

		} catch (GSSException e) {
			handleError(e);
		} catch (PrivilegedActionException e) {
			handleError(e);
		} catch (LoginException e) {
			handleError(e);
		}

		SpnegoTokenGeneratorResponse response = new SpnegoTokenGeneratorResponse();
		response.setContext(context);
		response.setToken(spnegotoken);

		return response;
	}

	private GSSContext createGssContext(String servicePrincipal)
			throws GSSException {
		GSSContext context;
		Oid oid = new Oid(SPNEGO_OID);

		GSSManager manager = GSSManager.getInstance();
		GSSName serverName = manager.createName(servicePrincipal, null);

		context = manager.createContext(serverName.canonicalize(oid), oid,
				null, GSSContext.DEFAULT_LIFETIME);

		context.requestMutualAuth(mutualAuthentication); // Mutual authentication
		context.requestCredDeleg(credentialDelegation); // credential delegation
		context.requestConf(confidentiality); // Will use confidentiality later
		context.requestInteg(integrety); // Will use integrity later
		return context;
	}

	private void performJaasLogin(String userName, char[] password) throws LoginException {
		if (jaasLoginContext == null || !reuseableJaasLoginContext) {
			if (userName == null) {
				jaasLoginContext = new LoginContext(getJaasConfigurationName());
			} else{
				jaasLoginContext = new LoginContext(getJaasConfigurationName(),
						getUsernamePasswordHandler(userName, password));
			}

			jaasLoginContext.login();
		}
	}

	private void handleError(Exception e) {
		LOGGER.error("Error while generating spnego token! ", e);
		throw new SpnegoTokenGeneratorException(
				"Error while generating spnego token! ", e);
	}

	private void checkParameters(String servicePrincipal, String userName) {
		String msg = "";
		msg += checkSPNParameter(servicePrincipal);

		if (!"".equals(msg)) {
			throw new SpnegoTokenGeneratorException(msg);
		}
	}

	private String checkSPNParameter(String servicePrincipal) {
		if (servicePrincipal == null || "".equals(servicePrincipal)) {
			return "Please provide a service principal (not null or empty).  ";
		} else {
			return "";
		}
	}

	public String getJaasConfigurationName() {
		return jaasConfigurationName;
	}

	public void setJaasConfigurationName(String jaasConfigurationName) {
		this.jaasConfigurationName = jaasConfigurationName;
	}

	public static CallbackHandler getUsernamePasswordHandler(
			final String username, final char[] password) {
		final CallbackHandler handler = new CallbackHandler() {

			public void handle(final Callback[] callback) {
				for (int i = 0; i < callback.length; i++) {
					if (callback[i] instanceof NameCallback) {
						final NameCallback nameCallback = (NameCallback) callback[i];
						nameCallback.setName(username);
					} else if (callback[i] instanceof PasswordCallback) {
						final PasswordCallback passCallback = (PasswordCallback) callback[i];
						passCallback.setPassword(password);
					}
				}
			}
		};
		return handler;
	}

	private final class CreateServiceTicketAction implements
			PrivilegedExceptionAction<byte[]> {
		private final GSSContext context;
		private final byte[] token;

		private CreateServiceTicketAction(GSSContext context, byte[] token) {
			this.context = context;
			this.token = token;
		}

		public byte[] run() throws GSSException {
			return context.initSecContext(token, 0, token.length);
		}
	}

	public boolean isReusableJaasLoginContext() {
		return reuseableJaasLoginContext;
	}

	public void setReusableJaasLoginContext(boolean reuseableJaasLoginContext) {
		this.reuseableJaasLoginContext = reuseableJaasLoginContext;
	}

	public boolean isReuseableJaasLoginContext() {
		return reuseableJaasLoginContext;
	}

	public void setReuseableJaasLoginContext(boolean reuseableJaasLoginContext) {
		this.reuseableJaasLoginContext = reuseableJaasLoginContext;
	}

	public boolean isMutualAuthentication() {
		return mutualAuthentication;
	}

	public void setMutualAuthentication(boolean mutualAuthentication) {
		this.mutualAuthentication = mutualAuthentication;
	}

	public boolean isCredentialDelegation() {
		return credentialDelegation;
	}

	public void setCredentialDelegation(boolean credentialDelegation) {
		this.credentialDelegation = credentialDelegation;
	}

	public boolean isConfidentiality() {
		return confidentiality;
	}

	public void setConfidentiality(boolean confidentiality) {
		this.confidentiality = confidentiality;
	}

	public boolean isIntegrety() {
		return integrety;
	}

	public void setIntegrety(boolean integrety) {
		this.integrety = integrety;
	}

	public LoginContext getJaasLoginContext() {
		return jaasLoginContext;
	}

	public void setJaasLoginContext(LoginContext jaasLoginContext) {
		this.jaasLoginContext = jaasLoginContext;
	}

}
