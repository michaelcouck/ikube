package ikube.cluster.listener.hzc;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import ikube.cluster.listener.IListener;
import ikube.scheduling.schedule.Event;
import ikube.scheduling.schedule.SnapshotSchedule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This listener will cause the snapshot schedule to run, getting a more recent data set
 * for the server, typically this event will be cluster wide.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 20-05-2013
 */
public class SnapshotListener implements IListener<Message<Object>>, MessageListener<Object> {

    @Autowired
    private SnapshotSchedule snapshotSchedule;

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
            if (Event.TAKE_SNAPSHOT.equals(event.getType())) {
                event.setConsumed(Boolean.TRUE);
                snapshotSchedule.run();
            }
        }
    }

}
