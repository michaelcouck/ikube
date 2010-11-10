package ikube.index.visitor.email;

import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.index.visitor.IndexableVisitor;
import ikube.model.IndexableEmail;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

import com.sun.mail.pop3.POP3SSLStore;

/**
 * This class is responsible to visit a mail server and return the the message content to its parent who will index it.
 *
 * @author Bruno Barin
 * @since 17/08/2010
 */
public class IndexableEmailVisitor<I> extends IndexableVisitor<IndexableEmail> {

	static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
	static final String MAIL_PROTOCOL = "pop3";

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void visit(IndexableEmail indexableMail) {
		javax.mail.Store store = null;
		try {
			store = getStore(indexableMail);
			store.connect();
		} catch (MessagingException e) {
			final StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Could not connect to the mail server:");
			stringBuilder.append(indexableMail.getMailHost());
			stringBuilder.append(" port:");
			stringBuilder.append(indexableMail.getPort());
			logger.error(stringBuilder, e);
			if (logger.isDebugEnabled()) {
				logger.debug(e.getStackTrace());
			}
			return;
		}

		try {
			Folder folder = store.getFolder("inbox");
			folder.open(Folder.READ_ONLY);

			// For each message found in the server, index it.
			logger.error("Message count : " + folder.getMessageCount());
			for (Message message : folder.getMessages()) {
				// Builds the identifier
				Date recievedDate = message.getReceivedDate();
				Date sentDate = message.getSentDate();
				int messageNumber = message.getMessageNumber();
				long timestamp = recievedDate != null ? recievedDate.getTime() : sentDate != null ? sentDate.getTime() : 0l;

				logger.info("Recieved : " + recievedDate);
				logger.info("Sent : " + sentDate);
				logger.info("Message number : " + messageNumber);
				logger.info("Timestamp : " + timestamp);

				StringBuilder builder = new StringBuilder(indexableMail.getMailHost()).append(".").append(indexableMail.getUsername())
						.append(".").append(messageNumber).append(".").append(timestamp);

				Store mustStore = indexableMail.isStored() ? Store.YES : Store.NO;
				Index analyzed = indexableMail.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
				TermVector termVector = indexableMail.isVectored() ? TermVector.YES : TermVector.NO;

				Document document = new Document();
				// Add the id field to the document
				addStringField(indexableMail.getIdField(), builder.toString(), document, mustStore, analyzed, termVector);
				// Add the title field to the document
				addStringField(indexableMail.getTitleField(), message.getSubject(), document, mustStore, analyzed, termVector);
				String messageContent = getMessageContent(message);
				if (StringUtils.isNotEmpty(messageContent)) {
					byte[] bytes = messageContent.getBytes();
					IParser parser = ParserProvider.getParser(message.getContentType(), bytes);
					String parsedMessageContent = parser.parse(messageContent);
					// Add the content field to the document
					addStringField(indexableMail.getContentField(), parsedMessageContent, document, mustStore, analyzed, termVector);
				}
			}
			folder.close(true);
		} catch (MessagingException e) {
			logger.error("The inbox folder does not exist or is not available");
			if (logger.isDebugEnabled()) {
				logger.debug(e.getStackTrace());
			}
		} catch (IOException e) {
			logger.error("Could not retrieve the message content");
			if (logger.isDebugEnabled()) {
				logger.debug(e.getStackTrace());
			}
		} catch (Exception e) {
			logger.error("General exception parsing the message", e);
		}

		closeMailServerConnection(store);
	}

	/**
	 * Closes the connection to the mail server
	 *
	 * @param store
	 *            The Store object that holds the connection to the mail server.
	 */
	private void closeMailServerConnection(final javax.mail.Store store) {
		try {
			store.close();
		} catch (MessagingException e) {
			logger.error("Could not close the connetion to the mail server in a graceful mode");
			if (logger.isDebugEnabled()) {
				logger.debug(e.getStackTrace());
			}
		}
	}

	/**
	 * Returns a message content given a Message object
	 *
	 * @param message
	 *            The Message object representing a message in the mail server
	 * @return The message content.
	 * @throws IOException
	 *             If some problem occurs when trying to access the message content.
	 * @throws MessagingException
	 *             If some problem occurs when trying to access the message content.
	 */
	private String getMessageContent(Message message) throws IOException, MessagingException {
		String messageContent = null;
		Object obj = message.getContent();
		if (obj.getClass().isAssignableFrom(Multipart.class)) {
			if (logger.isDebugEnabled()) {
				// TODO - we need to parse the attachments and add them to the accumulator
				// Get the data. Get the name of the attachment
				// IParser parser = ParserFactory.getParserFactory().getParser("text/html", bytes);
				logger.info("Skiping attachment");
			}
		} else {
			if (obj.getClass().isAssignableFrom(MimeMultipart.class)) {
				MimeMultipart mimeMultipart = (MimeMultipart) obj;
				BodyPart bodyPart = mimeMultipart.getBodyPart(0);
				Object content = bodyPart.getContent();
				if (String.class.isAssignableFrom(content.getClass())) {
					messageContent = (String) content;
				} else {
					// TODO - what am I
					logger.info("What is this content : " + content);
				}
			} else {
				if (obj.getClass().isAssignableFrom(String.class)) {
					messageContent = (String) obj;
				}
			}

		}
		return messageContent;
	}

	/**
	 * Returns a connection to the mail server given a Mail visitable
	 *
	 * @param mailVisitable
	 *            The {@link IndexableEmail} indexable object
	 * @return The {@link Store} object that holds a connection to the mail server.
	 * @throws NoSuchProviderException
	 *             If the mail provider wasn't correct specified.
	 */
	private javax.mail.Store getStore(final IndexableEmail indexableMail) throws NoSuchProviderException {
		String host = indexableMail.getMailHost();
		final String username = indexableMail.getUsername();
		final String password = indexableMail.getPassword();
		int port = Integer.valueOf(indexableMail.getPort()).intValue();
		String protocol = indexableMail.getProtocol() != null ? indexableMail.getProtocol() : MAIL_PROTOCOL;

		Properties pop3Props = new Properties();

		if (indexableMail.isSecureSocketLayer()) {
			pop3Props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
			pop3Props.setProperty("mail.pop3.socketFactory.fallback", "false");
		}

		URLName url = new URLName(protocol, host, port, "", username, password);

		// Session session = Session.getInstance(pop3Props, null);
		Session session = Session.getDefaultInstance(pop3Props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		session.setDebug(Boolean.TRUE);

		javax.mail.Store store;
		if (indexableMail.isSecureSocketLayer()) {
			store = new POP3SSLStore(session, url);
		} else {
			store = session.getStore(url);
		}
		return store;
	}

}