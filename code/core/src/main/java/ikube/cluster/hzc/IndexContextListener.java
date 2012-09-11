package ikube.cluster.hzc;

import ikube.database.IDataBase;
import ikube.model.IndexContext;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class IndexContextListener implements MessageListener<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexContextListener.class);

	@Autowired
	private IDataBase dataBase;

	@Override
	@SuppressWarnings("rawtypes")
	public void onMessage(Message<Object> message) {
		Object object = message.getMessageObject();
		if (object == null || !IndexContext.class.isAssignableFrom(object.getClass())) {
			return;
		}
		IndexContext<?> indexContext = (IndexContext<?>) object;
		indexContext.setId(0);
		LOGGER.info("Got index context message : " + indexContext);
		// Check the database for this context
		IndexContext dbIndexContext = dataBase.findCriteria(IndexContext.class, new String[] { "name" },
				new Object[] { indexContext.getIndexName() });
		if (dbIndexContext == null) {
			LOGGER.info("Adding index context : " + indexContext);
			dataBase.persist(indexContext);
		}
		// Check that all the indexables are in the local index context
		boolean contextsEqual = EqualsBuilder.reflectionEquals(indexContext, dbIndexContext, false);
		if (!contextsEqual) {
			LOGGER.info("Adding index context : " + indexContext);
			dataBase.remove(dbIndexContext);
			dataBase.persist(indexContext);
		}
	}

}