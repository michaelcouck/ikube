package ikube.scheduling.schedule;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.Action;
import ikube.model.Server;
import ikube.scheduling.Schedule;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
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
    private IClusterManager clusterManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Server server = clusterManager.getServer();
        List<Action> actions = new ArrayList<>();
        for (final Action action : server.getActions()) {
            if (action.getEndTime() == null) {
                actions.add(action);
            }
        }
        server.setActions(actions);
        // If the server has been changed the pop it back in the grid
        clusterManager.put(IConstants.SERVER, server.getAddress(), server);
    }

}