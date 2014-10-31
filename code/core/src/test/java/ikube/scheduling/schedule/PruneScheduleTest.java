package ikube.scheduling.schedule;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Persistable;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

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

    @Spy
    @InjectMocks
    private PruneSchedule pruneSchedule;

    @Test
    @SuppressWarnings("unchecked")
    public void internalExecute() {
        List<Object> entities = new ArrayList<>();
        when(dataBase.find(any(Class.class), any(String[].class), any(Boolean[].class), anyInt(), anyInt()))
                .thenReturn(entities, new ArrayList<>());

        addEntities(1, entities);
        pruneSchedule.run();
        verify(dataBase, times(0)).removeBatch(any(List.class));

        when(dataBase.find(any(Class.class), any(String[].class), any(Boolean[].class), anyInt(), anyInt()))
                .thenReturn(entities, new ArrayList<>());
        when(dataBase.count(any(Class.class))).thenReturn(IConstants.MAX_ACTIONS * 10, 0l);
        addEntities(IConstants.MAX_ACTIONS + 10, entities);
        pruneSchedule.run();
        verify(dataBase, times(1)).removeBatch(any(List.class));
    }

    private void addEntities(final long iterations, final List<Object> entities) {
        for (long i = iterations; i > 0; i--) {
            entities.add(mock(Persistable.class));
        }
    }

}