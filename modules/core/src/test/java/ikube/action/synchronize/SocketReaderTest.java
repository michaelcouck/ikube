package ikube.action.synchronize;

import ikube.BaseTest;

import org.junit.Ignore;
import org.junit.Test;

/**
 * This test must run with the {@link SocketWriterTest}. First start this test in a Jvm, so there is a listener on a port for files to be
 * synchronized. Then start the SocketWriterTest that will create a new index and write the files to this listener/reader.
 *
 * @author Michael Couck
 * @since 07.11.10
 * @version 01.00
 */
@Ignore
public class SocketReaderTest extends BaseTest {

	@Test
	public void read() throws Exception {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				new SocketReader().read();
			}
		});
		thread.start();
		thread.join();
	}

}
