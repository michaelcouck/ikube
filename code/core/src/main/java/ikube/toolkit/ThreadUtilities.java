package ikube.toolkit;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class just has a method that will wait for a list of threads to finish.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class ThreadUtilities {

	private static final Logger LOGGER = Logger.getLogger(ThreadUtilities.class);
	
	private ThreadUtilities() {
	}

	/**
	 * This method iterates through the list of threads looking for one that is still alive and joins it. Once all the threads have finished
	 * then this method will return to the caller indicating that all the threads have finished.
	 * 
	 * @param threads
	 *            the threads to wait for
	 */
	public static void waitForThreads(final List<Thread> threads) {
		outer: while (true) {
			for (Thread thread : threads) {
				if (thread.isAlive()) {
					try {
						thread.join();
					} catch (InterruptedException e) {
						LOGGER.error("Interrupted waiting for thread : " + thread + ", this thread : " + Thread.currentThread(), e);
					}
					continue outer;
				}
			}
			break;
		}
	}

}
