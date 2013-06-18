package ikube.ant;

import java.util.Arrays;
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
@Deprecated
public class SshXcuteTask extends Task {

	static final Logger LOGGER = Logger.getLogger(SshXcuteTask.class.getName());

	static final String PIPE = "\\|";
	static final String DELIMITERS = ";";
	static final String DELIMITERS_KEEP = "(?<=;)";

	private String[] ips;
	private String[] usernames;
	private String[] passwords;
	private String[] commands;
	private String[][] filesToCopy;
	private String[][] directoriesToCopy;

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
		for (int i = 0; ips != null && i < ips.length; i++) {
			SSHExec sshExec = null;
			try {
				sshExec = getSshExec(ips[i], usernames[i], passwords[i]);
				executeCommands(sshExec, getCommands());
				// Now transfer any files that need to be transferred
				executeFileCopy(sshExec, getFilesToCopy());
				// And copy any directories that need to be copied to the server
				executeDirectoryCopy(sshExec, getDirectoriesToCopy());
			} finally {
				disconnect(sshExec);
			}
		}
	}

	private void executeDirectoryCopy(final SSHExec sshExec, final String[][] directoriesToCopy) {
		if (directoriesToCopy == null || directoriesToCopy.length == 0) {
			return;
		}
		for (final String[] directoryToCopy : directoriesToCopy) {
			try {
				sshExec.uploadAllDataToServer(directoryToCopy[0], directoryToCopy[1]);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Exception copying directory : " + Arrays.deepToString(directoryToCopy), e);
			}
		}
	}

	private void executeFileCopy(final SSHExec sshExec, final String[][] filesToCopy) {
		if (filesToCopy == null || filesToCopy.length == 0) {
			return;
		}
		for (final String[] fileToCopy : filesToCopy) {
			try {
				sshExec.uploadSingleDataToServer(fileToCopy[0], fileToCopy[1]);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Exception copying file : " + Arrays.deepToString(filesToCopy), e);
			}
		}
	}

	private void executeCommands(final SSHExec sshExec, final String... commands) {
		if (commands == null || commands.length == 0) {
			return;
		}
		try {
			CustomTask sampleTask = new ExecCommand(commands);
			Result result = sshExec.exec(sampleTask);
			LOGGER.log(Level.FINE, "$" + result);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception executing task : " + Arrays.deepToString(commands), e);
		}
	}

	private SSHExec getSshExec(final String ip, final String username, final String password) {
		SSHExec sshExec = null;
		ConnBean connBean = null;
		try {
			// Initialize a ConnBean object, parameter list is ip, username, password
			connBean = new ConnBean(ip, username, password);
			// Put the ConnBean instance as parameter for SSHExec static method
			sshExec = SSHExec.getInstance(connBean);
			// Connect to server
			sshExec.connect();
			return sshExec;
		} catch (Exception e) {
			disconnect(sshExec);
			throw new RuntimeException(e);
		}
	}

	private void disconnect(final SSHExec sshExec) {
		try {
			if (sshExec != null) {
				sshExec.disconnect();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
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

	public String[][] getFilesToCopy() {
		return filesToCopy;
	}

	public void setFilesToCopy(final String filesToCopy) {
		this.filesToCopy = splitStringTwice(filesToCopy, DELIMITERS, PIPE);
	}

	public String[][] getDirectoriesToCopy() {
		return directoriesToCopy;
	}

	public void setDirectoriesToCopy(final String directoriesToCopy) {
		this.directoriesToCopy = splitStringTwice(directoriesToCopy, DELIMITERS, PIPE);
	}

	String[] splitString(final String string, final String delimiters) {
		return string.split(delimiters);
	}

	String[][] splitStringTwice(final String string, final String delimiterOne, final String delimiterTwo) {
		if (string == null || "".equals(string.trim())) {
			return new String[0][];
		}
		int index = 0;
		// First split the files into pairs of files and target directories
		String[] sourceTargetPairs = splitString(string, delimiterOne);
		String[][] sourceTargetPairArray = new String[sourceTargetPairs.length][2];
		// Then split the pairs into double string arrays
		for (final String sourceTargetPair : sourceTargetPairs) {
			sourceTargetPairArray[index] = splitString(sourceTargetPair, delimiterTwo);
			index++;
		}
		return sourceTargetPairArray;
	}

}