package ikube.action;

import ikube.index.IndexManager;
import ikube.index.handler.Handler;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;

import java.util.List;
import java.util.Map;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class Index extends Action<IndexContext, Boolean> {

	@Override
	public Boolean execute(IndexContext indexContext) {
		long thisThread = Thread.currentThread().hashCode();
		try {
			List<Indexable<?>> indexables = indexContext.getIndexables();
			String actionName = getClass().getName();
			boolean indexCurrent = isIndexCurrent(indexContext);
			logger.debug(Logging.getString("Index current : ", indexCurrent, ", ", thisThread));
			if (indexCurrent) {
				return Boolean.FALSE;
			}
			if (getClusterManager().anyWorking(actionName)) {
				// Other servers working on an action other than this action. These
				// actions need to be atomic, i.e. only one server executing one action
				// at a time, other than the indexing action of course
				logger.debug("Other servers working on different actions : ");
				return Boolean.FALSE;
			}
			// If we get here then there are two possibilities:
			// 1) The index is not current and we will start the index
			// 2) The index is current and there are other servers working on the index, so we join them
			getClusterManager().setWorking(indexContext, this.getClass().getName(), Boolean.TRUE, System.currentTimeMillis());
			long lastWorkingStartTime = getClusterManager().getLastWorkingTime(indexContext.getIndexName(), actionName);
			logger.debug(Logging.getString("Index : Last working time : ", lastWorkingStartTime, ", ", thisThread));
			Server server = getClusterManager().getServer();
			// Start the indexing for this server
			IndexManager.openIndexWriter(server.getIp(), indexContext, lastWorkingStartTime);
			try {
				Map<String, Handler> handlers = ApplicationContextManager.getBeans(Handler.class);
				for (Handler handler : handlers.values()) {
					for (Indexable<?> indexable : indexables) {
						// Execute each handler and wait for the threads to finish
						logger.info("Executing handler : " + handler);
						List<Thread> threads = handler.handle(indexContext, indexable);
						logger.info("Threads to wait for : " + threads);
						waitForThreads(threads);
					}
				}
			} catch (Exception e) {
				logger.error("Exception indexing data : " + indexContext.getIndexName(), e);
			}
		} finally {
			IndexManager.closeIndexWriter(indexContext);
			getClusterManager().setWorking(indexContext, null, Boolean.FALSE, 0);
		}
		String indexName = indexContext.getIndexName();
		String contextName = indexContext.getName();
		logger.debug(Logging.getString("Index : Finished indexing : ", indexName, ", ", contextName, ", ", thisThread));
		return Boolean.TRUE;
	}

}