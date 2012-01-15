package ikube.toolkit;

import ikube.IConstants;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * This class just has a method that will wait for a list of threads to finish.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public final class ThreadUtilities {

	private static final Logger LOGGER = Logger.getLogger(ThreadUtilities.class);

	private static ExecutorService EXECUTER_SERVICE;

	/**
	 * Singularity.
	 */
	private ThreadUtilities() {
		// Documented
	}

	/**
	 * This method submits a runnable with the executer service from the concurrent package and returns the future immediately.
	 * 
	 * @param runnable the runnable to execute
	 * @return the future that will be a handle to the thread running the runnable
	 */
	public static Future<?> submit(Runnable runnable) {
		if (EXECUTER_SERVICE.isShutdown()) {
			LOGGER.warn("Executer service already shutdown : " + runnable);
			return null;
		}
		return EXECUTER_SERVICE.submit(runnable);
	}

	/**
	 * This method initializes the executer service, and the thread pool that will execute runnables.
	 */
	public static void initialize() {
		ThreadUtilities.destroy();
		EXECUTER_SERVICE = Executors.newFixedThreadPool(IConstants.THREAD_POOL_SIZE);
	}

	/**
	 * This method will destroy the thread pool. All threads that are currently running will be interrupted,and should catch this exception
	 * and exit the run method.
	 */
	public static void destroy() {
		if (EXECUTER_SERVICE == null || EXECUTER_SERVICE.isShutdown()) {
			LOGGER.warn("Executer service already shutdown : ");
			return;
		}
		EXECUTER_SERVICE.shutdown();
		List<Runnable> runnables = EXECUTER_SERVICE.shutdownNow();
		LOGGER.info("Shutdown runnables : " + runnables);
	}

	/**
	 * This method will wait for all the futures to finish their logic.
	 * 
	 * @param futures the futures to wait for
	 * @param maxWait and the maximum amount of time to wait
	 */
	public static void waitForFutures(List<Future<?>> futures, long maxWait) {
		for (Future<?> future : futures) {
			ThreadUtilities.waitForFuture(future, maxWait);
		}
	}

	/**
	 * 
	 * @param future
	 * @param maxWait
	 */
	public static void waitForFuture(Future<?> future, long maxWait) {
		if (future == null) {
			LOGGER.warn("Future null returning : ");
			return;
		}
		long start = System.currentTimeMillis();
		while (!future.isDone()) {
			try {
				future.get(maxWait, TimeUnit.SECONDS);
			} catch (Exception e) {
				LOGGER.error("Exception waiting for future : ", e);
			}
			ThreadUtilities.sleep(1000);
			LOGGER.debug("Future : " + future);
			if ((System.currentTimeMillis() - start) > maxWait) {
				break;
			}
		}
	}

	/**
	 * This method iterates through the list of threads looking for one that is still alive and joins it. Once all the threads have finished
	 * then this method will return to the caller indicating that all the threads have finished.
	 * 
	 * @param threads the threads to wait for
	 */
	public static void waitForThreads(final Collection<Thread> threads) {
		if (threads == null) {
			LOGGER.warn("Threads null : ");
			return;
		}
		outer: while (true) {
			for (Thread thread : threads) {
				if (thread.isAlive()) {
					try {
						thread.join(10000);
					} catch (InterruptedException e) {
						LOGGER.error("Interrupted waiting for thread : " + thread + ", this thread : " + Thread.currentThread(), e);
					}
					continue outer;
				}
			}
			break;
		}
	}

	public static void sleep(long sleep) {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			LOGGER.error("Sleep interrupted : " + Thread.currentThread(), e);
			throw new RuntimeException(e);
		}
	}

}
