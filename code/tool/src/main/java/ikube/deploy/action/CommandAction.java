package ikube.deploy.action;

import ikube.deploy.model.Command;
import ikube.deploy.model.Server;

import java.util.Collection;

import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

public class CommandAction extends Action {

	private Collection<Command> commands;

	@Override
	public boolean execute(final Server server) {
		SSHExec sshExec = getSshExec(server.getIp(), server.getUsername(), server.getPassword());
		if (commands != null) {
			for (final Command command : commands) {
				try {
					CustomTask sampleTask = new ExecCommand(command.getCommand());
					Result result = sshExec.exec(sampleTask);
					logger.debug("Result of command : " + result);
				} catch (Exception e) {
					logger.error("Exception executing command on server : " + command.getCommand() + ", server : " + server.getIp(), e);
					if (isBreakOnError()) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return Boolean.TRUE;
	}

	public Collection<Command> getCommands() {
		return commands;
	}

	public void setCommands(Collection<Command> commands) {
		this.commands = commands;
	}

}
