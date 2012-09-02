package ikube.cluster.hzc;

import ikube.listener.Event;
import ikube.toolkit.ThreadUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

/**
 * This class will listen to the cluster for termination events to stop indexing.
 * 
 * @author Michael Couck
 * @version 01.00
 * @since 24.08.12
 */
public class StopListener implements MessageListener<Object> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void onMessage(Message<Object> message) {
		// If this is a stop working message then find the future in the
		// thread utilities and kill it
		Object source = message.getSource();
		Object object = message.getMessageObject();
		if (object != null && Event.class.isAssignableFrom(object.getClass())) {
			Event event = (Event) object;
			if (Event.TERMINATE.equals(event.getType())) {
				logger.info("Got message : " + source + ", " + object);
				if (event.getObject() != null && String.class.isAssignableFrom(event.getObject().getClass())) {
					ThreadUtilities.destroy((String) event.getObject());
				}
			}
		}
	}

}
