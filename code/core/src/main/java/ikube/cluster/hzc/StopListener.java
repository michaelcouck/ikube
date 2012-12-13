package ikube.cluster.hzc;

import ikube.listener.Event;
import ikube.toolkit.ThreadUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

/**
 * This class will listen to the cluster for termination events to stop indexing. In the first case it will destroy the thread(s) that is
 * running a particular job, which could be an indexing job, which will terminate the job gracefully. In the second case it will destroy the
 * thread pool which will then terminate all jobs, also gracefully.
 * 
 * @author Michael Couck
 * @version 01.00
 * @since 24.08.12
 */
public class StopListener implements MessageListener<Object> {

	@SuppressWarnings("unused")
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private ThreadUtilities threadUtilities;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onMessage(Message<Object> message) {
		// If this is a stop working message then find the future in the thread utilities and kill it
		@SuppressWarnings("unused")
		Object source = message.getSource();
		Object object = message.getMessageObject();
		if (object != null && Event.class.isAssignableFrom(object.getClass())) {
			// logger.info("Got message : " + source + ", " + object);
			Event event = (Event) object;
			if (Event.TERMINATE.equals(event.getType())) {
				Object indexName = event.getObject();
				if (indexName != null && String.class.isAssignableFrom(indexName.getClass())) {
					ThreadUtilities.destroy((String) indexName);
				}
			} else if (Event.TERMINATE_ALL.equals(event.getType())) {
				// logger.info("Terminating all indexing : ");
				threadUtilities.destroy();
			}
		}
	}

}
