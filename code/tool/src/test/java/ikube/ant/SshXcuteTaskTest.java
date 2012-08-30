package ikube.ant;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SshXcuteTaskTest {

	@Test
	public void main() {
		new SshXcuteTask().executeCommand("192.168.1.4", "root", "caherline", "killall java");
		new SshXcuteTask().executeCommand("192.168.1.6", "root", "caherline", "killall java");
		new SshXcuteTask().executeCommand("192.168.1.7", "root", "caherline", "killall java");
	}

	@Test
	public void one() {
		new SshXcuteTask().executeCommand("192.168.1.4", "root", "caherline", "killall java");
	}

	@Test
	public void two() {
		new SshXcuteTask().executeCommand("192.168.1.6", "root", "caherline", "killall java");
	}

	@Test
	public void three() {
		new SshXcuteTask().executeCommand("192.168.1.7", "root", "caherline", "killall java");
	}

}
