package ikube.toolkit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.scheduling.listener.Event;
import ikube.scheduling.listener.ListenerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This test if for the thread utilities, which has methods to wait for threads etc.
 * 
 * @author Michael Couck
 * @since 20.03.11
 * @version 01.00
 */
public class ThreadUtilitiesTest extends ATest {

	// The runnable to sleep for a while
	class Sleepy implements Runnable {

		long sleep;

		public Sleepy(long sleep) {
			this.sleep = sleep;
		}

		public Sleepy() {
			this(1000);
		}

		public void run() {
			ThreadUtilities.sleep(sleep);
		}
	}

	// The class to destroy the executer pool
	class Destroyer implements Runnable {
		public void run() {
			ThreadUtilities.sleep(1000);
			new ThreadUtilities().destroy();
		}
	}

	public ThreadUtilitiesTest() {
		super(ThreadUtilitiesTest.class);
	}

	@Before
	public void before() {
		new ThreadUtilities().initialize();
	}

	@After
	public void after() {
		new ThreadUtilities().destroy();
	}

	@Test
	public void waitForThreads() {
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < 3; i++) {
			Thread thread = new Thread(new Sleepy());
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

	@Test
	public void waitForFuture() {
		// We just wait for this future to finish, must be less than the time we expect to wait
		long start = System.currentTimeMillis();
		Future<?> future = ThreadUtilities.submit(new Sleepy(3000));
		ThreadUtilities.waitForFuture(future, 3);
		assertTrue(System.currentTimeMillis() - start < 4000);

		// We destroy this future and return from the wait method
		start = System.currentTimeMillis();
		future = ThreadUtilities.submit(new Sleepy(Integer.MAX_VALUE));
		logger.info("Going into wait before destroying the thread pool : " + future);
		new Thread(new Destroyer()).start();
		ThreadUtilities.waitForFuture(future, Integer.MAX_VALUE);
		long duration = System.currentTimeMillis() - start;
		logger.info("Duration : " + duration);
		assertTrue(duration < 20000);
	}

	@Test
	public void submitDestroy() {
		String name = "name";
		Future<?> future = ThreadUtilities.submit(name, new Sleepy(Integer.MAX_VALUE));
		logger.info("Future : " + future.isCancelled() + ", " + future.isDone());

		ThreadUtilities.sleep(3000);
		ThreadUtilities.destroy(name);
		ThreadUtilities.sleep(3000);
		logger.info("Future : " + future.isCancelled() + ", " + future.isDone());

		assertTrue(future.isDone());
		assertTrue(future.isCancelled());
	}

	/** This method just checks the concurrency of the threading, that there are no blocking synchronized blocks. */
	@Test
	public void multiThreaded() {
		final int iterations = 100;
		final ListenerManager listenerManager = new ListenerManager();
		listenerManager.addListener(new ThreadUtilities());
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < 10; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					int i = iterations;
					while (i-- > 0) {
						ThreadUtilities.sleep(10);
						ThreadUtilities.submit(this.toString(), new Sleepy());
						ThreadUtilities.getFutures(this.toString());
						ThreadUtilities.getFutures();
						ThreadUtilities.submit(new Sleepy());
						ThreadUtilities.destroy(this.toString());
						listenerManager.fireEvent(Event.TIMER, System.currentTimeMillis(), null, Boolean.FALSE);
					}
				}
			});
			thread.start();
			threads.add(thread);
		}
		ThreadUtilities.waitForThreads(threads);
		Map<String, List<Future<?>>> allFutures = ThreadUtilities.getFutures();
		for (Map.Entry<String, List<Future<?>>> mapEntry : allFutures.entrySet()) {
			logger.info("Future list : " + mapEntry.getKey() + ", " + mapEntry.getValue().size());
		}
	}

}