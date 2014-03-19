package ikube.cluster.hzc;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.OutOfMemoryHandler;

/**
 * On an out of memory we terminate the server immediately, there is no recovering from this.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 07-03-2014
 */
public class ClusterManagerOutOfMemoryHandler extends OutOfMemoryHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOutOfMemory(final OutOfMemoryError oom, final HazelcastInstance[] hazelcastInstances) {
        try {
            for (final HazelcastInstance hazelcastInstance : hazelcastInstances) {
                ClusterManagerHazelcast.printStatistics(hazelcastInstance);
            }
        } catch (final Throwable e) {
            e.printStackTrace();
        }
        System.err.println("Out of memory, exiting jvm with extreme prejudice : ");
        // System.exit(1);
    }

}
