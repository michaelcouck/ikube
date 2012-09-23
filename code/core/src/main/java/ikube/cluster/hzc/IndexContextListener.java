package ikube.cluster.hzc;

import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.Indexable;
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
		} else if (Indexable.class.isAssignableFrom(object.getClass())) {
			handleIndexable((Indexable<?>) object);
		}
	}

	protected void handleIndexable(final Indexable<?> indexable) {
		// Find the indexable in the database
		Indexable<?> dbIndexable = dataBase.findCriteria(indexable.getClass(), new String[] { "name" },
				new Object[] { indexable.getName() });
		// Remove the old one from the index context
		IndexContext<?> indexContext = (IndexContext<?>) dbIndexable.getParent();
		indexContext.getChildren().remove(dbIndexable);
		// Add the new one to the index context
		indexable.setId(0);
		indexContext.getChildren().add(indexable);
		// Merge the index context
		dataBase.merge(indexContext);

	}

	protected void handleIndexContext(final IndexContext<?> indexContext) {
		indexContext.setId(0);
		indexContext.setSnapshots(null);
		// LOGGER.info("Got index context message : " + indexContext);
		// Check the database for this context
		IndexContext<?> localIndexContext = monitorService.getIndexContext(indexContext.getIndexName());
		if (localIndexContext == null) {
			LOGGER.info("Adding index context : " + indexContext);
			dataBase.persist(indexContext);
		} else {
			// Check that all the indexables are in the local index context
			boolean contextsEqual = indexablesEqual(indexContext, localIndexContext);
			if (!contextsEqual) {
				LOGGER.info("Adding index context : " + indexContext);
				dataBase.remove(localIndexContext);
				dataBase.persist(indexContext);
			}
		}
	}

	protected boolean indexablesEqual(final Indexable<?> indexableOne, final Indexable<?> indexableTwo) {
		if (!indexableOne.getName().equals(indexableTwo.getName())) {
			return Boolean.FALSE;
		}
		if ((indexableOne.getChildren() == null || indexableOne.getChildren().size() == 0) //
				&& (indexableTwo.getChildren() == null || indexableTwo.getChildren().size() == 0)) {
			return Boolean.TRUE;
		}
		if (indexableOne.getChildren() == null) {
			return Boolean.FALSE;
		} else if (indexableTwo.getChildren() == null) {
			return Boolean.FALSE;
		}
		for (Indexable<?> childOne : indexableOne.getChildren()) {
			boolean contains = Boolean.FALSE;
			for (Indexable<?> childTwo : indexableTwo.getChildren()) {
				if (childOne.getName().equals(childTwo.getName())) {
					contains = Boolean.TRUE;
					break;
				}
			}
			if (!contains) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

}