package ikube.scheduling.schedule;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.Server;
import ikube.scheduling.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * This listener is for removing any servers that have timed out. We do not poll the servers, they send messages
 * to maintain their status in the cluster. If the timestamp of the server runs out then they are removed from each
 * member in the cluster.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 31-12-2011
 */
public class ServerSchedule extends Schedule {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSchedule.class);

    @Autowired
    private IClusterManager clusterManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        // Remove all servers that are past the max age
        Collection<Server> servers = new ArrayList<>(clusterManager.getServers().values());
        for (final Server server : servers) {
            if (server.getAddress().equals(clusterManager.getServer().getAddress())) {
                continue;
            }
            long age = System.currentTimeMillis() - server.getAge();
            boolean expired = age > IConstants.MAX_AGE;
            if (expired) {
                Object[] parameters = {server.getAddress(), new Date(server.getAge()), (age / 1000), expired};
                LOGGER.info("Removing expired server : {}, date : {}, age : {}, expited : {}", parameters);
                clusterManager.remove(server.getAddress());
            }
        }
        // Finally put ourselves back in the grid
        Server server = clusterManager.getServer();
        server.setAge(System.currentTimeMillis());
        // LOGGER.info("Putting server back : " + new Date(server.getAge()));
        clusterManager.put(IConstants.SERVER, server.getAddress(), server);
    }
}