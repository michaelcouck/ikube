package ikube.listener;

import ikube.cluster.IClusterManager;
import ikube.model.Server;

/**
 * @author Michael Couck
 * @since 08.10.11
 * @version 01.00
 */
public class ActionListener implements IListener {

	private IClusterManager	clusterManager;

	@Override
	public void handleNotification(Event event) {
		if (!event.getType().equals(Event.ALIVE)) {
			return;
		}
		// Set our own server age
		Server server = clusterManager.getServer();
		server.setAge(System.currentTimeMillis());
		clusterManager.set(Server.class.getName(), server.getId(), server);
	}

	public void setClusterManager(IClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

}