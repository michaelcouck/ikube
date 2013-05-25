package ikube.scheduling.schedule;

import ikube.cluster.IClusterManager;
import ikube.model.Server;
import ikube.scheduling.Schedule;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This listener will just set the server age to stay in the cluster club. If the server times out the other servers will remove it from the
 * grid.
 * 
 * @author Michael Couck
 * @since 05.12.12
 * @version 01.00
 */
@Deprecated
public class ClusterSchedule extends Schedule {

	@Autowired
	private IClusterManager clusterManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		Server server = clusterManager.getServer();
		server.setAge(System.currentTimeMillis());
		// clusterManager.put(server.getAddress(), server);
	}
}
