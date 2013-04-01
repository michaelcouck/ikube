package ikube.toolkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

/**
 * This class just has a method that will wait for a list of threads to finish and an executer service that will execute 'threads' and
 * return futures that can be waited for by the callers.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public final class ThreadUtilities {

	private static final Logger LOGGER = Logger.getLogger(ThreadUtilities.class);

	/** Executes the 'threads' and returns a future. */
	private static ExecutorService EXECUTER_SERVICE;
	/** The number of times to try to cancel the future(s). */
	private static int MAX_RETRY_COUNT = 3;
	/** A list of futures by name so we can kill them. */
	private static Map<String, List<Future<?>>> FUTURES;

	/**
	 * This method will submit the runnable and add it to a map so the caller can cancel the future if necessary.
	 * 
	 * @param name the name to assign to the future to be able to cancel it if necessary
	 * @param runnable the runnable to schedule for running
	 * @return the future that is being submitted to execute
	 */
	public synchronized static Future<?> submit(final String name, final Runnable runnable) {
		try {
			if (EXECUTER_SERVICE == null || EXECUTER_SERVICE.isShutdown()) {
				LOGGER.info("Executer service is shutdown : " + runnable);
				return null;
			}
			Future<?> future = EXECUTER_SERVICE.submit(runnable);
			if (name != null) {
				getFutures(name).add(future);
			} else {
				getFutures(ThreadUtilities.class.getSimpleName()).add(future);
			}
			return future;
		} finally {
			ThreadUtilities.class.notifyAll();
		}
	}

	/**
	 * This method will terminate the future(s) with the specified name, essentially interrupting it and remove it from the list. In the
	 * case where this future is running an action the action will terminate abruptly. Note that futures typically run in groups of three or
	 * four, and are keyed by the name, so all the futures in the group need to be cancelled.
	 * 
	 * @param name the name that was assigned to the future when it was submitted for execution
	 */
	public static synchronized void destroy(final String name) {
		try {
			List<Future<?>> futures = getFutures(name);
			if (futures != null) {
				for (final Future<?> future : futures) {
					if (future == null || future.isDone()) {
						LOGGER.warn("No such future or completed already : " + future + ", " + name);
						return;
					}
					int maxRetryCount = MAX_RETRY_COUNT;
					while (maxRetryCount-- > 0) {
						if (future.cancel(true) || future.isCancelled()) {
							LOGGER.info("Cancelled future : " + name + ", " + future + ", " + maxRetryCount);
							break;
						}
						ThreadUtilities.sleep(10);
					}
					if (!future.isCancelled() && !future.isDone()) {
						LOGGER.warn("Couldn't cancel future : " + name + ", " + future + ", " + FUTURES.size() + ", " + maxRetryCount);
					}
				}
			}
			futures.clear();
		} finally {
			ThreadUtilities.class.notifyAll();
		}
	}

	/**
	 * This method will wait for all the futures to finish their logic.
	 * 
	 * @param futures the futures to wait for in seconds
	 * @param maxWait and the maximum amount of time to wait
	 */
	public static void waitForFutures(final List<Future<?>> futures, final long maxWait) {
		for (Future<?> future : futures) {
			ThreadUtilities.waitForFuture(future, maxWait);
		}
	}

	/**
	 * This method will wait for the future to finish doing it's work. In the event the future is interrupted, for example by the executer
	 * service closing down and interrupting all it's threads, it will return immediately. If the future takes too long and passes the
	 * maximum wait time, then the method will return immediately.
	 * 
	 * @param future the future to wait for in seconds
	 * @param maxWait the maximum amount of time to wait
	 */
	public static void waitForFuture(final Future<?> future, final long maxWait) {
		if (future == null) {
			LOGGER.debug("Future null returning : ");
			return;
		}
		try {
			future.get(maxWait, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOGGER.debug(null, e);
			LOGGER.warn("Coitus interruptus... : " + e.getMessage());
		} catch (TimeoutException e) {
			LOGGER.info("Timed out waiting for future : " + e.getMessage());
		} catch (Exception e) {
			LOGGER.error("Exception waiting for future : ", e);
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
						thread.join(1000);
					} catch (InterruptedException e) {
						LOGGER.error("Interrupted waiting for thread : " + thread + ", this thread : " + Thread.currentThread(), e);
					}
					continue outer;
				}
			}
			break;
		}
	}

	/**
	 * This method will just sleep for the specified time without the interrupted exception needing to be caught.
	 * 
	 * @param sleep the time for the current thread to sleep
	 */
	public static void sleep(final long sleep) {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			LOGGER.error("Sleep interrupted : " + Thread.currentThread());
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

	/**
	 * This method initializes the executer service, and the thread pool that will execute runnables.
	 */
	public static void initialize() {
		if (EXECUTER_SERVICE != null && !EXECUTER_SERVICE.isShutdown()) {
			LOGGER.info("Executer service already initialized : ");
			return;
		}
		FUTURES = new HashMap<String, List<Future<?>>>();
		EXECUTER_SERVICE = Executors.newCachedThreadPool();
	}

	/**
	 * This method will destroy the thread pool. All threads that are currently running will be interrupted,and should catch this exception
	 * and exit the run method.
	 */
	public static synchronized void destroy() {
		try {
			if (EXECUTER_SERVICE == null || EXECUTER_SERVICE.isShutdown()) {
				LOGGER.info("Executer service already shutdown : ");
				return;
			}
			Collection<String> futureNames = new ArrayList<String>(FUTURES.keySet());
			for (String futureName : futureNames) {
				destroy(futureName);
			}
			EXECUTER_SERVICE.shutdown();
			try {
				int maxRetryCount = MAX_RETRY_COUNT;
				while (!EXECUTER_SERVICE.awaitTermination(10, TimeUnit.SECONDS) && maxRetryCount-- > 0) {
					List<Runnable> runnables = EXECUTER_SERVICE.shutdownNow();
					LOGGER.info("Shutdown runnables : " + runnables);
					LOGGER.info("Still waiting to shutdown : ");
					EXECUTER_SERVICE.shutdown();
				}
			} catch (InterruptedException e) {
				LOGGER.error("Executer service thread interrupted : ", e);
				// Preserve interrupt status
				Thread.currentThread().interrupt();
			}
			List<Runnable> runnables = EXECUTER_SERVICE.shutdownNow();
			LOGGER.info("Shutdown runnables : " + runnables);
			FUTURES.clear();
			FUTURES = null;
			EXECUTER_SERVICE = null;
		} finally {
			ThreadUtilities.class.notifyAll();
		}
	}

	protected static synchronized List<Future<?>> getFutures(final String name) {
		try {
			List<Future<?>> futures = FUTURES.get(name);
			if (futures == null) {
				futures = new ArrayList<Future<?>>();
				FUTURES.put(name, futures);
			}
			return futures;
		} finally {
			ThreadUtilities.class.notifyAll();
		}
	}

	protected static synchronized Map<String, List<Future<?>>> getFutures() {
		try {
			Map<String, List<Future<?>>> futures = new HashMap<String, List<Future<?>>>();
			futures.putAll(FUTURES);
			return futures;
		} finally {
			ThreadUtilities.class.notifyAll();
		}
	}

	public ThreadUtilities() {
		// Documented
	}

}