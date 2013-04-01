package ikube.cluster.listener.hzc;

import ikube.cluster.listener.IListener;
import ikube.scheduling.schedule.Event;
import ikube.toolkit.ThreadUtilities;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

/**
 * This listener is to terminate the executer service, essentially aborting any actions that may be submitted, like indexing for example.
 * 
 * @author Michael Couck
 * @since 24.12.11
 * @version 01.00
 */
public class TerminationListener implements IListener<Message<Object>>, MessageListener<Object> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onMessage(final Message<Object> message) {
		Object object = message.getMessageObject();
		if (object != null && Event.class.isAssignableFrom(object.getClass())) {
			Event event = (Event) object;
			if (Event.TERMINATE.equals(event.getType())) {
				ThreadUtilities.destroy();
			}
		}
	}

}
