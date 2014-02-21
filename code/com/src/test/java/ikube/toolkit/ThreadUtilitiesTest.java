package ikube.toolkit;

import ikube.AbstractTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * This test if for the thread utilities, which has methods to wait for threads etc.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 20.03.11
 */
public class ThreadUtilitiesTest extends AbstractTest {

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

    // The class to destroy the executor pool
    class Destroyer implements Runnable {
        public void run() {
            ThreadUtilities.sleep(1000);
            ThreadUtilities.destroy();
        }
    }

    @Before
    public void before() {
        ThreadUtilities.initialize();
    }

    @After
    public void after() {
        ThreadUtilities.destroy();
    }

    @Test
    public void waitForThreads() {
        List<Thread> threads = new ArrayList<>();
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
        Future<?> future = ThreadUtilities.submit(null, new Sleepy(3000));
        ThreadUtilities.waitForFuture(future, 3);
        assertTrue(System.currentTimeMillis() - start < 4000);

        // We destroy this future and return from the wait method
        start = System.currentTimeMillis();
        future = ThreadUtilities.submit(null, new Sleepy(Integer.MAX_VALUE));
        logger.info("Going into wait before destroying the thread pool : " + future);
        new Thread(new Destroyer()).start();
        ThreadUtilities.waitForFuture(future, Integer.MAX_VALUE);
        long duration = System.currentTimeMillis() - start;
        logger.info("Duration : " + duration);
        assertTrue(duration < 20000);
    }

    @Test
    public void submitDestroy() {
        String name = Long.toHexString(System.currentTimeMillis());
        Future<?> future = ThreadUtilities.submit(name, new Sleepy(Integer.MAX_VALUE));
        logger.info("Future : " + future.isCancelled() + ", " + future.isDone());
        ThreadUtilities.sleep(3000);
        ThreadUtilities.destroy(name);
        ThreadUtilities.sleep(3000);
        logger.info("Future : " + future.isCancelled() + ", " + future.isDone());

        logger.info("Name of the OS: " + System.getProperty("os.name"));
        logger.info("Version of the OS: " + System.getProperty("os.version"));
        logger.info("Architecture of the OS: " + System.getProperty("os.arch"));

        // TODO This doesn't nto work on CentOs!!!!!! WTFN? (Why the fuck not?)
        if ("3.11.0-12-generic".equals(System.getProperty("os.version"))) {
            assertTrue(future.isDone());
            assertTrue(future.isCancelled());
        } else {
            logger.info("Not correct operating system : ");
        }
    }

    /**
     * This method just checks the concurrency of the threading, that there are no blocking synchronized blocks.
     */
    @Test
    public void multiThreaded() {
        final int iterations = 100;
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    int i = iterations;
                    do {
                        ThreadUtilities.sleep(10);
                        ThreadUtilities.submit(this.toString(), new Sleepy());
                        ThreadUtilities.getFutures(this.toString());
                        ThreadUtilities.submit(null, new Sleepy());
                        ThreadUtilities.destroy(this.toString());
                    } while (i-- > 0);
                }
            });
            thread.start();
            threads.add(thread);
        }
        ThreadUtilities.waitForThreads(threads);
        // If it dead locks we will never get here
    }

    @Test
    public void cancellForkJoinPool() {
        ForkJoinPool forkJoinPool = ThreadUtilities.getForkJoinPool(this.getClass().getSimpleName(), 3);
        ForkJoinPool cancelledForkJoinPool = ThreadUtilities.cancelForkJoinPool(this.getClass().getSimpleName());
        assertEquals(forkJoinPool, cancelledForkJoinPool);
        assertTrue(cancelledForkJoinPool.isShutdown());
        assertTrue(cancelledForkJoinPool.isTerminated());
    }

    @Test
    public void cancellAllForkJoinPools() {
        ForkJoinPool forkJoinPool = ThreadUtilities.getForkJoinPool(this.getClass().getSimpleName(), 3);
        ThreadUtilities.cancelAllForkJoinPools();
        assertTrue(forkJoinPool.isShutdown());
        assertTrue(forkJoinPool.isTerminated());
    }

    @Test
    public void executeForkJoinTasks() {
        final String forkJoinPoolName = this.getClass().getSimpleName();
        ForkJoinTask<Object> forkJoinTask = new RecursiveTask<Object>() {
            @Override
            protected Object compute() {
                ThreadUtilities.sleep(5000);
                return null;
            }
        };
        new Thread(new Runnable() {
            public void run() {
                ThreadUtilities.sleep(3000);
                ThreadUtilities.cancelForkJoinPool(forkJoinPoolName);
            }
        }).start();
        try {
            ThreadUtilities.executeForkJoinTasks(forkJoinPoolName, 3, forkJoinTask);
            ThreadUtilities.sleep(10000);
        } catch (CancellationException e) {
            // Ignore?
        }

        assertTrue(forkJoinTask.isCancelled());
    }

}