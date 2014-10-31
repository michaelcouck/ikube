package ikube.cluster.gg;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Server;
import mockit.MockClass;
import org.gridgain.grid.Grid;
import org.gridgain.grid.GridGain;
import org.gridgain.grid.GridIllegalStateException;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-08-2014
 */
@Ignore
public class ClusterManagerTest extends AbstractTest {

    @Spy
    private Server server;
    @Spy
    @InjectMocks
    private ClusterManager clusterManager;

    @Before
    public void before() throws Exception {
        // Mockit.setUpMock(GridGainMock.class);
        clusterManager.initialize();
    }

    @After
    public void after() {
        // Mockit.tearDownMocks(GridGain.class);
    }

    @Test
    public void lock() {
        clusterManager.lock(IConstants.IKUBE);
    }

    @Test
    public void unlock() {
        clusterManager.unlock(null);
    }

    @Test
    public void anyWorking() {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return servers;
            }
        }).when(clusterManager).getServers();
        clusterManager.anyWorking();
    }

    @Test
    public void anyWorkingAction() {
        clusterManager.anyWorking(null);
    }

    @Test
    public void startWorking() {
        clusterManager.startWorking(null, null, null);
    }

    @Test
    public void stopWorking() {
        clusterManager.stopWorking(null);
    }

    @Test
    public void getServers() {
        clusterManager.getServers();
    }

    @Test
    public void getServer() {
        clusterManager.getServer();
    }

    @Test
    public void sendMessage() {
        clusterManager.sendMessage(null);
    }

    @Test
    public void sendTask() {
        clusterManager.sendTask(null);
    }

    @Test
    public void wrapFuture() {
        clusterManager.wrapFuture(null);
    }

    @Test
    public void sendTaskToServer() {
        clusterManager.sendTaskTo(null, null);
    }

    @Test
    public void sendTaskToAll() {
        clusterManager.sendTaskToAll(null);
    }

    @Test
    public void get() {
        clusterManager.get(null);
    }

    @Test
    public void put() {
        clusterManager.put(null, null);
    }

    @Test
    public void remove() {
        clusterManager.remove(null);
    }

    @Test
    public void clear() {
        clusterManager.clear(null);
    }

    @Test
    public void getMap() {
        clusterManager.get(null, null);
    }

    @Test
    public void putMap() {
        clusterManager.put(null, null, null);
    }

    @Test
    public void removeMap() {
        clusterManager.remove(null, null);
    }

    @Test
    public void destroy() {
        clusterManager.destroy();
    }

    @MockClass(realClass = GridGain.class)
    public static class GridGainMock {
        @mockit.Mock
        public static Grid grid(@Nullable String name) throws GridIllegalStateException {
            return null;
        }
    }

}