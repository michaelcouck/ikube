package ikube.deploy.action;

import ikube.AbstractTest;
import ikube.deploy.model.Server;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import static ikube.toolkit.ObjectToolkit.populateFields;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-06-2013
 */
public class CmdActionTest extends AbstractTest {

    @Test
    public void execute() throws Exception {
        Server server = populateFields(new Server(), Boolean.TRUE, Integer.MAX_VALUE);
        final SSHClient sshExec = mock(SSHClient.class);
        Session session = mock(Session.class);
        Session.Command command = mock(Session.Command.class);
        InputStream inputStream = new ByteArrayInputStream("hello world".getBytes());
        when(sshExec.startSession()).thenReturn(session);
        when(session.exec(any(String.class))).thenReturn(command);
        when(command.getInputStream()).thenReturn(inputStream);
        when(command.getErrorStream()).thenReturn(inputStream);

        server.setSshExec(sshExec);

        CmdAction commandAction = new CmdAction() {
            protected SSHClient getSshExec(final Server server) {
                return sshExec;
            }
        };
        commandAction.setCommands(Arrays.asList("ls -l"));
        commandAction.execute(server);
        verify(session, atLeastOnce()).exec(any(String.class));
        verify(command, atLeastOnce()).getExitErrorMessage();
    }

}