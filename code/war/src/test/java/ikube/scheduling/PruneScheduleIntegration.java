package ikube.scheduling;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.Action;
import ikube.scheduling.schedule.PruneSchedule;
import ikube.toolkit.ApplicationContextManager;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 12-02-2014
 */
public class PruneScheduleIntegration extends IntegrationTest {

    private IDataBase dataBase;
    private PruneSchedule prune;

    @Before
    public void before() throws Exception {
        prune = new PruneSchedule();
        dataBase = ApplicationContextManager.getBean(IDataBase.class);
        Deencapsulation.setField(prune, dataBase);
        delete(dataBase, Action.class);
    }

    @Test
    public void execute() throws Exception {
        int startIndex = 0;
        int maxResults = 10;
        List<Action> actions = dataBase.find(Action.class, startIndex, maxResults);
        assertEquals("There should be no actions in the database : ", 0, actions.size());

        persistAction(1);

        actions = dataBase.find(Action.class, startIndex, maxResults);
        assertEquals("There should be one action in the database : ", 1, actions.size());

        prune.run();
        actions = dataBase.find(Action.class, startIndex, maxResults);
        assertEquals("There should be one action in the database : ", 1, actions.size());

        persistAction((int) IConstants.MAX_ACTIONS + 100);

        actions = dataBase.find(Action.class, 0, Integer.MAX_VALUE);
        assertTrue("There should be a lot of actions in the database : ", actions.size() > IConstants.MAX_ACTIONS);

        prune.run();
        actions = dataBase.find(Action.class, 0, Integer.MAX_VALUE);
        assertTrue("There should be less actions in the database than the maximum : ", IConstants.MAX_ACTIONS >= actions.size());
    }

    private void persistAction(int inserts) {
        List<Action> actions = new ArrayList<Action>();
        for (int i = 0; i < inserts; i++) {
            Action action = new Action();
            action.setActionName("actionName");
            action.setDuration(System.currentTimeMillis());
            action.setEndTime(new Timestamp(System.currentTimeMillis()));
            action.setIndexableName("indexableName");
            action.setIndexName("indexName");
            action.setStartTime(new Timestamp(System.currentTimeMillis()));
            action.setResult(Boolean.TRUE);
            actions.add(action);
            if (actions.size() > 1000) {
                dataBase.persistBatch(actions);
                actions.clear();
            }
        }
        dataBase.persistBatch(actions);

    }

}