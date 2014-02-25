package ikube;

import com.hazelcast.core.Hazelcast;
import ikube.cluster.IClusterManager;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ThreadUtilities;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class IClusterManagerIntegration extends IntegrationTest {

    public static class CallableImpl implements Callable {
        @Override
        public Object call() throws Exception {
            return Boolean.TRUE;
        }
    }

    private IClusterManager clusterManager;

    @Before
    public void before() {
        clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
    }

    @Test
    public void sendTask() {
        Future<?> future = clusterManager.sendTask(new CallableImpl());
        ThreadUtilities.waitForFuture(future, Integer.MAX_VALUE);
        assertNotNull("There should be one execution only : ", future);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void sendTaskToAll() throws Exception {
        List<Future<?>> futures = clusterManager.sendTaskToAll(new CallableImpl() {
            @Override
            public Object call() throws Exception {
                return Boolean.TRUE;
            }
        });
        ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
        for (final Future<?> future : futures) {
            assertTrue((Boolean) future.get());
        }
        int sizeOfCluster = Hazelcast.getAllHazelcastInstances().iterator().next().getCluster().getMembers().size();
        logger.info("Executions : " + futures.size() + ", cluster size : " + sizeOfCluster);
        assertEquals("There should be one execution per node in the cluster : ", sizeOfCluster, futures.size());
    }

}