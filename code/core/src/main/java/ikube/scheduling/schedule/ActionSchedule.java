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
		Iterator<Action> iterator = server.getActions().iterator();
		while (iterator.hasNext()) {
			Action action = iterator.next();
			Action dbAction = dataBase.find(Action.class, action.getId());
			if (dbAction == null || dbAction.getEndTime() != null) {
				iterator.remove();
				LOGGER.info("Removing expired action : " + action);
				clusterManager.stopWorking(dbAction);
				clusterManager.putObject(server.getAddress(), server);
			}
		}
	}

}