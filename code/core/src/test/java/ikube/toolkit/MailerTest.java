package ikube.toolkit;

import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.toolkit.IMailer;
import ikube.toolkit.Mailer;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Ignore
public class MailerTest extends AbstractTest {

	public MailerTest() {
		super(MailerTest.class);
	}

	@Test
	public void sendMail() throws Exception {
		// This must just pass
		IMailer mailer = new Mailer();
		mailer.setAuth("true");
		mailer.setMailHost("smtp.gmail.com");
		mailer.setPassword("Caherl2ne");
		mailer.setPort("465");
		mailer.setProtocol("pop3");
		mailer.setRecipients("ikube.ikube@gmail.com");
		mailer.setSender("ikube.ikube@gmail.com");
		mailer.setUser("ikube.ikube");

		boolean sent = mailer.sendMail("MailerTest : subject", "MailerTest : message");
		assertTrue("This mail should be sent : ", sent);
	}

}
