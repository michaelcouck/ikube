package ikube.toolkit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * This test if for the thread utilities, which has methods to wait for threads etc.
 * 
 * @author Michael Couck
 * @since 20.03.11
 * @version 01.00
 */
public class ThreadUtilitiesTest {
	
	private Logger logger = Logger.getLogger(this.getClass());

	@Test
	public void waitForThreads() {
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < 3; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep((long) (Math.random() * 10000));
					} catch (Exception e) {
						logger.error("Spit or swallow : ", e);
					}
					logger.debug("Thread exiting : " + Thread.currentThread());
				}
			});
			thread.start();
			threads.add(thread);
		}
		ThreadUtilities.waitForThreads(threads);
		// Verify that all the threads are dead
		for (Thread thread : threads) {
			assertFalse("All the threads should have died : ", thread.isAlive());
		}
		assertTrue("We just want to exit here after the threads die : ", true);
	}

}
