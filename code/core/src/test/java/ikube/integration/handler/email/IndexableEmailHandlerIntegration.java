package ikube.integration.handler.email;

import ikube.index.IndexManager;
import ikube.index.handler.email.IndexableEmailHandler;
import ikube.integration.AbstractIntegration;
import ikube.model.IndexContext;
import ikube.model.IndexableEmail;
import ikube.notify.IMailer;
import ikube.notify.Mailer;
import ikube.toolkit.ApplicationContextManager;

import java.net.InetAddress;

import org.apache.lucene.index.IndexWriter;
import org.junit.Test;

/**
 * Test for the mail handler. Put a mail on the account because once the mail is read it is marked as read and will not
 * read it again. Of course this must be fixed.
 * 
 * @author Big Boy Bruno Barin (B.B.B.B)
 * @since this century
 * @version 102235366.2215.3688.744RC112556
 */
public class IndexableEmailHandlerIntegration extends AbstractIntegration {
	
	@SuppressWarnings("rawtypes")
	private IndexContext indexContext;

	@Test
	public void handle() throws Exception {
		indexContext = monitorService.getIndexContext("indexContext");
		IMailer mailer = ApplicationContextManager.getBean(Mailer.class);
		mailer.sendMail("MailhandlerTest Subject", "Mail handler test mail body");
		String ip = InetAddress.getLocalHost().getHostAddress();
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
		indexContext.setIndexWriter(indexWriter);

		IndexableEmail indexableEmail = ApplicationContextManager.getBean(IndexableEmail.class);
		IndexableEmailHandler indexableEmailHandler = ApplicationContextManager.getBean(IndexableEmailHandler.class);

		indexableEmailHandler.handle(indexContext, indexableEmail);

		// TODO - check that there is some data in the index from the mail
	}

}
