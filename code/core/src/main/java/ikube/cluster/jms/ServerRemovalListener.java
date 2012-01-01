package ikube.cluster.jms;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.model.Server;

import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This listener is for removing any servers that have times out. We do not poll the servers, they send messages to maintain their status in
 * the cluster. If the timestamp of the server runs out then they are removed from each member in the cluster.
 * 
 * @author Michael Couck
 * @since 31.12.11
 * @version 01.00
 */
public class ServerRemovalListener implements IListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerRemovalListener.class);

	@Autowired
	private IClusterManager clusterManager;

	@Override
	public void handleNotification(Event event) {
		if (!Event.SERVER_RELEASE.equals(event.getType())) {
			return;
		}
		// Remove all servers that are past the max age
		Collection<Server> servers = clusterManager.getServers().values();
		Iterator<Server> iterator = servers.iterator();
		while (iterator.hasNext()) {
			Server server = iterator.next();
			if (System.currentTimeMillis() - server.getAge() > IConstants.MAX_AGE) {
				LOGGER.info("Removing server : " + server + ", " + (System.currentTimeMillis() - server.getAge() > IConstants.MAX_AGE));
				iterator.remove();
			}
		}
	}
}
