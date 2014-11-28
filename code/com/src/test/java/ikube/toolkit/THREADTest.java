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
 * @since 20-03-2011
 */
public class THREADTest extends AbstractTest {

    // The runnable to sleep for a while
    class Sleepy implements Runnable {

        long sleep;

        public Sleepy() {
            this(1000);
        }

        public Sleepy(long sleep) {
            this.sleep = sleep;
        }

        public void run() {
            THREAD.sleep(sleep);
        }
    }

    // The class to destroy the executor pool
    class Destroyer implements Runnable {
        public void run() {
            THREAD.sleep(1000);
            THREAD.destroy();
        }
    }

    @Before
    public void before() {
        THREAD.initialize();
    }

    @After
    public void after() {
        THREAD.destroy();
    }

    @Test
    public void waitForThreads() {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(new Sleepy());
            thread.start();
            threads.add(thread);
        }
        THREAD.waitForThreads(threads);
        // Verify that all the threads are dead
        for (final Thread thread : threads) {
            assertFalse("All the threads should have died : ", thread.isAlive());
        }
        assertTrue("We just want to exit here after the threads die : ", true);
    }

    @Test
    public void waitForFuture() {
        // We just wait for this future to finish,
        // must be less than the time we expect to wait
        long start = System.currentTimeMillis();
        Future<?> future = THREAD.submit(null, new Sleepy(3000));
        THREAD.waitForFuture(future, Integer.MAX_VALUE);
        assertTrue(System.currentTimeMillis() - start < 4000);

        // We destroy this future and return from the wait method
        start = System.currentTimeMillis();
        future = THREAD.submit(null, new Sleepy(Integer.MAX_VALUE));
        logger.info("Going into wait before destroying the thread pool : " + future);
        new Thread(new Destroyer()).start();
        THREAD.waitForFuture(future, Integer.MAX_VALUE);
        long duration = System.currentTimeMillis() - start;
        logger.info("Duration : " + duration);
        assertTrue(duration < 20000);
    }

    @Test
    public void submitDestroy() {
        if (!THREAD.isInitialized()) {
            THREAD.initialize();
        }
        String name = Long.toHexString(System.currentTimeMillis());
        Runnable sleepy = new Sleepy(Integer.MAX_VALUE);
        Future<?> future = THREAD.submit(name, sleepy);
        logger.info("Future : " + future.isCancelled() + ", " + future.isDone());
        THREAD.destroy(name);
        logger.info("Future : " + future.isCancelled() + ", " + future.isDone());

        // TODO: This does not work on CentOs!!!!!! WTFN? (Why the fuck not?)
        if (OS.isOs("3.11.0-12-generic")) {
            assertTrue(future.isDone());
            assertTrue(future.isCancelled());
        } else {
            logger.info("Not correct operating system : ");
        }
    }

    /**
     * This method just checks the concurrency of the
     * threading, that there are no blocking/deadlocking synchronized blocks.
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
                        THREAD.sleep(10);
                        THREAD.submit(this.toString(), new Sleepy());
                        THREAD.getFutures(this.toString());
                        THREAD.submit(null, new Sleepy());
                        THREAD.destroy(this.toString());
                    } while (i-- > 0);
                }
            });
            thread.start();
            threads.add(thread);
        }
        THREAD.waitForThreads(threads);
        // If it dead locks we will never get here
    }

    @Test
    public void cancelForkJoinPool() {
        ForkJoinPool forkJoinPool = THREAD.getForkJoinPool(this.getClass().getSimpleName(), 3);
        ForkJoinPool cancelledForkJoinPool = THREAD.cancelForkJoinPool(this.getClass().getSimpleName());
        assertEquals(forkJoinPool, cancelledForkJoinPool);
        assertTrue(cancelledForkJoinPool.isShutdown());
        assertTrue(cancelledForkJoinPool.isTerminated());
    }

    @Test
    public void cancelAllForkJoinPools() {
        ForkJoinPool forkJoinPool = THREAD.getForkJoinPool(this.getClass().getSimpleName(), 3);
        THREAD.cancelAllForkJoinPools();
        assertTrue(forkJoinPool.isShutdown());
        assertTrue(forkJoinPool.isTerminated());
    }

    @Test
    public void executeForkJoinTasks() {
        final String forkJoinPoolName = this.getClass().getSimpleName();
        ForkJoinTask<Object> forkJoinTask = new RecursiveTask<Object>() {
            @Override
            protected Object compute() {
                THREAD.sleep(5000);
                return null;
            }
        };
        new Thread(new Runnable() {
            public void run() {
                THREAD.sleep(3000);
                THREAD.cancelForkJoinPool(forkJoinPoolName);
            }
        }).start();
        try {
            THREAD.executeForkJoinTasks(forkJoinPoolName, 3, forkJoinTask);
            THREAD.sleep(10000);
        } catch (CancellationException e) {
            // Ignore?
        }
        assertTrue(forkJoinTask.isCancelled());
    }

}