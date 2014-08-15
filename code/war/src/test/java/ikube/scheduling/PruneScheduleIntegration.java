package ikube.scheduling;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.database.IDataBase;
import ikube.model.Action;
import ikube.scheduling.schedule.PruneSchedule;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 12-02-2014
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class PruneScheduleIntegration extends IntegrationTest {

    @Autowired
    private IDataBase dataBase;
    @Autowired
    private PruneSchedule prune;

    @After
    public void after() {
        delete(dataBase, Action.class);
    }

    @Test
    public void execute() throws Exception {
        int startIndex = 0;
        int maxResults = 10;
        List<Action> actions = dataBase.find(Action.class, startIndex, maxResults);
        logger.warn("Actions : " + actions.size());
        assertEquals("There should be no actions in the database : ", 0, actions.size());

        persistAction(1);
        actions = dataBase.find(Action.class, startIndex, maxResults);
        logger.warn("Actions : " + actions.size());
        assertEquals("There should be one action in the database : ", 1, actions.size());

        prune.run();
        actions = dataBase.find(Action.class, startIndex, maxResults);
        logger.warn("Actions : " + actions.size());
        assertEquals("There should be one action in the database : ", 1, actions.size());

        persistAction((int) IConstants.MAX_ACTIONS + 10);
        actions = dataBase.find(Action.class, 0, Integer.MAX_VALUE);
        logger.warn("Actions : " + actions.size());
        assertTrue("There should be a lot of actions in the database : ", actions.size() > IConstants.MAX_ACTIONS);

        prune.run();
        actions = dataBase.find(Action.class, 0, Integer.MAX_VALUE);
        assertTrue("There should be less actions in the database than the maximum : ", IConstants.MAX_ACTIONS >= actions.size());
    }

    private void persistAction(int inserts) {
        for (int i = 0; i < inserts; i++) {
            Action action = new Action();
            action.setActionName("actionName");
            action.setDuration(System.currentTimeMillis());
            action.setIndexableName("indexableName");
            action.setIndexName("indexName");
            action.setStartTime(new Timestamp(System.currentTimeMillis()));
            action.setEndTime(new Timestamp(System.currentTimeMillis()));
            action.setResult(Boolean.TRUE);
            if (i % 1000 == 0) {
                logger.warn("Inserts : " + i);
            }
            dataBase.persist(action);
        }
    }

}