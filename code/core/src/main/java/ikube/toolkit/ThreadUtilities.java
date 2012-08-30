package ikube.toolkit;

import ikube.IConstants;

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

	/**
	 * This class will iterate over the futures and remove the ones that are finished.
	 * 
	 * @author Michael Couck
	 * @since 24.08.12
	 * @version 01.00
	 */
	static class Cleaner implements Runnable {
		public void run() {
			while (true) {
				ThreadUtilities.sleep(60000);
				Collection<String> futureNames = new ArrayList<String>(FUTURES.keySet());
				for (String futureName : futureNames) {
					List<Future<?>> futures = FUTURES.get(futureName);
					for (Future<?> future : futures) {
						if (future.isCancelled() || future.isDone()) {
							futures.remove(future);
							LOGGER.info("Removed future : " + future);
						}
					}
				}
			}
		}
	}

	/** Executes the 'threads' and returns a future. */
	private static ExecutorService EXECUTER_SERVICE;
	/** A list of futures by name so we can kill them. */
	private static final Map<String, List<Future<?>>> FUTURES = new HashMap<String, List<Future<?>>>();

	/**
	 * This method will submit the runnable and add it to a map so the caller can cancel the future if necessary.
	 * 
	 * @param name the name to assign to the future to be able to cancel it if necessary
	 * @param runnable the runnable to schedule for running
	 * @return the future that is being submitted to execute
	 */
	public static Future<?> submit(final String name, final Runnable runnable) {
		Future<?> future = submit(runnable);
		getFutures(name).add(future);
		LOGGER.info("Submit future : " + name);
		return future;
	}

	private static List<Future<?>> getFutures(final String name) {
		List<Future<?>> futures = FUTURES.get(name);
		if (futures == null) {
			futures = new ArrayList<Future<?>>();
			FUTURES.put(name, futures);
		}
		return futures;
	}

	/**
	 * This method submits a runnable with the executer service from the concurrent package and returns the future immediately.
	 * 
	 * @param runnable the runnable to execute
	 * @return the future that will be a handle to the thread running the runnable
	 */
	public static Future<?> submit(final Runnable runnable) {
		if (EXECUTER_SERVICE == null || EXECUTER_SERVICE.isShutdown()) {
			LOGGER.info("Executer service is shutdown : " + runnable);
			return null;
		}
		return EXECUTER_SERVICE.submit(runnable);
	}

	/**
	 * This method initializes the executer service, and the thread pool that will execute runnables.
	 */
	public static void initialize() {
		if (EXECUTER_SERVICE != null && !EXECUTER_SERVICE.isShutdown()) {
			LOGGER.info("Executer service already initialized : ");
			return;
		}
		EXECUTER_SERVICE = Executors.newFixedThreadPool(IConstants.THREAD_POOL_SIZE);
		submit(new Cleaner());
	}

	/**
	 * This method will terminate the future(s) with the specified name, essentially interrupting it and remove it from the list. In the
	 * case where this future is running an action the action will terminate abruptly. Note that futures typically run in groups of three or
	 * four, and are keyed by the name, so all the futures in the group need to be cancelled.
	 * 
	 * @param name the name that was assigned to the future when it was submitted for execution
	 */
	public static void destroy(final String name) {
		List<Future<?>> futures = FUTURES.remove(name);
		if (futures != null) {
			for (Future<?> future : futures) {
				if (future == null) {
					LOGGER.warn("No such future : " + name);
					return;
				}
				future.cancel(true);
				LOGGER.info("Destroyed and removed future : " + name + ", " + future);
			}
		}
	}

	/**
	 * This method will destroy the thread pool. All threads that are currently running will be interrupted,and should catch this exception
	 * and exit the run method.
	 */
	public static void destroy() {
		if (EXECUTER_SERVICE == null || EXECUTER_SERVICE.isShutdown()) {
			LOGGER.info("Executer service already shutdown : ");
			return;
		}
		Collection<String> futureNames = new ArrayList<String>(FUTURES.keySet());
		for (String futureName : futureNames) {
			destroy(futureName);
		}
		EXECUTER_SERVICE.shutdown();
		List<Runnable> runnables = EXECUTER_SERVICE.shutdownNow();
		EXECUTER_SERVICE = null;
		LOGGER.info("Shutdown runnables : " + runnables);
	}

	/**
	 * This method will wait for all the futures to finish their logic.
	 * 
	 * @param futures the futures to wait for
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
	 * @param future the future to wait for
	 * @param maxWait the maximum amount of time to wait
	 */
	public static void waitForFuture(final Future<?> future, final long maxWait) {
		if (future == null) {
			LOGGER.info("Future null returning : ");
			return;
		}
		long start = System.currentTimeMillis();
		while (!future.isDone()) {
			try {
				future.get(maxWait, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				String message = "Coitus interruptus... : " + e.getMessage();
				LOGGER.warn(message);
				LOGGER.debug(message, e);
			} catch (TimeoutException e) {
				LOGGER.info("Timed out wairing for future : " + e.getMessage());
			} catch (Exception e) {
				LOGGER.error("Exception waiting for future : ", e);
			}
			ThreadUtilities.sleep(1000);
			if ((System.currentTimeMillis() - start) > maxWait * 1000) {
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

	public static void sleep(final long sleep) {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			LOGGER.error("Sleep interrupted : " + Thread.currentThread());
			throw new RuntimeException(e);
		}
	}

	/**
	 * Singularity.
	 */
	private ThreadUtilities() {
		// Documented
	}

}
