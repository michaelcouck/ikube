package ikube.scheduling.schedule;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Persistable;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 22-07-2014
 */
public class PruneScheduleTest extends AbstractTest {

    private PruneSchedule pruneSchedule;

    @Before
    public void before() throws Exception {
        pruneSchedule = new PruneSchedule();
        Deencapsulation.setField(pruneSchedule, dataBase);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void internalExecute() {
        List<Object> entities = new ArrayList<>();
        when(dataBase.find(any(Class.class), any(String[].class), any(Boolean[].class), anyInt(), anyInt())).thenReturn(entities, new ArrayList<>());

        addEntities(1, entities);
        pruneSchedule.run();
        verify(dataBase, atMost(0)).remove(any());

        when(dataBase.find(any(Class.class), any(String[].class), any(Boolean[].class), anyInt(), anyInt())).thenReturn(entities, new ArrayList<>());
        when(dataBase.count(any(Class.class))).thenReturn(IConstants.MAX_ACTIONS * 10, 0l);
        addEntities(IConstants.MAX_ACTIONS + 10000, entities);
        pruneSchedule.run();
        verify(dataBase, atLeastOnce()).remove(any());
    }

    private void addEntities(final long iterations, final List<Object> entities) {
        for (long i = iterations; i > 0; i--) {
            entities.add(mock(Persistable.class));
        }
    }

}