package ikube.scheduling.schedule;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.Action;
import ikube.model.Server;
import ikube.scheduling.Schedule;
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
        boolean changed = Boolean.FALSE;
        while (iterator.hasNext()) {
            Action gridAction = iterator.next();
            if (gridAction.getEndTime() != null) {
                iterator.remove();
                changed = Boolean.TRUE;
                logger.info("Removing expired action : " + gridAction);
            }
            Action dbAction = dataBase.find(Action.class, gridAction.getId());
            if (dbAction == null || dbAction.getEndTime() != null) {
                iterator.remove();
                changed = Boolean.TRUE;
                logger.info("Removing expired action : " + gridAction);
            }
            logger.info("Grid action : " + gridAction + ", db action : " + dbAction);
        }
        // If the server has been changed the pop it back in the grid
        if (changed) {
            clusterManager.put(IConstants.SERVER, server.getAddress(), server);
        }
    }

}