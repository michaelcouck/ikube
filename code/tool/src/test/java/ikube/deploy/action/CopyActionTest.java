package ikube.deploy.action;

import ikube.AbstractTest;
import ikube.deploy.model.Server;
import ikube.toolkit.FileUtilities;
import mockit.Cascading;
import mockit.Mockit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import net.schmizz.sshj.xfer.scp.SCPUploadClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-06-13
 */
public class CopyActionTest extends AbstractTest {

    @Cascading
    private SCPUploadClient uploader;

    @Before
    public void before() {
        Mockit.setUpMocks();
    }

    @After
    public void after() {
        Mockit.tearDownMocks();
    }

    @Test
    public void execute() throws Exception {
        final SSHClient sshExec = mock(SSHClient.class);
        final SCPFileTransfer scpFileTransfer = mock(SCPFileTransfer.class);

        Session session = mock(Session.class);
        when(sshExec.startSession()).thenReturn(session);
        when(sshExec.newSCPFileTransfer()).thenReturn(scpFileTransfer);
        when(scpFileTransfer.newSCPUploadClient()).thenReturn(uploader);

        CopyAction copyAction = new CopyAction() {
            protected SSHClient getSshExec(final Server server) {
                return sshExec;
            }
        };

        Server server = new Server();
        server.setUsername("nsername");
        server.setPassword("password");
        server.setIp("127.0.0.1");
        server.setSshExec(sshExec);

        Map<String, String> files = new HashMap<>();
        Map<String, String> directories = new HashMap<>();

        File file = FileUtilities.findFileRecursively(new File("."), "deployer.xml");
        File directory = FileUtilities.findDirectoryRecursively(new File("."), file.getParentFile().getName());

        files.put(file.getName(), "/tmp");
        directories.put(directory.getName(), "/tmp");

        copyAction.setFiles(files);
        copyAction.setDirectories(directories);

        copyAction.execute(server);
        verify(sshExec, atLeastOnce()).newSCPFileTransfer();
    }

}