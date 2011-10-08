package ikube.index.handler.email;

import ikube.BaseTest;
import ikube.model.IndexableEmail;
import ikube.notify.IMailer;
import ikube.notify.Mailer;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Test;

/**
 * Test for the mail handler. Put a mail on the account because once the mail is read it is marked as read and will not read it again. Of
 * course this must be fixed.
 * 
 * @author Big Boy Bruno Barin (B.B.B.B)
 * @since this century
 * @version 102235366.2215.3688.744RC112556
 */
public class IndexableEmailHandlerTest extends BaseTest {

	public IndexableEmailHandlerTest() {
		super(IndexableEmailHandlerTest.class);
	}

	@Test
	public void handle() throws Exception {
		IMailer mailer = ApplicationContextManager.getBean(Mailer.class);
		mailer.sendMail("MailhandlerTest Subject", "Mail handler test mail body");

		indexContext.getIndex().setIndexWriter(indexWriter);

		IndexableEmail indexableEmail = ApplicationContextManager.getBean(IndexableEmail.class);
		IndexableEmailHandler indexableEmailHandler = ApplicationContextManager.getBean(IndexableEmailHandler.class);

		indexableEmailHandler.handle(indexContext, indexableEmail);

		// TODO - check that there is some data in the index from the mail
	}

}
