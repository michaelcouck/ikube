package ikube.cluster.jms;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.model.Server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ServerRemovalListener implements IListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerRemovalListener.class);

	@Autowired
	private IClusterManager clusterManager;

	@Override
	public void handleNotification(Event event) {
		if (!Event.SERVER_RELEASE.equals(event.getType())) {
			return;
		}
		Server server = clusterManager.getServer();
		// Remove all servers that are past the max age
		List<Server> toRemove = new ArrayList<Server>();
		Collection<Server> servers = clusterManager.getServers().values();
		for (Server remoteServer : servers) {
			if (remoteServer.getAddress().equals(server.getAddress())) {
				continue;
			}
			if (System.currentTimeMillis() - remoteServer.getAge() > IConstants.MAX_AGE) {
				LOGGER.info("Removing server : " + remoteServer + ", "
						+ (System.currentTimeMillis() - remoteServer.getAge() > IConstants.MAX_AGE));
				toRemove.add(remoteServer);
			}
		}
		for (Server toRemoveServer : toRemove) {
			servers.remove(toRemoveServer);
		}
	}
}
