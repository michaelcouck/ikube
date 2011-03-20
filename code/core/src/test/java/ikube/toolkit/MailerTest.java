package ikube.toolkit;

import ikube.BaseTest;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class MailerTest extends BaseTest {

	public MailerTest() {
		super(MailerTest.class);
	}

	@Test
	public void sendMail() throws Exception {
		// This must just pass
		Mailer mailer = ApplicationContextManager.getBean(Mailer.class);
		mailer.sendMail("Mailer test subject", "Mailer test message");
	}

}
