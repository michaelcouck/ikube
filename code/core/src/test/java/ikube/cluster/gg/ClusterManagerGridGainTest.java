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

import java.util.HashMap;
import java.util.Map;

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
public class ClusterManagerGridGainTest extends AbstractTest {

    @MockClass(realClass = GridGain.class)
    public static class GridGainMock {
        @mockit.Mock
        public static Grid grid(@Nullable String name) throws GridIllegalStateException {
            return Mockito.mock(Grid.class);
        }
    }

    @Mock
    private Grid grid;
    @Mock
    private GridCache gridCache;
    @Mock
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
    @Ignore
    public void anyWorking() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map<String, Server> servers = new HashMap<String, Server>();
                servers.put("192.168.1.40", server);
                return servers;
            }
        }).when(clusterManager).getServers();
        boolean anyWorking = clusterManager.anyWorking();
        assertFalse(anyWorking);
        assertTrue(servers.size() > 0);
        for (final Server server : servers.values()) {
            verify(server, atLeastOnce()).isWorking();
        }
    }

    @Test
    @Ignore
    public void anyWorkingAction() {
        clusterManager.anyWorking(null);
    }

    @Test
    @Ignore
    public void startWorking() {
        clusterManager.startWorking(null, null, null);
    }

    @Test
    @Ignore
    public void stopWorking() {
        clusterManager.stopWorking(null);
    }

    @Test
    @Ignore
    public void getServers() {
        clusterManager.getServers();
    }

    @Test
    @Ignore
    public void getServer() {
        clusterManager.getServer();
    }

    @Test
    @Ignore
    public void sendMessage() {
        clusterManager.sendMessage(null);
    }

    @Test
    @Ignore
    public void sendTask() {
        clusterManager.sendTask(null);
    }

    @Test
    @Ignore
    public void wrapFuture() {
        clusterManager.wrapFuture(null);
    }

    @Test
    @Ignore
    public void sendTaskToServer() {
        clusterManager.sendTaskTo(null, null);
    }

    @Test
    @Ignore
    public void sendTaskToAll() {
        clusterManager.sendTaskToAll(null);
    }

    @Test
    @Ignore
    public void get() {
        clusterManager.get(null);
    }

    @Test
    @Ignore
    public void put() {
        clusterManager.put(null, null);
    }

    @Test
    @Ignore
    public void remove() {
        clusterManager.remove(null);
    }

    @Test
    @Ignore
    public void clear() {
        clusterManager.clear(null);
    }

    @Test
    @Ignore
    public void getMap() {
        clusterManager.get(null, null);
    }

    @Test
    @Ignore
    public void putMap() {
        clusterManager.put(null, null, null);
    }

    @Test
    @Ignore
    public void removeMap() {
        clusterManager.remove(null, null);
    }

    @Test
    @Ignore
    public void destroy() {
        clusterManager.destroy();
    }

}