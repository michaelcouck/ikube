package ikube.listener;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.Server;

import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * This listener will respond to clean events and it will remove servers that have not checked in, i.e. their sell by date is expired.
 * 
 * @author Michael Couck
 * @since 08.10.11
 * @version 01.00
 */
@Deprecated
public class ClusterListener implements IListener {

	private static final Logger LOGGER = Logger.getLogger(ClusterListener.class);

	private IClusterManager clusterManager;

	@Override
	public void handleNotification(Event event) {
		if (!event.getType().equals(Event.CLEAN)) {
			return;
		}
		Server server = clusterManager.getServer();
		// Remove all servers that are past the max age
		Collection<Server> servers = clusterManager.getServers().values();
		for (Server remoteServer : servers) {
			if (remoteServer.getAddress().equals(server.getAddress())) {
				continue;
			}
			if (System.currentTimeMillis() - remoteServer.getAge() > IConstants.MAX_AGE) {
				LOGGER.info("Removing server : " + remoteServer + ", "
						+ (System.currentTimeMillis() - remoteServer.getAge() > IConstants.MAX_AGE));
				// clusterManager.remove(Server.class.getName(), remoteServer.getId());
			}
		}
	}

	public void setClusterManager(IClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}
}