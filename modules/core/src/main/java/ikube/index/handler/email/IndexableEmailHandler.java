package ikube.index.handler.email;

import ikube.index.IndexManager;
import ikube.index.handler.IndexableHandler;
import ikube.index.handler.IndexableHandlerType;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.model.IndexableEmail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
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
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

import com.sun.mail.pop3.POP3SSLStore;

/**
 * @author Bruno Barin
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableEmailHandler extends IndexableHandler<IndexableEmail> {

	static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
	static final String MAIL_PROTOCOL = "pop3";

	@Override
	@IndexableHandlerType(type = IndexableEmail.class)
	public List<Thread> handle(final IndexContext indexContext, final IndexableEmail indexable) throws Exception {
		handleEmail(indexContext, (IndexableEmail) indexable);
		return null;
	}

	protected void handleEmail(final IndexContext indexContext, IndexableEmail indexableMail) {
		javax.mail.Store store = null;
		try {
			store = getStore(indexableMail);
			store.connect();
		} catch (MessagingException e) {
			String message = Logging.getString("Could not connect to the mail server : ", indexableMail.getMailHost(), " port : ",
					indexableMail.getPort());
			logger.error(message, e);
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
				long timestamp = recievedDate != null ? recievedDate.getTime() : sentDate != null ? sentDate.getTime() : 0;

				if (logger.isDebugEnabled()) {
					logger.debug("Recieved : " + recievedDate);
					logger.debug("Sent : " + sentDate);
					logger.debug("Message number : " + messageNumber);
					logger.debug("Timestamp : " + timestamp);
				}

				StringBuilder builder = new StringBuilder();
				builder.append(indexableMail.getMailHost());
				builder.append(".");
				builder.append(indexableMail.getUsername());
				builder.append(".");
				builder.append(messageNumber);
				builder.append(".");
				builder.append(timestamp);

				Store mustStore = indexableMail.isStored() ? Store.YES : Store.NO;
				Index analyzed = indexableMail.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
				TermVector termVector = indexableMail.isVectored() ? TermVector.YES : TermVector.NO;

				Document document = new Document();
				// Add the id field to the document
				IndexManager.addStringField(indexableMail.getIdField(), builder.toString(), document, mustStore, analyzed, termVector);
				// Add the title field to the document
				IndexManager.addStringField(indexableMail.getTitleField(), message.getSubject(), document, mustStore, analyzed, termVector);
				String messageContent = getMessageContent(message);
				if (StringUtils.isNotEmpty(messageContent)) {
					byte[] bytes = messageContent.getBytes();
					IParser parser = ParserProvider.getParser(message.getContentType(), bytes);
					OutputStream outputStream = parser.parse(new ByteArrayInputStream(bytes), new ByteArrayOutputStream());
					String fieldContent = outputStream.toString();
					// Add the content field to the document
					IndexManager.addStringField(indexableMail.getContentField(), fieldContent, document, mustStore, analyzed, termVector);
				}
				indexContext.getIndexWriter().addDocument(document);
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
	 * @param indexableMail
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
		session.setDebug(Boolean.FALSE);

		javax.mail.Store store;
		if (indexableMail.isSecureSocketLayer()) {
			store = new POP3SSLStore(session, url);
		} else {
			store = session.getStore(url);
		}
		return store;
	}

}
