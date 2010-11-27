package ikube.index.handler.email;

import ikube.BaseTest;
import ikube.model.IndexableEmail;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Mailer;

import org.junit.Test;

/**
 * Test for the mail visitor. Put a mail on the account because once the mail is read it is marked as read and will not read it again. Of
 * course this must be fixed.
 * 
 * @author Big Boy Bruno Barin (B.B.B.B)
 * @since this century
 * @version 102235366.2215.3688.744RC112556
 */
public class IndexableEmailHandlerTest extends BaseTest {

	@Test
	public void visit() throws Exception {
		Mailer mailer = ApplicationContextManager.getBean(Mailer.class);
		mailer.sendMail("MailVisitorTest Subject", "Mail visitor test mail body");

		indexContext.setIndexWriter(indexWriter);

		IndexableEmail indexableEmail = ApplicationContextManager.getBean(IndexableEmail.class);
		IndexableEmailHandler indexableEmailVisitor = ApplicationContextManager.getBean(IndexableEmailHandler.class);

		indexableEmailVisitor.handle(indexContext, indexableEmail);

		// TODO - check that there is some data in the index from the mail
	}

}
