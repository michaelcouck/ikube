package ikube.listener;

import ikube.cluster.IClusterManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This listener will just send the server object into the cluster to maintain the alive status in the cluster.
 * 
 * @author Michael Couck
 * @since 31.12.11
 * @version 01.00
 */
public class ServerClubListener implements IListener {

	@Autowired
	private IClusterManager clusterManager;

	@Override
	public void handleNotification(Event event) {
		if (!Event.SERVER_CLUB.equals(event.getType())) {
			return;
		}
		clusterManager.sendMessage(clusterManager.getServer());
	}
}