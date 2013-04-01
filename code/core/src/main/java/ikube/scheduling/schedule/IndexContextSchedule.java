package ikube.scheduling.schedule;

import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.model.IndexContext;
import ikube.scheduling.Schedule;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class constantly publishes it's index contexts to the other servers that may be new to the cluster.
 * 
 * @author Michael Couck
 * @since 10.09.12
 * @version 01.00
 */
public class IndexContextSchedule extends Schedule {

	@Autowired
	private IMonitorService monitorService;
	@Autowired
	private IClusterManager clusterManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void run() {
		// Check the database for new index contexts
		Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
		for (IndexContext indexContext : indexContexts.values()) {
			// Send messages to all the other servers on the index contexts in this server
			clusterManager.sendMessage(indexContext);
		}
	}

}