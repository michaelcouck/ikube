package ikube.action;

import ikube.index.IndexManager;
import ikube.index.handler.IHandler;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ThreadUtilities;

import java.util.List;
import java.util.Map;

/**
 * This class executes the handlers on the indexables, effectively creating the index. Each indexable has a handler that is implemented to
 * handle it. Each handler is configured with an annotation that specifies the type of indexable that it can handle. This class then
 * iterates over all the indexables in the context for the index, finds the correct handler and calls the
 * {@link IHandler#handle(IndexContext, Indexable)} method with the indexable. The return value from this method from the handlers is a list
 * of threads. The caller must then wait for all the threads to finish and die before continuing.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class Index extends Action {

	@Override
	public Boolean execute(IndexContext indexContext) throws Exception {
		boolean indexCurrent = isIndexCurrent(indexContext);
		logger.debug(Logging.getString("Index current : ", indexCurrent, indexContext));
		if (indexCurrent) {
			// Check if there are any other servers working on this index
			if (!getClusterManager().anyWorking(indexContext.getIndexName())) {
				return Boolean.FALSE;
			}
		}
		Server server = getClusterManager().getServer();
		List<Indexable<?>> indexables = indexContext.getIndexables();
		String indexName = indexContext.getIndexName();
		try {
			// If we get here then there are two possibilities:
			// 1) The index is not current and we will start the index
			// 2) The index is current and there are other servers working on the index, so we join them
			long lastWorkingStartTime = getClusterManager().setWorking(indexName, "", Boolean.TRUE);
			logger.info(Logging.getString("Index : Last working time : ", lastWorkingStartTime));
			// Start the indexing for this server
			IndexManager.openIndexWriter(server.getAddress(), indexContext, lastWorkingStartTime);
			for (Indexable<?> indexable : indexables) {
				try {
					// Get the right handler for this indexable
					IHandler<Indexable<?>> handler = getHandler(indexable);
					if (handler == null) {
						logger.warn(Logging.getString("Not handling indexable : ", indexable, " no handler defined."));
						continue;
					}
					// Execute the handler and wait for the threads to finish
					getClusterManager().setWorking(indexName, indexable.getName(), Boolean.TRUE);
					logger.info("Executing handler : " + handler + ", " + indexable);
					List<Thread> threads = handler.handle(indexContext, indexable);
					if (threads != null && threads.size() > 0) {
						logger.info("Waiting for threads : " + threads);
						ThreadUtilities.waitForThreads(threads);
					}
				} catch (Exception e) {
					logger.error("Exception indexing data : " + indexContext.getIndexName(), e);
				}
			}
		} finally {
			IndexManager.closeIndexWriter(indexContext);
			getClusterManager().setWorking(indexName, "", Boolean.FALSE);
		}
		String contextName = indexContext.getIndexName();
		logger.debug(Logging.getString("Index : Finished indexing : ", indexName, contextName));
		return Boolean.TRUE;
	}

	/**
	 * This method finds the correct handler for the indexable.
	 * 
	 * @param indexableHandlers
	 *            a map of all the handlers in the configuration
	 * @param indexable
	 *            the indexable to find the handler for
	 * @return the handler for the indexable or null if there is no handler for the indexable. This will fail with a warning if there is no
	 *         handler for the indexable
	 */
	protected IHandler<Indexable<?>> getHandler(Indexable<?> indexable) {
		@SuppressWarnings("rawtypes")
		Map<String, IHandler> indexableHandlers = ApplicationContextManager.getBeans(IHandler.class);
		for (IHandler<Indexable<?>> handler : indexableHandlers.values()) {
			if (handler.getIndexableClass().isAssignableFrom(indexable.getClass())) {
				return handler;
			}
		}
		logger.warn("No handler for type : " + indexable);
		return null;
	}

}