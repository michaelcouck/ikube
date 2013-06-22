package ikube.deploy.action;

import ikube.deploy.model.Server;

import java.util.Collection;

import org.apache.commons.lang.builder.ToStringBuilder;

import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

public class CommandAction extends Action {

	private Collection<String> commands;

	@Override
	public boolean execute(final Server server) {
		SSHExec sshExec = getSshExec(server.getIp(), server.getUsername(), server.getPassword());
		logger.error("Ssh exec : " + sshExec + ", " + commands);
		System.out.println("Ssh exec : " + sshExec + ", " + commands);
		System.err.println("Ssh exec : " + sshExec + ", " + commands);
		try {
			if (commands != null) {
				for (final String command : commands) {
					try {
						logger.info("Executing command : " + command + ", on server : " + server.getIp());
						CustomTask sampleTask = new ExecCommand(command);
						Result result = sshExec.exec(sampleTask);
						logger.info("Result of command : " + ToStringBuilder.reflectionToString(result));
					} catch (Exception e) {
						handleException("Exception executing command on server : " + command + ", server : " + server.getIp(), e);
					}
				}
			}
		} finally {
			disconnect(sshExec);
		}
		return Boolean.TRUE;
	}

	public void setCommands(Collection<String> commands) {
		this.commands = commands;
	}

}
