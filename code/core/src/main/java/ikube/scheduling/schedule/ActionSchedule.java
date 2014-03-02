package ikube.scheduling.schedule;

import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.Action;
import ikube.model.Server;
import ikube.scheduling.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.List;

/**
 * This schedule will look at the database and find actions that have got an end date, indicating that they
 * have finished. Then go to the grid and if they are present remove them from the grid, probably because the
 * grid provider threw an exception and the action was not removed, or because the server instance that owns the
 * action went down hard and the action remains int he grid.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-09-2012
 */
public class ActionSchedule extends Schedule {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionSchedule.class);

    @Autowired
    private IDataBase dataBase;
    @Autowired
    private IClusterManager clusterManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Server server = clusterManager.getServer();
        List<Action> actions = server.getActions();
        Iterator<Action> iterator = actions.iterator();
        while (iterator.hasNext()) {
            Action gridAction = iterator.next();
            Action dbAction = dataBase.find(Action.class, gridAction.getId());
            if (gridAction.getEndTime() != null || dbAction == null || dbAction.getEndTime() != null) {
                LOGGER.info("Removing expired action : {}", gridAction);
                iterator.remove();
            }
        }
    }

}