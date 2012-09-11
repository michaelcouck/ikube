package ikube.listener;

import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author Michael Couck
 * @since 10.09.12
 * @version 01.00
 */
public class IndexContextListener implements IListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexContextListener.class);

	@Autowired
	private IDataBase dataBase;
	@Autowired
	private IClusterManager clusterManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void handleNotification(Event event) {
		if (!Event.TIMER.equals(event.getType())) {
			return;
		}
		// Check the database for new index contexts
		ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext();
		Map<String, IndexContext> xmlIndexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		List<IndexContext> databaseIndexContexts = dataBase.find(IndexContext.class, 0, Integer.MAX_VALUE);
		for (IndexContext databaseIndexContext : databaseIndexContexts) {
			if (!containsIndexContext(databaseIndexContext, xmlIndexContexts.values())) {
				// TODO Add the index context to the application context, perhaps merge
				LOGGER.info("Adding index context : " + databaseIndexContext);
				IndexContext indexContext = applicationContext.getAutowireCapableBeanFactory().createBean(IndexContext.class);
				try {
					BeanUtils.copyProperties(databaseIndexContext, indexContext);
				} catch (Exception e) {
					LOGGER.error("Exception adding index context from database : ", e);
				}
			}
			// Send messages to all the other servers on the index contexts in this server
			clusterManager.sendMessage(databaseIndexContext);
		}
	}

	@SuppressWarnings("rawtypes")
	private boolean containsIndexContext(final IndexContext indexContext, final Collection<IndexContext> indexContexts) {
		for (final IndexContext xmlIndexContext : indexContexts) {
			if (indexContext.getIndexName().equals(xmlIndexContext.getIndexName())) {
				return true;
			}
		}
		return false;
	}

}