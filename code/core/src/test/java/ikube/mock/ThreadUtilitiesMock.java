package ikube.mock;

import ikube.toolkit.ThreadUtilities;
import mockit.Mock;
import mockit.MockClass;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 30-01-2014
 */
@SuppressWarnings("UnusedParameters")
@MockClass(realClass = ThreadUtilities.class)
public class ThreadUtilitiesMock {

    @Mock()
    public static ForkJoinPool executeForkJoinTasks(final String name, final int threads, final ForkJoinTask<?>... forkJoinTasks) {
        return null;
    }

    @Mock()
    public static void waitForFuture(final Future<?> future, final long maxWait) {
    }
}