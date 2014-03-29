package ikube.action;

import ikube.AbstractTest;
import ikube.toolkit.IMailer;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;

import java.io.Closeable;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 16-01-2012
 */
public class ActionTest extends AbstractTest {

    private Action<?, ?> action;

    @Before
    public void before() {
        action = new Backup();
        Deencapsulation.setField(action, clusterManager);
    }

    @Test
    public void execute() throws Exception {
        action.execute(indexContext);
    }

    @Test
    public void close() throws Exception {
        Closeable closeable = mock(Closeable.class);
        action.close(closeable);
        verify(closeable, atLeastOnce()).close();
    }

    @Test
    public void sendNotification() throws Exception {
        IMailer mailer = mock(IMailer.class);
        Deencapsulation.setField(action, mailer);
        assertNotNull("This should be the mocked cluster manager : ", clusterManager);
        action.sendNotification("subject", "body");
        verify(mailer, atLeast(1)).sendMail(any(String.class), any(String.class));
    }

}