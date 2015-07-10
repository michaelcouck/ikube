package ikube.scheduling.schedule;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.Server;
import ikube.scheduling.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

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
    @Qualifier(value = "ikube.cluster.IClusterManager")
    private IClusterManager clusterManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Server local = clusterManager.getServer();
        // Remove all servers that are past the max age
        Collection<Server> servers = new ArrayList<>(clusterManager.getServers().values());
        for (final Server remote : servers) {
            if (remote.getAddress().equals(local.getAddress())) {
                continue;
            }
            long age = System.currentTimeMillis() - remote.getAge();
            boolean expired = age > IConstants.MAX_AGE;
            if (expired) {
                Date birthDate = new Date(remote.getAge());
                LOGGER.info("Removing expired server : " + remote.getAddress() + ", date : " + birthDate + ", age : " + (age / 1000));
                clusterManager.remove(IConstants.SERVER, remote.getAddress());
            }
        }
        // Finally put ourselves back in the grid
        local.setAge(System.currentTimeMillis());
        // LOGGER.info("Putting server back : " + new Date(server.getAge()));
        clusterManager.put(IConstants.SERVER, local.getAddress(), local);
    }
}