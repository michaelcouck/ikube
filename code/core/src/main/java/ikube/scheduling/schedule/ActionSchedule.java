package ikube.scheduling.schedule;

import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.Action;
import ikube.model.Server;
import ikube.scheduling.Schedule;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This schedule will look at the database and find actions that have got an end date, indicating that they have finished. Then go to the
 * grid and if they are present remove them from the grid, probably because the grid provider threw an exception and the action was not
 * removed, or because the server instance that owns the action went down hard and the action remains int he grid.
 * 
 * @author Michael Couck
 * @since 10.09.12
 * @version 01.00
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
		Action action = null;
		for (final Iterator<Action> iterator = server.getActions().iterator(); iterator.hasNext(); action = iterator.next()) {
			Action dbAction = dataBase.find(Action.class, action.getId());
			if (dbAction == null || dbAction.getEndTime() != null) {
				LOGGER.info("Removing expired action : " + action);
				iterator.remove();
				clusterManager.stopWorking(dbAction);
				clusterManager.put(server.getAddress(), server);
			}
		}
	}

}