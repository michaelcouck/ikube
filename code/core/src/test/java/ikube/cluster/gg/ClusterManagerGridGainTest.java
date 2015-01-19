package ikube.cluster.gg;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Server;
import mockit.MockClass;
import org.gridgain.grid.Grid;
import org.gridgain.grid.GridGain;
import org.gridgain.grid.GridIllegalStateException;
import org.gridgain.grid.cache.GridCache;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-08-2014
 */
@Ignore
public class ClusterManagerGridGainTest extends AbstractTest {

    @Mock
    private Grid grid;
    @Mock
    private GridCache gridCache;
    @Spy
    private Server server;
    @Spy
    @InjectMocks
    private ClusterManagerGridGain clusterManager;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        Mockito.when(grid.cache(Matchers.anyString())).thenReturn(gridCache);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void lock() throws Exception {
        Mockito.when(gridCache.lock(any(), anyInt())).thenReturn(Boolean.TRUE);
        boolean locked = clusterManager.lock(IConstants.IKUBE);
        assertTrue(locked);
    }

    @Test
    public void unlock() throws Exception {
        boolean unlocked = clusterManager.unlock(null);
        assertTrue(unlocked);
    }

    @Test
    public void anyWorking() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return servers;
            }
        }).when(clusterManager).getServers();
        boolean anyWorking = clusterManager.anyWorking();
        assertFalse(anyWorking);
        assertTrue(servers.size() > 0);
        for (final Server server : servers.values()) {
            verify(server.isWorking(), atLeastOnce());
        }
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