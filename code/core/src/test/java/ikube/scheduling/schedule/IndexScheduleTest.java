package ikube.scheduling.schedule;

import ikube.AbstractTest;
import ikube.action.IAction;
import ikube.action.Index;
import ikube.cluster.IMonitorService;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.toolkit.THREAD;
import mockit.Deencapsulation;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 12-10-2010
 */
public class IndexScheduleTest extends AbstractTest {

    @Mock
    private Index index;
    @Spy
    @InjectMocks
    private IndexSchedule indexSchedule;
    @Mock
    private IMonitorService monitorService;

    @Before
    @SuppressWarnings("rawtypes")
    public void before() {
        THREAD.initialize();
        List<IAction<IndexContext, Boolean>> actions = new ArrayList<>();

        Map<String, IndexContext> indexContexts = new HashMap<>();
        indexContexts.put(indexContext.getName(), indexContext);
        when(monitorService.getIndexContexts()).thenReturn(indexContexts);

        actions.add(index);

        when(server.isWorking()).thenReturn(Boolean.FALSE);
        when(clusterManager.getServer()).thenReturn(server);

        Deencapsulation.setField(indexSchedule, "actions", actions);
        Deencapsulation.setField(indexSchedule, "monitorService", monitorService);

        Mockit.setUpMocks(ApplicationContextManagerMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(ApplicationContextManagerMock.class);
    }

    @Test
    public void handleNotification() throws Exception {
        Event event = new Event();
        event.setTimestamp(System.currentTimeMillis());
        event.setType(Event.TIMER);

        indexSchedule.run();
        THREAD.sleep(3000);
        verify(index, times(1)).execute(any(IndexContext.class));

        when(server.isWorking()).thenReturn(Boolean.TRUE);
        indexSchedule.run();
        THREAD.sleep(3000);
        verify(index, times(2)).execute(any(IndexContext.class));
    }

}