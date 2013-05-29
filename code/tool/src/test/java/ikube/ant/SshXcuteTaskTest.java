package ikube.ant;

import static org.junit.Assert.assertEquals;

import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.Before;
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
	public void executeEndToEnd() {
		File bzip2 = FileUtilities.findFileRecursively(new File("."), "bzip2\\.bzip2");
		String bzip2Path = FileUtilities.cleanFilePath(bzip2.getAbsolutePath());
		
		File wiki = FileUtilities.findDirectoryRecursively(new File("."), "7zip");
		String wikiPath = FileUtilities.cleanFilePath(wiki.getAbsolutePath());
		
		sshXcuteTask.setIps("localhost");
		sshXcuteTask.setUsernames("user");
		sshXcuteTask.setPasswords("password");
		sshXcuteTask.setCommands("pwd;ls -l");
		sshXcuteTask.setDirectoriesToCopy(wikiPath + "|/tmp;");
		sshXcuteTask.setFilesToCopy(bzip2Path + "|/tmp/" + bzip2.getName());
		sshXcuteTask.execute();
	}

	@Test
	public void setCommands() {
		sshXcuteTask.setCommands("hello;world;commands;to be done;");
		String[] commands = sshXcuteTask.getCommands();
		assertEquals("hello", commands[0]);
		assertEquals("world", commands[1]);
		assertEquals("commands", commands[2]);
		assertEquals("to be done", commands[3]);
	}

	@Test
	public void splitString() {
		String[] splitString = sshXcuteTask.splitString("192.168.1.8;192.168.1.8;", SshXcuteTask.DELIMITERS);
		assertEquals("192.168.1.8", splitString[0]);
		assertEquals("192.168.1.8", splitString[1]);

		splitString = sshXcuteTask.splitString("192.168.1.8|192.168.1.8", SshXcuteTask.PIPE);
		assertEquals("192.168.1.8", splitString[0]);
		assertEquals("192.168.1.8", splitString[1]);
	}

	@Test
	public void splitStringTwice() {
		String[][] splitString = sshXcuteTask.splitStringTwice("/usr/share|/usr/share;", SshXcuteTask.DELIMITERS, SshXcuteTask.PIPE);
		assertEquals("/usr/share", splitString[0][0]);
		assertEquals("/usr/share", splitString[0][1]);
	}

}
