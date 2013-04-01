package ikube.cluster.listener.hzc;

import ikube.cluster.listener.IListener;
import ikube.scheduling.schedule.Event;
import ikube.toolkit.ThreadUtilities;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

/**
 * Similar to the termination listener, this listener waits for an event triggering the start up of the executer service, to allow actions
 * to be submitted for execution.
 * 
 * @author Michael couck
 * @since 24.12.11
 * @version 01.00
 */
public class StartupListener implements IListener<Message<Object>>, MessageListener<Object> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onMessage(final Message<Object> message) {
		Object object = message.getMessageObject();
		if (object != null && Event.class.isAssignableFrom(object.getClass())) {
			Event event = (Event) object;
			if (Event.STARTUP.equals(event.getType())) {
				ThreadUtilities.initialize();
			}
		}
	}

}
