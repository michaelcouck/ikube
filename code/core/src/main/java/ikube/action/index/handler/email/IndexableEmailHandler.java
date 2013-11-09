package ikube.action.index.handler.email;

import ikube.action.index.IndexManager;
import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.action.index.parse.IParser;
import ikube.action.index.parse.ParserProvider;
import ikube.model.IndexContext;
import ikube.model.IndexableEmail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ForkJoinTask;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;

import com.sun.mail.pop3.POP3SSLStore;

/**
 * This class reads and indexes a mail account. At the time of writing it was not multi-threaded but could be made multi, however this would only be needed with
 * very large accounts indeed.
 * 
 * @author Bruno Barin
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableEmailHandler extends IndexableHandler<IndexableEmail> {

	private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
	private static final String MAIL_PROTOCOL = "pop3";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ForkJoinTask<?> handleIndexableForked(final IndexContext<?> indexContext, final IndexableEmail indexableEmail) throws Exception {
		IResourceProvider<IndexableEmail> emailResourceProvider = new IResourceProvider<IndexableEmail>() {

			List<IndexableEmail> indexableEmails;

			@Override
			public IndexableEmail getResource() {
				if (indexableEmails.isEmpty()) {
					return null;
				}
				return indexableEmails.get(0);
			}

			@Override
			public void setResources(final List<IndexableEmail> resources) {
				this.indexableEmails = resources;
			}
		};
		emailResourceProvider.setResources(new ArrayList<IndexableEmail>(Arrays.asList(indexableEmail)));
		return getRecursiveAction(indexContext, indexableEmail, emailResourceProvider);
	}

	@Override
	protected List<?> handleResource(final IndexContext<?> indexContext, final IndexableEmail indexableEmail, final Object resource) {
		logger.info("Handling resource : " + resource + ", thread : " + Thread.currentThread().hashCode());
		handleEmail(indexContext, indexableEmail);
		return null;
	}

	/**
	 * This method actually goes to the account and indexes the data.
	 * 
	 * @param indexContext the context for the index
	 * @param indexableMail the indexable to index
	 */
	protected void handleEmail(final IndexContext<?> indexContext, final IndexableEmail indexableMail) {
		Store store;
		try {
			store = getStore(indexableMail);
			store.connect();
		} catch (MessagingException e) {
			handleException(indexableMail, e);
			return;
		}

		try {
			Folder inbox = store.getFolder("inbox");
			handleFolder(indexContext, indexableMail, inbox);
		} catch (Exception e) {
			handleException(indexableMail, e);
		}

		// TODO - We would like to access all the folders, but how?
		Folder[] folders;
		try {
			folders = store.getPersonalNamespaces();
		} catch (MessagingException e) {
			handleException(indexableMail, e);
			return;
		}

		for (final Folder folder : folders) {
			try {
				handleFolder(indexContext, indexableMail, folder);
			} catch (Exception e) {
				handleException(indexableMail, e);
			}
		}

		closeMailServerConnection(store);
	}

	/**
	 * Handles one folder in the mail account, reading the messages and indexing the content.
	 * 
	 * @param indexContext the index context
	 * @param indexableMail
	 * @param folder
	 * @throws Exception
	 */
	protected void handleFolder(final IndexContext<?> indexContext, final IndexableEmail indexableMail, final Folder folder) throws Exception {
		folder.open(Folder.READ_ONLY);

		// For each message found in the server, index it.
		logger.info("Message count : " + folder.getMessageCount() + ", " + folder.getFullName());
		for (Message message : folder.getMessages()) {
			handleResource(indexContext, indexableMail, new Document(), message);
			Thread.sleep(indexContext.getThrottle());
		}
		folder.close(true);
	}

	Document handleResource(IndexContext<?> indexContext, IndexableEmail indexableMail, Document document, Object resource) throws Exception {
		Message message = (Message) resource;
		Date recievedDate = message.getReceivedDate();
		Date sentDate = message.getSentDate();
		int messageNumber = message.getMessageNumber();
		long timestamp = recievedDate != null ? recievedDate.getTime() : sentDate != null ? sentDate.getTime() : 0;
		String messageId = getMessageId(indexableMail, messageNumber, timestamp);

		if (logger.isDebugEnabled()) {
			logger.debug("Sent : " + sentDate);
			logger.debug("Recieved : " + recievedDate);
			logger.debug("Timestamp : " + timestamp);
			logger.debug("Message number : " + messageNumber);
		}

		// Add the id field to the document
		IndexManager.addStringField(indexableMail.getIdField(), messageId, indexableMail, document);
		// Add the title field to the document
		IndexManager.addStringField(indexableMail.getTitleField(), message.getSubject(), indexableMail, document);
		String messageContent = getMessageContent(message);
		if (StringUtils.isNotEmpty(messageContent)) {
			byte[] bytes = messageContent.getBytes();
			IParser parser = ParserProvider.getParser(message.getContentType(), bytes);
			OutputStream outputStream = parser.parse(new ByteArrayInputStream(bytes), new ByteArrayOutputStream());
			String fieldContent = outputStream.toString();
			// Add the content field to the document
			IndexManager.addStringField(indexableMail.getContentField(), fieldContent, indexableMail, document);
		}
		resourceHandler.handleResource(indexContext, indexableMail, document, message);
		return document;
	}

	protected String getMessageId(final IndexableEmail indexableMail, final int messageNumber, final long timestamp) {
		StringBuilder builder = new StringBuilder();
		builder.append(indexableMail.getMailHost());
		builder.append('.');
		builder.append(indexableMail.getUsername());
		builder.append('.');
		builder.append(messageNumber);
		builder.append('.');
		builder.append(timestamp);
		return builder.toString();
	}

	/**
	 * Closes the connection to the mail server
	 * 
	 * @param store The Store object that holds the connection to the mail server.
	 */
	private void closeMailServerConnection(final javax.mail.Store store) {
		try {
			store.close();
		} catch (MessagingException e) {
			handleException(null, e);
		}
	}

	/**
	 * Returns a message content given a SynchronizationMessage object
	 * 
	 * @param message The SynchronizationMessage object representing a message in the mail server
	 * @return The message content.
	 * @throws IOException If some problem occurs when trying to access the message content.
	 * @throws MessagingException If some problem occurs when trying to access the message content.
	 */
	private String getMessageContent(final Message message) throws IOException, MessagingException {
		StringBuilder messageContent = new StringBuilder();
		Object obj = message.getContent();
		if (obj.getClass().isAssignableFrom(Multipart.class)) {
			Multipart multipart = (Multipart) obj;
			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				Object content = bodyPart.getContent();
				if (content != null) {
					byte[] bytes = content.toString().getBytes();
					IParser parser = ParserProvider.getParser(null, bytes);
					try {
						String parsedContent = parser.parse(new ByteArrayInputStream(bytes), new ByteArrayOutputStream()).toString();
						messageContent.append(parsedContent);
						messageContent.append(" ");
					} catch (Exception e) {
						logger.error("Exception getting the attachments of the messages : ", e);
					}
				}
			}
		} else {
			messageContent.append(obj);
		}
		messageContent.append(" ");
		return messageContent.toString();
	}

	/**
	 * Returns a connection to the mail server given a Mail visitable
	 * 
	 * @param indexableMail The {@link IndexableEmail} indexable object
	 * @return The {@link Store} object that holds a connection to the mail server.
	 * @throws NoSuchProviderException If the mail provider wasn't correct specified.
	 */
	private Store getStore(final IndexableEmail indexableMail) throws NoSuchProviderException {
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