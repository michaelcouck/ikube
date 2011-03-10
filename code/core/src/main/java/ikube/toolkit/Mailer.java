package ikube.toolkit;

import java.security.Security;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

/**
 * This class actually does the sending of the mail to the recipients when the license agreement maximum documents is exceeded. The
 * recipients are in an encrypted file in the application, and the {@link License} object holds the decrypted data from the file.
 * 
 * @author Michael Couck
 * @since 16.08.10
 * @version 01.00
 */
@SuppressWarnings("restriction")
public class Mailer {

	static {
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	}

	private Logger logger = Logger.getLogger(this.getClass());
	/** "smtp.gmail.com" */
	private transient String mailHost;
	private transient String user;
	private transient String password;
	private transient String sender;
	private transient String recipients;
	/** "smtp" */
	private transient String protocol;
	/** "true" */
	private transient String auth;
	/** "465" */
	private transient String port;

	public void sendMail(final String subject, final String body) throws Exception {
		Properties properties = System.getProperties();
		properties.put("mail.transport.protocol", protocol);
		properties.put("mail.host", mailHost);
		properties.put("mail.smtp.auth", auth);
		properties.put("mail.smtp.port", port);
		properties.put("mail.smtp.socketFactory.port", port);
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.socketFactory.fallback", "false");
		properties.put("mail.smtp.quitwait", "false");

		Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, password);
			}
		});
		// session.setDebug(Boolean.TRUE);

		MimeMessage message = new MimeMessage(session);
		message.setSender(new InternetAddress(sender));
		message.setSubject(subject);
		message.setContent(body, "text/plain");
		if (recipients.indexOf(',') > 0) {
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
		} else {
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
		}

		// Transport.send(message);
		Transport transport = null;
		try {
			transport = session.getTransport("smtps");
			transport.connect(mailHost, Integer.parseInt(port), user, password);
			transport.sendMessage(message, message.getAllRecipients());
		} catch (Exception e) {
			logger.error("Exception sending mail to : " + ToStringBuilder.reflectionToString(this), e);
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (Exception e) {
					logger.error("Exception closing the mail transport : " + transport, e);
				}
			}
		}
	}

	public void setMailHost(final String mailHost) {
		this.mailHost = mailHost;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public void setSender(final String sender) {
		this.sender = sender;
	}

	public void setRecipients(final String recipients) {
		this.recipients = recipients;
	}

	public void setProtocol(final String protocol) {
		this.protocol = protocol;
	}

	public void setAuth(final String auth) {
		this.auth = auth;
	}

	public void setPort(final String port) {
		this.port = port;
	}

}