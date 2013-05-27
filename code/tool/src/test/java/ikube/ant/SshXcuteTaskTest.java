package ikube.ant;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 26.05.2013
 * @version 01.00
 */
public class SshXcuteTaskTest {

	private SshXcuteTask sshXcuteTask;

	@Before
	public void before() {
		sshXcuteTask = new SshXcuteTask();
	}

	@Test
	@Ignore
	public void execute() {
		sshXcuteTask.setIps("localhost");
		sshXcuteTask.setUsernames("michael");
		sshXcuteTask.setPasswords("and-the-password");
		sshXcuteTask.setCommands("pwd");
		sshXcuteTask.execute();
	}

	@Test
	public void setCommands() {
		sshXcuteTask.setCommands("hello:world|:commands,,|,|to be done");
		String[] commands = sshXcuteTask.getCommands();
		assertEquals("hello", commands[0]);
		assertEquals("world", commands[1]);
		assertEquals("commands", commands[2]);
		assertEquals("to be done", commands[3]);
	}

	@Test
	public void splitString() {
		String[] splitString = sshXcuteTask.splitString("192.168.1.8|192.168.1.8", SshXcuteTask.DELIMITERS);
		assertEquals("192.168.1.8", splitString[0]);
		assertEquals("192.168.1.8", splitString[1]);
	}

}
