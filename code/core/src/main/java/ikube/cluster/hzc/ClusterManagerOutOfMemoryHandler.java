package ikube.cluster.hzc;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.OutOfMemoryHandler;
import ikube.toolkit.THREAD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * On an out of memory we terminate the server immediately, there is no recovering from this.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 07-03-2014
 */
public class ClusterManagerOutOfMemoryHandler extends OutOfMemoryHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterManagerOutOfMemoryHandler.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOutOfMemory(final OutOfMemoryError oom, final HazelcastInstance[] hazelcastInstances) {
        System.err.println("Out of memory, exiting jobs with extreme prejudice : ");
        // First we'll try to destroy everything
        THREAD.destroy();
        THREAD.sleep(60000);
        // Now dump all the threads to the log
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        LOGGER.info("Threads : " + threads.size());
        for (final Map.Entry<Thread, StackTraceElement[]> mapEntry : threads.entrySet()) {
            Thread thread = mapEntry.getKey();
            LOGGER.info("        thread : " + thread);
            StackTraceElement[] stackTraceElements = mapEntry.getValue();
            for (final StackTraceElement stackTraceElement : stackTraceElements) {
                LOGGER.info(stackTraceElement.toString());
            }
        }
        try {
            for (final HazelcastInstance hazelcastInstance : hazelcastInstances) {
                new ClusterManagerHazelcastStatistics().printStatistics(hazelcastInstance);
            }
        } catch (final Throwable e) {
            LOGGER.error("Oops...", e);
        }
        // System.exit(1);
    }

}
