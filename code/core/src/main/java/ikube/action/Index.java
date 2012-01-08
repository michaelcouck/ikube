package ikube.action;

import ikube.index.IndexManager;
import ikube.index.handler.IHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

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
		ikube.model.Action action = null;
		try {
			if (indexables != null) {
				long startTime = System.currentTimeMillis();
				// Start the indexing for this server
				IndexManager.openIndexWriter(indexContext, startTime, server.getAddress());
				Iterator<Indexable<?>> iterator = indexables.iterator();
				while (iterator.hasNext()) {
					Indexable<?> indexable = iterator.next();
					try {
						// Get the right handler for this indexable
						IHandler<Indexable<?>> handler = getHandler(indexable);
						if (handler == null) {
							String message = Logging.getString("Not handling indexable : ", indexable.getName(), " no handler defined.");
							logger.warn(message);
							continue;
						}
						if (action != null) {
							stop(action);
						}
						action = start(indexContext.getIndexName(), indexable.getName());
						indexContext.setAction(action);
						logger.info("Indexable : " + indexable.getName());
						logger.info("Handler : " + handler.getClass() + ", " + handler.getIndexableClass());
						// Execute the handler and wait for the threads to finish
						List<Future<?>> futures = handler.handle(indexContext, indexable);
						ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
					} catch (Exception e) {
						logger.error("Exception indexing data : " + indexName, e);
					} finally {
						// Close the index writer before the last action is stopped
						if (!iterator.hasNext()) {
							IndexManager.closeIndexWriter(indexContext);
						}
					}
				}
			}
			return Boolean.TRUE;
		} finally {
			logger.debug(Logging.getString("Finished indexing : ", indexName));
			// We'll try to close the writer, even though it should already be closed
			IndexManager.closeIndexWriter(indexContext);
			indexContext.setAction(null);
			stop(action);
		}
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
		logger.warn("No handler for type : " + indexable.getName());
		return null;
	}

}