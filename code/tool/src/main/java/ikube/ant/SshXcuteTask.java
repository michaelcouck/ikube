package ikube.ant;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

import org.apache.tools.ant.Task;

/**
 * This task just executes ssh commands on remote linux machines.
 * 
 * @author Michael Couck
 * @since 26.05.2013
 * @version 01.00
 */
public class SshXcuteTask extends Task {

	static final Logger LOGGER = Logger.getLogger(SshXcuteTask.class.getName());

	static final String DELIMITERS = "\n\r\t		";

	private String[] ips;
	private String[] usernames;
	private String[] passwords;
	private String[] commands;

	public static void main(final String[] args) {
		SshXcuteTask sshXcuteTask = new SshXcuteTask();
		sshXcuteTask.setIps(args[0]);
		sshXcuteTask.setUsernames(args[1]);
		sshXcuteTask.setPasswords(args[2]);
		sshXcuteTask.setCommands(args[3]);
		sshXcuteTask.execute();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() {
		for (int i = 0; i < ips.length; i++) {
			String ip = ips[i];
			String username = usernames[i];
			String password = passwords[i];
			SSHExec sshExec = null;
			ConnBean connBean = null;
			try {
				// Initialize a ConnBean object, parameter list is ip, username, password
				connBean = new ConnBean(ip, username, password);
				// Put the ConnBean instance as parameter for SSHExec static method
				sshExec = SSHExec.getInstance(connBean);
				// Connect to server
				sshExec.connect();
				for (final String command : commands) {
					LOGGER.info("Running command : " + command);
					CustomTask sampleTask = new ExecCommand(command);
					Result result = sshExec.exec(sampleTask);
					LOGGER.info("$" + result);
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Exception executing task : " + Arrays.deepToString(commands), e);
			} finally {
				try {
					if (sshExec != null) {
						sshExec.disconnect();
					}
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Exception closing connection to : " + ip, e);
				}
			}
		}
	}

	public String[] getIps() {
		return ips;
	}

	public void setIps(final String ips) {
		this.ips = splitString(ips, DELIMITERS);
	}

	public String[] getUsernames() {
		return usernames;
	}

	public void setUsernames(final String usernames) {
		this.usernames = splitString(usernames, DELIMITERS);
	}

	public String[] getPasswords() {
		return passwords;
	}

	public void setPasswords(final String passwords) {
		this.passwords = splitString(passwords, DELIMITERS);
	}

	public String[] getCommands() {
		return commands;
	}

	public void setCommands(final String commands) {
		this.commands = splitString(commands, DELIMITERS);
	}

	String[] splitString(final String string, final String delimiters) {
		StringTokenizer tokenizer = new StringTokenizer(string, delimiters);
		String[] stringArray = new String[tokenizer.countTokens()];
		for (int i = 0; tokenizer.hasMoreTokens(); i++) {
			String token = tokenizer.nextToken().trim();
			stringArray[i] = token;
		}
		return stringArray;
	}

}