package ikube.listener;

import ikube.cluster.IClusterManager;
import ikube.model.Execution;
import ikube.model.Server;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Michael Couck
 * @since 08.10.11
 * @version 01.00
 */
public class MonitoringListener implements IListener {

	private IClusterManager	clusterManager;

	@Override
	public void handleNotification(Event event) {
		if (event.getType().equals(Event.PERFORMANCE)) {
			// Get the server
			Server server = clusterManager.getServer();
			// TODO Get the searching executions from the database, and calculate
			// TODO Get the indexing executions from the database, and calculate
			// Publish the server with the new data
			clusterManager.set(Server.class.getName(), server.getId(), server);
		}
	}

	protected void calculateStatistics(Map<String, Execution> executions) {
		for (Execution execution : executions.values()) {
			long duration = TimeUnit.NANOSECONDS.toSeconds(execution.getDuration());
			if (duration > 0) {
				execution.setExecutionsPerSecond((double) (execution.getInvocations() / duration));
			}
		}
	}

	public void setClusterManager(IClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

}
