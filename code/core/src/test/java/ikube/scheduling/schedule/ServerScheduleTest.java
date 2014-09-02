package ikube.scheduling.schedule;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.Server;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.mockito.Mockito.*;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 13-08-2014
 */
public class ServerScheduleTest extends AbstractTest {

    @Spy
    @InjectMocks
    private ServerSchedule serverSchedule;
    @Mock
    private IClusterManager clusterManager;
    @Mock
    private Server remote;

    @Test
    @SuppressWarnings("rawtypes")
    public void run() {
        when(remote.getAddress()).thenReturn("127.0.0.1-8001");
        when(remote.getAge()).thenReturn(System.currentTimeMillis() - IConstants.MAX_AGE - 1);

        when(clusterManager.getServer()).thenReturn(server);
        when(clusterManager.getServers()).thenReturn(servers);
        setInternalState(serverSchedule, "clusterManager", clusterManager);

        servers.put(remote.getAddress(), remote);

        serverSchedule.run();

        verify(clusterManager, atLeastOnce()).remove(IConstants.SERVER, remote.getAddress());
        verify(clusterManager, atLeastOnce()).put(IConstants.SERVER, server.getAddress(), server);
    }

}