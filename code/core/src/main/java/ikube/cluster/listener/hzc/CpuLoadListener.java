package ikube.cluster.listener.hzc;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import ikube.cluster.IClusterManager;
import ikube.cluster.listener.IListener;
import ikube.model.Server;
import ikube.scheduling.schedule.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This listener is to set the cpu throttling flag in the server. If the flag is set then the jobs will
 * be throttled if the cpu load for the server gets too high, typically more than 0.9 per cpu on a Linux machine.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 20-05-2013
 */
public class CpuLoadListener implements IListener<Message<Object>>, MessageListener<Object> {

    private Logger logger = LoggerFactory.getLogger(CpuLoadListener.class);
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
            if (Event.CPU_LOAD_THROTTLING.equals(event.getType())) {
                event.setConsumed(Boolean.TRUE);
                Server server = clusterManager.getServer();
                logger.info("Toggling cpu throttling : " + server.isCpuThrottling());
                server.setCpuThrottling(!server.isCpuThrottling());
            }
        }
    }

}
