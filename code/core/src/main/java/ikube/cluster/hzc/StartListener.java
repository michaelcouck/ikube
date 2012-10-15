package ikube.cluster.hzc;

import ikube.cluster.IClusterManager;
import ikube.listener.Event;
import ikube.listener.ListenerManager;
import ikube.model.IndexContext;
import ikube.service.IMonitorService;
import ikube.toolkit.ThreadUtilities;

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
public class StartListener implements MessageListener<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(StartListener.class);

	@Autowired
	private IMonitorService monitorService;
	@Autowired
	private IClusterManager clusterManager;
	@Autowired
	private ListenerManager listenerManager;

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
			String indexName = event.getObject().toString();
			IndexContext<?> indexContext = monitorService.getIndexContexts().get(indexName);
			indexContext.setMaxAge(0);
		} else if (Event.STARTUP_ALL.equals(event.getType())) {
			LOGGER.info("Re-starting the indexing threads");
			ThreadUtilities.initialize();
		}
	}

}