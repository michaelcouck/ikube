package ikube.action;

import ikube.index.IndexManager;
import ikube.index.handler.IHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ThreadUtilities;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author Michael Couck
 * @since 26.12.12
 * @version 01.00
 */
public abstract class AIndex extends Action<IndexContext<?>, Boolean> {

	void executeIndexables(final IndexContext<?> indexContext, final Iterator<Indexable<?>> iterator) {
		while (iterator.hasNext()) {
			ikube.model.Action action = null;
			try {
				Indexable<?> indexable = iterator.next();
				// Get the right handler for this indexable
				IHandler<Indexable<?>> handler = getHandler(indexable);
				action = start(indexContext.getIndexName(), indexable.getName());
				logger.info("Indexable : " + indexable.getName());
				// Execute the handler and wait for the threads to finish
				List<Future<?>> futures = handler.handle(indexContext, indexable);
				ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
			} catch (Exception e) {
				logger.error("Exception indexing data : " + indexContext.getIndexName(), e);
			} finally {
				// Close the index writers before the last action is stopped or
				// in the ui it looks like the action has completely stopped but the
				// index is still being optimized
				if (!iterator.hasNext()) {
					IndexManager.closeIndexWriters(indexContext);
					indexContext.setIndexWriters();
				}
				stop(action);
			}
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
			if (handler.getIndexableClass().equals(indexable.getClass())) {
				return handler;
			}
		}
		logger.warn("No handler for type : " + indexable.getName());
		throw new RuntimeException("No handler defined for indexable : " + indexable);
	}

}