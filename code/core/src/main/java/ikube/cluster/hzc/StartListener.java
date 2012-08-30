package ikube.cluster.hzc;

import ikube.cluster.IClusterManager;
import ikube.listener.Event;
import ikube.listener.ListenerManager;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.service.IMonitorService;
import ikube.toolkit.ThreadUtilities;

import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class StartListener implements MessageListener<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(StartListener.class);

	@Autowired
	private IClusterManager clusterManager;
	@Autowired
	private IMonitorService monitorService;
	@Autowired
	private ListenerManager listenerManager;

	@Override
	public void onMessage(Message<Object> message) {
		Object object = message.getMessageObject();
		if (object == null || !Event.class.isAssignableFrom(object.getClass())) {
			return;
		}
		final Event event = (Event) object;
		if (Event.STARTUP.equals(event.getType())) {
			LOGGER.info("Manually starting indexing : " + ToStringBuilder.reflectionToString(event, ToStringStyle.SHORT_PREFIX_STYLE));
			ThreadUtilities.submit(new Runnable() {
				public void run() {
					boolean indexing = false;
					while (!indexing) {
						try {
							String indexName = event.getObject().toString();
							IndexContext<?> indexContext = monitorService.getIndexContexts().get(indexName);
							long maxAge = indexContext.getMaxAge();
							indexContext.setMaxAge(0);
							listenerManager.fireEvent(Event.TIMER, System.currentTimeMillis(), null, Boolean.FALSE);
							ThreadUtilities.sleep(10000);
							// Check if the action is started
							indexContext.setMaxAge(maxAge);
							for (Map.Entry<String, Server> mapEntry : clusterManager.getServers().entrySet()) {
								for (Action action : mapEntry.getValue().getActions()) {
									if (action.getIndexName().equals(indexName)) {
										indexing = true;
										break;
									}
								}
							}
						} catch (Exception e) {
							LOGGER.error("Exception trying to start the indexing : " + event, e);
						}
					}
				}
			});
		}
	}

}