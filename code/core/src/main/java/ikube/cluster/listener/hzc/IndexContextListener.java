package ikube.cluster.listener.hzc;

import ikube.cluster.listener.IListener;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.service.IMonitorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

/**
 * This class will listen for index context changes which will be sent over the cluster manager. If the index context is updated, either an
 * indexable is added for example, or it is a new index context then it will be added to the local server contexts.
 * 
 * @author Michael Couck
 * @since 11.09.12
 * @version 01.00
 */
@Deprecated
public class IndexContextListener implements IListener<Message<Object>>, MessageListener<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexContextListener.class);

	/** The database wrapper to persist the index context. */
	@Autowired
	private IDataBase dataBase;
	/** The general monitoring service for querying the index contexts. */
	@Autowired
	private IMonitorService monitorService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onMessage(Message<Object> message) {
		Object object = message.getMessageObject();
		if (object == null) {
			LOGGER.info("Message object null : ");
			return;
		}
		if (IndexContext.class.isAssignableFrom(object.getClass())) {
			handleIndexContext((IndexContext<?>) object);
		}
	}

	protected void handleIndexContext(final IndexContext<?> remoteIndexContext) {
		remoteIndexContext.setId(0);
		remoteIndexContext.setSnapshots(null);
		// LOGGER.info("Got index context message : " + indexContext);
		// Check the database for this context
		if (monitorService == null || dataBase == null) {
			// This is a workaround for the integration tests
			return;
		}
		IndexContext<?> localIndexContext = monitorService.getIndexContext(remoteIndexContext.getIndexName());
		if (localIndexContext == null) {
			LOGGER.info("Adding index context : " + remoteIndexContext);
			dataBase.persist(remoteIndexContext);
		} else {
			// Check the time stamp of the contexts
			if (remoteIndexContext.getTimestamp() == null || localIndexContext.getTimestamp() == null) {
				return;
			}
			if (remoteIndexContext.getTimestamp().after(localIndexContext.getTimestamp())) {
				LOGGER.info("Adding index context : " + remoteIndexContext);
				dataBase.remove(localIndexContext);
				dataBase.persist(remoteIndexContext);
			}
		}
	}

}