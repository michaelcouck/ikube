package ikube.cluster.listener.hzc;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.cluster.listener.IListener;
import ikube.database.IDataBase;
import ikube.model.Search;
import ikube.scheduling.schedule.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This listener will just evict objects from the grid that have been designated for deletion.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 24-08-2014
 */
@SuppressWarnings("ALL")
public class EvictionListener implements IListener<Message<Object>>, MessageListener<Object> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IDataBase dataBase;
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
            if (event.isConsumed() || !Event.EVICTION.equals(event.getType())) {
                return;
            }
            Long key = (Long) event.getObject();
            if (key == null) {
                logger.warn("Trying to evict object with null key : " + event.getType());
                return;
            }
            event.setConsumed(Boolean.TRUE);
            logger.info("Evicting object with key : " + key);
            clusterManager.remove(IConstants.SEARCH, key);
            dataBase.remove(Search.class, key);
        }
    }

}