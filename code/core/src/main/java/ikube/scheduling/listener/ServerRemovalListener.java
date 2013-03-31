package ikube.scheduling.listener;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.Server;
import ikube.scheduling.Schedule;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This listener is for removing any servers that have timed out. We do not poll the servers, they send messages to maintain their status in
 * the cluster. If the timestamp of the server runs out then they are removed from each member in the cluster.
 * 
 * @author Michael Couck
 * @since 31.12.11
 * @version 01.00
 */
public class ServerRemovalListener extends Schedule {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerRemovalListener.class);

	@Autowired
	private IClusterManager clusterManager;

	@Override
	public void run() {
		// Remove all servers that are past the max age
		Collection<Server> servers = new ArrayList<Server>(clusterManager.getServers().values());
		for (final Server server : servers) {
			if (server.getAddress().equals(clusterManager.getServer().getAddress())) {
				continue;
			}
			long age = System.currentTimeMillis() - server.getAge();
			if (age > IConstants.MAX_AGE) {
				LOGGER.info("Removing server : " + server.getAddress() + ", age : " + (age > IConstants.MAX_AGE));
				clusterManager.remove(server.getAddress());
			}
		}
	}
}
