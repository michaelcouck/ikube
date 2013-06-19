package ikube.deploy.action;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.SSHExec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Action implements IAction {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	protected boolean breakOnError;

	public boolean isBreakOnError() {
		return breakOnError;
	}

	public void setBreakOnError(boolean breakOnError) {
		this.breakOnError = breakOnError;
	}

	protected SSHExec getSshExec(final String ip, final String username, final String password) {
		SSHExec sshExec = null;
		ConnBean connBean = null;
		try {
			logger.info("Connecting to : " + ip + " as " + username);
			// Initialize a ConnBean object, parameter list is ip, username, password
			connBean = new ConnBean(ip, username, password);
			// Put the ConnBean instance as parameter for SSHExec static method
			sshExec = SSHExec.getInstance(connBean);
			// Connect to server
			boolean connected = sshExec.connect();
			if (!connected) {
				logger.error("Couldn't connect to server : " + ip);
			}
		} catch (Exception e) {
			disconnect(sshExec);
			handleException("Exception connecting to : " + ip, e);
		}
		return sshExec;
	}

	protected void disconnect(final SSHExec sshExec) {
		try {
			if (sshExec != null) {
				boolean disconnected = sshExec.disconnect();
				if (!disconnected) {
					logger.warn("Couldn't disconnect from : " + sshExec);
				}
			}
		} catch (Exception e) {
			handleException("Exception closing connection : " + sshExec, e);
		}
	}

	protected void handleException(final String message, final Exception exception) {
		logger.error(message, exception);
		if (isBreakOnError()) {
			throw new RuntimeException(exception);
		}
	}

}
