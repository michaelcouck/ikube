package ikube.ant;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshXcuteTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(SshXcuteTask.class);

	public void main() {
		executeCommand("192.168.1.7", "root", "caherline", "killall java");
		executeCommand("192.168.1.6", "root", "caherline", "killall java");
		executeCommand("192.168.1.4", "root", "caherline", "killall java");
	}

	public void executeCommand(final String ip, final String username, final String password, final String command) {
		SSHExec sshExec = null;
		ConnBean connBean = null;
		try {
			// Initialize a ConnBean object, parameter list is ip, username, password
			connBean = new ConnBean(ip, username, password);
			// Put the ConnBean instance as parameter for SSHExec static method
			// getInstance(ConnBean) to retrieve a singleton SSHExec instance
			sshExec = SSHExec.getInstance(connBean);
			// Connect to server
			sshExec.connect();
			// CustomTask sampleTask = new ExecCommand("kill -9 27966");
			CustomTask sampleTask = new ExecCommand(command);
			Result result = sshExec.exec(sampleTask);
			LOGGER.info("Result : " + ToStringBuilder.reflectionToString(result, ToStringStyle.SHORT_PREFIX_STYLE));
		} catch (Exception e) {
			LOGGER.error("Exception executing task : " + command, e);
		} finally {
			if (sshExec != null) {
				sshExec.disconnect();
				sshExec = null;
			}
			if (connBean != null) {
				connBean = null;
			}
		}
	}

}