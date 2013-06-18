package ikube.deploy.action;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import ikube.AbstractTest;
import ikube.deploy.Deployer;
import ikube.deploy.action.CopyAction;
import ikube.deploy.model.Server;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.SSHExec;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CopyActionTest extends AbstractTest {

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
		Server server = (Server) Deployer.getApplicationContext().getBean("server-one");
		CopyAction copyAction = (CopyAction) Deployer.getApplicationContext().getBean("copy-action");
		copyAction.execute(server);
		verify(SSHExecMock.sshExec, atLeastOnce()).uploadSingleDataToServer(anyString(), anyString());
		verify(SSHExecMock.sshExec, atLeastOnce()).uploadAllDataToServer(anyString(), anyString());
	}

}