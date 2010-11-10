package ikube.index.visitor.email;

import ikube.BaseTest;
import ikube.model.IndexableEmail;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Mailer;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for the mail visitor. Put a mail on the account because once the mail is read it is marked as read and will not read it again. Of
 * course this must be fixed.
 *
 * @author Big Boy Bruno Barin (B.B.B.B)
 * @since this century
 * @version 102235366.2215.3688.744RC112556
 */
public class IndexableEmailVisitorTest extends BaseTest {

	final String username = "isearch.eu@gmail.com";
	final String password = "caherline";
	final String host = "pop.gmail.com";
	final String port = "995";

	@Test
	@Ignore
	public void visit() throws Exception {
		Mailer mailer = ApplicationContextManager.getBean(Mailer.class);
		mailer.sendMail("MailVisitorTest Subject", "Mail visitor test mail body");

		IndexableEmailVisitor<IndexableEmail> indexableEmailVisitor = new IndexableEmailVisitor<IndexableEmail>();
		IndexableEmail indexableEmail = new IndexableEmail();
		indexableEmail.setMailHost(host);
		indexableEmail.setUsername(username);
		indexableEmail.setPassword(password);
		indexableEmail.setPort(port);
		indexableEmail.setSecureSocketLayer(true);
		indexableEmailVisitor.visit(indexableEmail);

		// TODO - implement this test
	}

}
