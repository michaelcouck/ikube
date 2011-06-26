package ikube.action;

import ikube.index.IndexManager;
import ikube.index.handler.IHandler;
import ikube.index.handler.internet.crawler.IUrlHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;

import java.util.List;
import java.util.Map;

/**
 * This class executes the handlers on the indexables, effectively creating the index. Each indexable has a handler that is implemented to
 * handle it. Each handler is configured with an annotation that specifies the type of indexable that it can handle. This class then
 * iterates over all the indexables in the context for the index, finds the correct handler and calls the
 * {@link IUrlHandler#handle(IndexContext, Indexable)} method with the indexable. The return value from this method from the handlers is a
 * list of threads. The caller must then wait for all the threads to finish and die before continuing.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class Index extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean execute(final IndexContext<?> indexContext) throws Exception {
		String indexName = indexContext.getIndexName();
		Server server = getClusterManager().getServer();
		List<Indexable<?>> indexables = indexContext.getIndexables();
		String actionName = this.getClass().getSimpleName();
		try {
			if (indexables != null && indexables.size() > 0) {
				long lastWorkingStartTime = getClusterManager().setWorking(actionName, indexContext.getIndexName(),
						indexables.get(0).getName(), Boolean.TRUE);
				if (lastWorkingStartTime <= 0) {
					logger.warn("Failed to join the cluster indexing : " + indexContext);
					getClusterManager().setWorking(actionName, indexContext.getIndexName(), indexables.get(0).getName(), Boolean.FALSE);
					return Boolean.FALSE;
				}
				// If we get here then there are two possibilities:
				// 1) The index is not current and we will start the index
				// 2) The index is current and there are other servers working on the index, so we join them
				logger.info(Logging.getString("Last working time : ", lastWorkingStartTime));
				// Start the indexing for this server
				IndexManager.openIndexWriter(indexContext, lastWorkingStartTime, server.getAddress());
				for (Indexable<?> indexable : indexables) {
					// Get the right handler for this indexable
					IHandler<Indexable<?>> handler = getHandler(indexable);
					if (handler == null) {
						logger.warn(Logging.getString("Not handling indexable : ", indexable, " no handler defined."));
						continue;
					}
					try {
						server = getClusterManager().getServer();
						if (server.getAction() != null) {
							// We need to reset the id of the next row
							// after each indexable has been indexed of course
							server.getAction().setIdNumber(0);
						}
						getClusterManager().setWorking(actionName, indexContext.getIndexName(), indexable.getName(), Boolean.TRUE);
						logger.info("Executing handler : " + handler + ", " + indexable.getName());
						// Execute the handler and wait for the threads to finish
						List<Thread> threads = handler.handle(indexContext, indexable);
						if (threads != null && !threads.isEmpty()) {
							logger.info("Waiting for threads : " + threads);
							ThreadUtilities.waitForThreads(threads);
						}
					} catch (Exception e) {
						logger.error("Exception indexing data : " + indexContext.getIndexName(), e);
					} finally {
						// getClusterManager().setWorking(actionName, indexContext.getIndexName(), indexable.getName(), Boolean.FALSE);
					}
				}
			}
		} finally {
			IndexManager.closeIndexWriter(indexContext);
			getClusterManager().setWorking(indexContext.getIndexName(), actionName, "", Boolean.FALSE);
		}
		String contextName = indexContext.getIndexName();
		logger.debug(Logging.getString("Finished indexing : ", indexName, contextName));
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
	protected IHandler<Indexable<?>> getHandler(final Indexable<?> indexable) {
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