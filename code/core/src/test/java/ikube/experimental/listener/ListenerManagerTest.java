package ikube.experimental.listener;

import ikube.cluster.gg.ClusterManagerGridGain;
import ikube.experimental.AbstractTest;
import ikube.experimental.Context;
import ikube.toolkit.THREAD;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-07-2015
 */
public class ListenerManagerTest extends AbstractTest {

    @Mock
    private Context context;
    @Mock
    private IEvent<?, ?> event;
    @Spy
    @InjectMocks
    private ListenerManager listenerManager;
    @Mock
    private IListener<IEvent<?, ?>> listener;
    @Mock
    private Map<String, List<IListener<IEvent<?, ?>>>> listeners;
    @Mock
    private ClusterManagerGridGain clusterManager;

    @BeforeClass
    public static void beforeClass() {
        THREAD.initialize();
    }

    @AfterClass
    public static void afterClass() {
        THREAD.destroy();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add() {
        // IListener<IEvent<?, ?>>, String, String
        listenerManager.add(listener, IEvent.class.getSimpleName(), "real-time-search");
        logger.info("Listeners : " + listeners);
        verify(listeners, times(1)).put(any(String.class), any(List.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addQueueListener() {
        listenerManager.addQueueListener();
        verify(clusterManager, times(1)).addQueueListener(any(String.class), any(IListener.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addTopicListener() {
        listenerManager.addTopicListener();
        verify(clusterManager, times(1)).addTopicListener(any(String.class), any(IListener.class));
    }

    @Test
    public void fire() {
        when(event.getContext()).thenReturn(context);
        when(event.getContext().getName()).thenReturn("context-name");
        when(listeners.get(any(String.class))).thenReturn(Arrays.asList(listener));

        listenerManager.fire(event, true);
        THREAD.sleep(100);

        verify(listener, times(1)).notify(any(IEvent.class));
        verify(listenerManager, times(1)).notify(any(IEvent.class));

        listenerManager.fire(event, false);
        THREAD.sleep(100);
        verify(clusterManager, times(1)).send(any(), any());
    }

    @Test
    public void get() {
        List<IListener<IEvent<?, ?>>> listeners = listenerManager.get("type", "name");
        assertNotNull(listeners);
    }

    @Test
    public void notifyListeners() {
        when(event.getContext()).thenReturn(context);
        when(event.getContext().getName()).thenReturn("context-name");
        when(listeners.get(any(String.class))).thenReturn(Arrays.asList(listener));

        listenerManager.notify(event);
        THREAD.sleep(100);
        verify(listener, times(1)).notify(any(IEvent.class));
    }

    @Test
    public void remove() {
        // IListener<IEvent<?, ?>>, String, String
        listenerManager.remove(listener, "event-type", "context-name");
        verify(listeners, times(1)).get(any(Object.class));
    }

}