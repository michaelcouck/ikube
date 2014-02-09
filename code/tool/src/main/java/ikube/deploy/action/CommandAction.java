package ikube.deploy.action;

import ikube.deploy.model.Server;

import java.util.Collection;

import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

import org.apache.commons.lang.builder.ToStringBuilder;

public class CommandAction extends Action {

	private Collection<String> commands;

	@Override
	public boolean execute(final Server server) {
		SSHExec sshExec = getSshExec(server.getIp(), server.getUsername(), server.getPassword());
		logger.info("Ssh exec : " + sshExec + ", " + commands);
		try {
			if (commands != null) {
				for (final String command : commands) {
                    boolean success = Boolean.FALSE;
                    String message = null;
                    String error = null;
                    int returnCode = 0;
					try {
						CustomTask sampleTask = new ExecCommand(command);
						Result result = sshExec.exec(sampleTask);
                        success = result.isSuccess;
                        message = result.sysout;
                        error = result.error_msg;
                        returnCode = result.rc;
					} catch (final Exception e) {
						handleException("Exception executing command on server : " + command + ", server : " + server.getIp(), e);
					} finally {
                        logger.info("Result of command : " + success + ", " + message + ", " + error + ", " + returnCode);
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
