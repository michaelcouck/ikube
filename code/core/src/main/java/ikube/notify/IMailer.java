package ikube.notify;

/**
 * This interface is for mailing logic.
 * 
 * @author Michael Couck
 * @since 18.07.11
 * @version 01.00
 */
public interface IMailer {

	/**
	 * @param port
	 *            sets the port
	 */
	void setPort(final String port);

	/**
	 * @param auth
	 *            sets the authorization token for the connection, true or false, generally this should be true
	 */
	void setAuth(final String auth);

	/**
	 * @param protocol
	 *            sets the protocol, which typically is pop or imap
	 */
	void setProtocol(final String protocol);

	/**
	 * @param recipients
	 *            sets the recipients, i.e. a list of email addresses
	 */
	void setRecipients(final String recipients);

	/**
	 * @param sender
	 *            sets the sender of this message, i.e. the email address where this is coming from, could be any email address
	 */
	void setSender(final String sender);

	/**
	 * @param password
	 *            sets the password for the account where the message will be sent from. This is the Gmail account for example
	 */
	void setPassword(final String password);

	/**
	 * @param user
	 *            sets the user of the account where the message sill be sent from
	 */
	void setUser(final String user);

	/**
	 * @param mailHost
	 *            sets the mail host which is the url to the account, something like pop.gmail.com
	 */
	void setMailHost(final String mailHost);

	/**
	 * This method is the implementation of the mailing functionality. Implementors need to take the parameters that were set for the
	 * account, make a connection to the mail server and send the message to the recipients defined.
	 * 
	 * @param subject
	 *            the subject of the message, this is the 'title' of the message
	 * @param body
	 *            the body of the message, i.e. the text of the message
	 * @throws Exception
	 *             if anything goes wrong during the transmission
	 */
	boolean sendMail(final String subject, final String body) throws Exception;

}
