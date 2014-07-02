package ikube.toolkit;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public class MailerTest extends AbstractTest {

	@Spy
	@InjectMocks
	private Mailer mailer;

    @Test
    public void sendMail() throws Exception {
        // This must just pass
        mailer.setAuth("true");
        mailer.setMailHost("smtp.gmail.com");
        mailer.setPassword("caherline");
        mailer.setPort("465");
        mailer.setProtocol("pop3");
        mailer.setRecipients("ikube.notifications@gmail.com");
        mailer.setSender("ikube.notifications@gmail.com");
        mailer.setUser("ikube.notifications");

        boolean sent = mailer.sendMail("MailerTest : subject", "MailerTest : message");
        assertTrue("This mail should be sent : ", sent);

    }

}
