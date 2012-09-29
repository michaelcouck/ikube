package ikube.cluster.hzc;

import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.service.IMonitorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class IndexContextListener implements MessageListener<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexContextListener.class);

	@Autowired
	private IDataBase dataBase;
	@Autowired
	private IMonitorService monitorService;

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
			if (remoteIndexContext.getTimestamp().after(localIndexContext.getTimestamp())) {
				LOGGER.info("Adding index context : " + remoteIndexContext);
				dataBase.remove(localIndexContext);
				dataBase.persist(remoteIndexContext);
			}
		}
	}

}