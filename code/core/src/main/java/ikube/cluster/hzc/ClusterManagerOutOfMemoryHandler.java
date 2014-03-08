package ikube.cluster.hzc;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.OutOfMemoryHandler;

/**
 * On an out of memory we terminate the server immediately, there is no revocering from this.
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
        System.err.println("Out of memory, exiting jvm with extreme prejudice : ");
        System.exit(1);
    }

}
