package ikube.cluster.jms;

import ikube.cluster.IClusterManager;
import ikube.listener.Event;
import ikube.listener.IListener;

import org.springframework.beans.factory.annotation.Autowired;

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