package ikube.cluster;

import com.hazelcast.core.Hazelcast;
import ikube.IntegrationTest;
import ikube.model.Task;
import ikube.toolkit.THREAD;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * This test will check the grid scheduling of tasks. Typically there will be a server
 * started, and along with this instance of ikube, there should be at least two nodes in the cluster
 * which affords a suitable test environment.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 25-02-2014
 */
@Ignore
@SuppressWarnings("SpringJavaAutowiringInspection")
public class ClusterManagerIntegration extends IntegrationTest {

    @Autowired
    private IClusterManager clusterManager;

    @Test
    public void sendTask() {
        Future<?> future = clusterManager.sendTask(new Task());
        THREAD.waitForFuture(future, Integer.MAX_VALUE);
        assertNotNull("There should be one execution only : ", future);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void sendTaskToAll() throws Exception {
        List<Future<Object>> futures = clusterManager.sendTaskToAll(new Task());
        THREAD.waitForFutures(futures, Integer.MAX_VALUE);
        for (final Future<?> future : futures) {
            assertTrue((Boolean) future.get());
        }
        int sizeOfCluster = Hazelcast.getAllHazelcastInstances().iterator().next().getCluster().getMembers().size();
        logger.info("Executions : " + futures.size() + ", cluster size : " + sizeOfCluster);
        assertEquals("There should be one execution per node in the cluster : ", sizeOfCluster, futures.size());
    }

}