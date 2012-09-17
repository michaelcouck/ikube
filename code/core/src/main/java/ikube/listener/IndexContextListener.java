package ikube.listener;

import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.service.IMonitorService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class constantly publishes it's index contexts to the other servers that may be new to the cluster.
 * 
 * @author Michael Couck
 * @since 10.09.12
 * @version 01.00
 */
public class IndexContextListener implements IListener {

	@Autowired
	private IMonitorService monitorService;
	@Autowired
	private IClusterManager clusterManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void handleNotification(Event event) {
		if (!Event.TIMER.equals(event.getType())) {
			return;
		}
		// Check the database for new index contexts
		Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
		for (IndexContext indexContext : indexContexts.values()) {
			// Send messages to all the other servers on the index contexts in this server
			clusterManager.sendMessage(indexContext);
		}
	}

}