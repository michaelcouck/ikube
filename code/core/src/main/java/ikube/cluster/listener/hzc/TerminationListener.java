package ikube.cluster.listener.hzc;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TerminationListener.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onMessage(final Message<Object> message) {
		Object object = message.getMessageObject();
		if (object != null && Event.class.isAssignableFrom(object.getClass())) {
			Event event = (Event) object;
			if (!event.isConsumed() && Event.TERMINATE.equals(event.getType())) {
				LOGGER.info("Terminating schedules : " + ToStringBuilder.reflectionToString(event, ToStringStyle.SHORT_PREFIX_STYLE));
				event.setConsumed(Boolean.TRUE);
				ThreadUtilities.destroy();
			}
		}
	}

}
