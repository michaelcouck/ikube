package ikube.toolkit;

import java.util.List;

import org.apache.log4j.Logger;

public class ThreadUtilities {

	private static Logger LOGGER = Logger.getLogger(ThreadUtilities.class);

	public static void waitForThreads(List<Thread> threads) {
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
