package ikube.toolkit;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a test to stop immediately a thread, which is nice and indeed useful.
 * 
 * @author Michael Couck
 * @since 12.01.2013
 * @version 01.00
 */
public class ThreadDeath {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadDeath.class);

	public void main() {
		// Start some threads
		final List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < 3; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						while (true) {
							// Do nothing
						}
					} catch (Exception e) {
						LOGGER.error(null, e);
					}
				}
			});
			thread.start();
			threads.add(thread);
		}
		// Create a thread that will kill all the others
		new Thread(new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				ThreadUtilities.sleep(1000);
				for (final Thread thread : threads) {
					thread.interrupt();
					thread.stop();
				}
			}
		}).start();
		ThreadUtilities.waitForThreads(threads);
		// If the stop didn't work on the threads we would never get here, i.e.
		// the test would never end and the cpu would be at 100% forever and ever
	}

}
