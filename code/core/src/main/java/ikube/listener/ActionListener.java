package ikube.listener;

import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.Action;
import ikube.model.Server;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Michael Couck
 * @since 10.09.12
 * @version 01.00
 */
public class ActionListener implements IListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActionListener.class);

	@Autowired
	private IDataBase dataBase;
	@Autowired
	private IClusterManager clusterManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleNotification(Event event) {
		if (!Event.TIMER.equals(event.getType())) {
			return;
		}
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