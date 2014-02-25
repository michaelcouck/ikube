package ikube;

import ikube.cluster.IClusterManager;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ThreadUtilities;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class IClusterManagerIntegration extends IntegrationTest {

    public static class CallableImpl implements Callable {
        @Override
        public Object call() throws Exception {
            return null;
        }
    }

    private IClusterManager clusterManager;

    @Before
    public void before() {
        clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
    }

    @Test
    public void sendTask() {
        final List<Boolean> executions = new ArrayList<>();
        Future<?> future = clusterManager.sendTask(new CallableImpl() {
            @Override
            public Object call() throws Exception {
                executions.add(Boolean.TRUE);
                return null;
            }
        });
        ThreadUtilities.waitForFuture(future, Integer.MAX_VALUE);
        logger.info("Executions : " + executions.size());
        assertEquals("There should be one execution only : ", 1, executions.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void sendTaskToAll() throws Exception {
        int sizeOfCluster = clusterManager.getServers().size();
        final List<Boolean> executions = new ArrayList<>();
        List<Future<?>> futures = clusterManager.sendTaskToAll(new CallableImpl() {
            @Override
            public Object call() throws Exception {
                return Boolean.TRUE;
            }
        });
        ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
        for (final Future<?> future : futures) {
            executions.add((Boolean) future.get());
        }
        logger.info("Executions : " + executions.size());
        assertEquals("There should be one execution per node in the cluster : ", sizeOfCluster, executions.size());
    }

}