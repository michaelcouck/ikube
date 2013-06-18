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
			// Initialize a ConnBean object, parameter list is ip, username, password
			connBean = new ConnBean(ip, username, password);
			// Put the ConnBean instance as parameter for SSHExec static method
			sshExec = SSHExec.getInstance(connBean);
			// Connect to server
			boolean connected = sshExec.connect();
			if (!connected) {
				logger.error("Couldn't connect to server : " + ip);
			}
			return sshExec;
		} catch (Exception e) {
			disconnect(sshExec);
			throw new RuntimeException(e);
		}
	}

	protected void disconnect(final SSHExec sshExec) {
		try {
			if (sshExec != null) {
				sshExec.disconnect();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
