package ikube.deploy.action;

import ikube.AbstractTest;
import ikube.deploy.model.Server;
import ikube.toolkit.FileUtilities;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.SSHExec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class CopyActionTest extends AbstractTest {

    @SuppressWarnings("UnusedDeclaration")
    @MockClass(realClass = SSHExec.class)
    public static class SSHExecMock {

        public static SSHExec sshExec;

        @Mock
        public static SSHExec getInstance(ConnBean conn) {
            if (sshExec == null) {
                sshExec = Mockito.mock(SSHExec.class);
            }
            return sshExec;
        }
    }

    @Before
    public void before() {
        Mockit.setUpMocks(SSHExecMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(SSHExecMock.class);
    }

    @Test
    public void execute() throws Exception {
        Server server = new Server();
        CopyAction copyAction = new CopyAction();

        Map<String, String> files = new HashMap<>();
        Map<String, String> directories = new HashMap<>();

        File file = FileUtilities.findFileRecursively(new File("."), "deployer.xml");
        File directory = FileUtilities.findDirectoryRecursively(new File("."), file.getParentFile().getName());

        files.put(file.getName(), file.getAbsolutePath());
        directories.put(directory.getName(), directory.getAbsolutePath());

        copyAction.setFiles(files);
        copyAction.setDirectories(directories);

        copyAction.execute(server);
        verify(SSHExecMock.sshExec, atLeastOnce()).uploadSingleDataToServer(anyString(), anyString());
        verify(SSHExecMock.sshExec, atLeastOnce()).uploadAllDataToServer(anyString(), anyString());
    }

}