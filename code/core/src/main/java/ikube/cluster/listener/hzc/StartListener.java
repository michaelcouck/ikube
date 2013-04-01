package ikube.cluster.listener.hzc;

import ikube.action.index.IndexManager;
import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.cluster.listener.IListener;
import ikube.model.IndexContext;
import ikube.scheduling.schedule.Event;
import ikube.toolkit.ThreadUtilities;

import java.io.File;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

/**
 * This class starts the indexing mechanism. In the first case it will set the max age for the index context to 0 which will trigger the
 * indexing process by one of the servers in the cluster. In the second case it just starts the thread pool that executes the indexing and
 * jobs.
 * 
 * @author Michael Couck
 * @since 30.08.12
 * @version 01.00
 */
public class StartListener implements IListener<Message<Object>>, MessageListener<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(StartListener.class);

	@Autowired
	private IMonitorService monitorService;
	@Autowired
	private IClusterManager clusterManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onMessage(Message<Object> message) {
		Object object = message.getMessageObject();
		if (object == null || !Event.class.isAssignableFrom(object.getClass())) {
			return;
		}
		final Event event = (Event) object;
		if (Event.STARTUP.equals(event.getType())) {
			LOGGER.info("Manually starting indexing : " + ToStringBuilder.reflectionToString(event, ToStringStyle.SHORT_PREFIX_STYLE));
			final String indexName = event.getObject().toString();
			final IndexContext<?> indexContext = monitorService.getIndexContexts().get(indexName);
			final long maxAge = indexContext.getMaxAge();
			indexContext.setMaxAge(0);
			// Start a thread to revert the max age of the index
			ThreadUtilities.submit(null, new Runnable() {
				public void run() {
					File newLatestIndexDirectory = null;
					File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
					do {
						if (latestIndexDirectory == null) {
							break;
						}
						ThreadUtilities.sleep(10000);
						newLatestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
						LOGGER.info("Latest : " + latestIndexDirectory + ", new latest : " + newLatestIndexDirectory);
					} while (latestIndexDirectory.equals(newLatestIndexDirectory));
					LOGGER.info("Setting the max age back to the original : " + maxAge);
					indexContext.setMaxAge(maxAge);
				}
			});
		} else if (Event.STARTUP_ALL.equals(event.getType())) {
			LOGGER.info("Re-starting the indexing threads");
			ThreadUtilities.initialize();
		}
	}

}