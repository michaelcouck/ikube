package ikube.deploy.action;

import static ikube.toolkit.ObjectToolkit.populateFields;
import ikube.AbstractTest;
import ikube.deploy.model.Server;

import java.util.Arrays;

import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.task.impl.ExecCommand;

import org.junit.Test;
import org.mockito.Mockito;

public class CommandActionTest extends AbstractTest {

	@Test
	public void execute() throws Exception {
		Server server = populateFields(new Server(), Boolean.TRUE, Integer.MAX_VALUE);
		final SSHExec sshExec = Mockito.mock(SSHExec.class);
		CommandAction commandAction = new CommandAction() {
			protected SSHExec getSshExec(final String ip, final String username, final String password) {
				System.out.println("Ssh : " + sshExec);
				return sshExec;
			}
		};
		commandAction.setCommands(Arrays.asList("command"));
		commandAction.execute(server);
		Mockito.verify(sshExec, Mockito.atLeastOnce()).exec(Mockito.any(ExecCommand.class));
	}

}