package ikube.toolkit;

import ikube.BaseTest;

import org.junit.Test;

public class MailerTest extends BaseTest {

	@Test
	public void sendMail() throws Exception {
		// This must just pass
		Mailer mailer = ApplicationContextManager.getBean(Mailer.class);
		mailer.sendMail("Mailer test subject", "Mailer test message");
	}

}
