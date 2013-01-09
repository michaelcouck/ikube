package ikube.toolkit;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadDeath {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadDeath.class);

	public static void main(String[] args) {
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
			public void run() {
				ThreadUtilities.sleep(1000);
				for (final Thread thread : threads) {
					thread.interrupt();
					thread.stop();
				}
			}
		}).start();
		ThreadUtilities.waitForThreads(threads);
		
	}

}
