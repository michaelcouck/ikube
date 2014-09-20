package ikube.deploy.action;

import ikube.AbstractTest;
import ikube.deploy.model.Server;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 13-08-2014
 */
public class ActionTest extends AbstractTest {

    private Action action;

    @Before
    public void before() {
        action = new Action() {
            @Override
            public boolean execute(final Server server) throws Exception {
                return Boolean.TRUE;
            }
        };
    }

    @After
    public void after() {
        Mockit.tearDownMocks(SSHClientMock.class);
    }

    @Test
    public void getSshExec() throws Exception {
        Server server = mock(Server.class);
        when(server.getIp()).thenReturn("127.0.0.1");
        when(server.getName()).thenReturn("localhost");
        when(server.getUsername()).thenReturn("username");
        when(server.getPassword()).thenReturn("password");

        action.getSshExec(server);
        verify(server, times(0)).setSshExec(any(SSHClient.class));
        verify(server, atLeast(3)).getSshExec();

        Mockit.setUpMocks(SSHClientMock.class);
        action.getSshExec(server);
        verify(server, times(4)).setSshExec(any(SSHClient.class));
    }

    @SuppressWarnings("UnusedDeclaration")
    @MockClass(realClass = SSHClient.class)
    public static class SSHClientMock {
        @Mock
        public void authPassword(final String username, final char[] password)
                throws UserAuthException, TransportException {
            // Do nothing
        }
    }

}