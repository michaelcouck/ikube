package ikube.action;

import ikube.index.IndexManager;
import ikube.index.handler.IHandler;
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
 * handle it. Each handler will return a list of threads that will do the indexing. The caller(in this case, this class) must then wait for
 * the threads to finish.
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
		Server server = clusterManager.getServer();
		List<Indexable<?>> indexables = indexContext.getIndexables();
		long actionId = 0;
		try {
			actionId = start(indexContext, "");
			if (indexables != null && indexables.size() > 0) {
				long lastWorkingStartTime = System.currentTimeMillis();
				ikube.model.Action action = getAction(server, actionId);
				if (action != null) {
					lastWorkingStartTime = action.getStartTime().getTime();
				}
				if (lastWorkingStartTime <= 0) {
					logger.warn("Failed to join the cluster indexing : " + indexContext);
					return Boolean.FALSE;
				}
				// If we get here then there are two possibilities:
				// 1) The index is not current and we will start the index
				// 2) The index is current and there are other servers working on the index, so we join them
				logger.debug(Logging.getString("Last working time : ", lastWorkingStartTime));
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
						action.setIndexableName(indexable.getName());
						logger.debug("Executing handler : " + handler + ", " + indexable.getName());
						// Execute the handler and wait for the threads to finish
						List<Thread> threads = handler.handle(indexContext, indexable);
						if (threads != null && !threads.isEmpty()) {
							logger.debug("Waiting for threads : " + threads);
							ThreadUtilities.waitForThreads(threads);
						}
					} catch (Exception e) {
						logger.error("Exception indexing data : " + indexName, e);
					}
				}
				return Boolean.TRUE;
			}
		} finally {
			logger.debug(Logging.getString("Finished indexing : ", indexName));
			IndexManager.closeIndexWriter(indexContext);
			stop(indexContext, actionId);
		}
		return Boolean.FALSE;
	}

	protected ikube.model.Action getAction(Server server, long id) {
		for (ikube.model.Action action : server.getActions()) {
			if (action.getId() == id) {
				return action;
			}
		}
		return null;
	}

	/**
	 * This method finds the correct handler for the indexable.
	 * 
	 * @param indexableHandlers a map of all the handlers in the configuration
	 * @param indexable the indexable to find the handler for
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