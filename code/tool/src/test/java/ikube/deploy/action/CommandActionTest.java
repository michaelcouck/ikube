package ikube.deploy.action;

import static org.mockito.Mockito.*;

import ikube.AbstractTest;
import ikube.deploy.Deployer;
import ikube.deploy.action.CommandAction;
import ikube.deploy.model.Server;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.task.CustomTask;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CommandActionTest extends AbstractTest {

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
		CommandAction commandAction = (CommandAction) Deployer.getApplicationContext().getBean("stop-action");
		commandAction.execute(server);
		verify(SSHExecMock.sshExec, atLeastOnce()).exec(any(CustomTask.class));
	}

}