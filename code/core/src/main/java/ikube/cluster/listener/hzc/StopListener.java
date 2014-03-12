package ikube.cluster.listener.hzc;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import ikube.cluster.listener.IListener;
import ikube.scheduling.schedule.Event;
import ikube.toolkit.ThreadUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will listen to the cluster for termination events to stop indexing. In the first case it will destroy the
 * thread(s) that is running a particular job, which could be an indexing job, which will terminate the job gracefully. In
 * the second case it will destroy the thread pool which will then terminate all jobs, also gracefully.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 24-08-2012
 */
public class StopListener implements IListener<Message<Object>>, MessageListener<Object> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(final Message<Object> message) {
        // If this is a stop working message then find the future in the thread utilities and kill it
        Object object = message.getMessageObject();
        if (object != null && Event.class.isAssignableFrom(object.getClass())) {
            Event event = (Event) object;
            if (event.isConsumed()) {
                return;
            }
            event.setConsumed(Boolean.TRUE);
            if (Event.TERMINATE.equals(event.getType())) {
                String indexName = event.getObject().toString();
                logger.info("Terminating indexing : " + indexName);
                ThreadUtilities.destroy(indexName);
                ThreadUtilities.cancelForkJoinPool(indexName);
            } else if (Event.TERMINATE_ALL.equals(event.getType())) {
                logger.info("Terminating all indexing : ");
                ThreadUtilities.destroy();
                ThreadUtilities.cancelAllForkJoinPools();
            }
        }
    }

}