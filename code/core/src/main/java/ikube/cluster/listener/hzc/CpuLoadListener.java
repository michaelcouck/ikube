package ikube.cluster.listener.hzc;

import ikube.cluster.IClusterManager;
import ikube.cluster.listener.IListener;
import ikube.model.Server;
import ikube.scheduling.schedule.Event;

import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20.05.13
 */
public class CpuLoadListener implements IListener<Message<Object>>, MessageListener<Object> {

	@Autowired
	private IClusterManager clusterManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onMessage(final Message<Object> message) {
		Object object = message.getMessageObject();
		if (object != null && Event.class.isAssignableFrom(object.getClass())) {
			Event event = (Event) object;
			if (event.isConsumed()) {
				return;
			}
			event.setConsumed(Boolean.TRUE);
			if (Event.CPU_LOAD_THROTTLING_GOGGLE.equals(event.getType())) {
				event.setConsumed(Boolean.TRUE);
				Server server = clusterManager.getServer();
				server.setCpuThrottling(!server.isCpuThrottling());
				// clusterManager.put(server.getAddress(), server);
			}
		}
	}

}
