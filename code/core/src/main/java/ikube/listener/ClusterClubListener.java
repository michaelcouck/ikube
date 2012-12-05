package ikube.listener;

import ikube.cluster.IClusterManager;
import ikube.model.Server;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This listener will just set the server age to stay in the cluster club. If the server times out the other servers will remove it from the
 * grid.
 * 
 * @author Michael Couck
 * @since 05.12.12
 * @version 01.00
 */
public class ClusterClubListener implements IListener {

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
		server.setAge(System.currentTimeMillis());
		clusterManager.putObject(server.getAddress(), server);
	}
}
