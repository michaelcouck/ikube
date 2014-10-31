package ikube.scheduling.schedule;

import ikube.AbstractTest;
import ikube.IConstants;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import java.util.Arrays;

import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 13-08-2014
 */
public class ActionScheduleTest extends AbstractTest {

    @Spy
    @InjectMocks
    private ActionSchedule actionSchedule;

    @Test
    @SuppressWarnings("rawtypes")
    public void run() {
        when(clusterManager.getServer()).thenReturn(server);
        when(server.getActions()).thenReturn(Arrays.asList(action));
        when(action.getEndTime()).thenReturn(null);

        actionSchedule.run();

        verify(clusterManager, atLeastOnce()).put(IConstants.SERVER, server.getAddress(), server);
    }

}