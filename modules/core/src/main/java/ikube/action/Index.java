package ikube.action;

import ikube.index.IndexManager;
import ikube.index.handler.IHandler;
import ikube.index.handler.IndexableHandlerType;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ThreadUtilities;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class Index extends Action {

	@Override
	public Boolean execute(IndexContext indexContext) throws Exception {
		boolean indexCurrent = isIndexCurrent(indexContext);
		logger.debug(Logging.getString("Index current : ", indexCurrent));
		if (indexCurrent) {
			return Boolean.FALSE;
		}
		List<Indexable<?>> indexables = indexContext.getIndexables();
		String actionName = getClass().getName();
		String indexName = indexContext.getIndexName();
		try {
			// If we get here then there are two possibilities:
			// 1) The index is not current and we will start the index
			// 2) The index is current and there are other servers working on the index, so we join them
			long lastWorkingStartTime = getClusterManager().setWorking(indexName, actionName, null, Boolean.TRUE);
			logger.debug(Logging.getString("Index : Last working time : ", lastWorkingStartTime));
			Server server = getClusterManager().getServer();
			// Start the indexing for this server
			IndexManager.openIndexWriter(server.getAddress(), indexContext, lastWorkingStartTime);
			@SuppressWarnings("rawtypes")
			Map<String, IHandler> indexableHandlers = ApplicationContextManager.getBeans(IHandler.class);
			for (Indexable<?> indexable : indexables) {
				try {
					// Get the right handler for this indexable
					IHandler<Indexable<?>> handler = getHandler(indexableHandlers, indexable);
					if (handler == null) {
						logger.warn("Not handling indexable : " + indexable);
						continue;
					}
					// Execute the handler and wait for the threads to finish
					logger.info("Executing handler : " + handler);
					getClusterManager().setWorking(indexName, actionName, handler.getClass().toString(), Boolean.TRUE);
					List<Thread> threads = handler.handle(indexContext, indexable);
					if (threads != null && threads.size() > 0) {
						logger.info("Threads to wait for : " + threads);
						ThreadUtilities.waitForThreads(threads);
					}
				} catch (Exception e) {
					logger.error("Exception indexing data : " + indexContext.getIndexName(), e);
				}
			}
		} finally {
			IndexManager.closeIndexWriter(indexContext);
			getClusterManager().setWorking(indexName, "", "", Boolean.FALSE);
		}
		String contextName = indexContext.getName();
		logger.debug(Logging.getString("Index : Finished indexing : ", indexName, ", ", contextName));
		return Boolean.TRUE;
	}

	protected IHandler<Indexable<?>> getHandler(@SuppressWarnings("rawtypes") Map<String, IHandler> indexableHandlers,
			Indexable<?> indexable) {
		for (IHandler<Indexable<?>> handler : indexableHandlers.values()) {
			Method[] methods = handler.getClass().getMethods();
			for (Method method : methods) {
				IndexableHandlerType indexableHandlerType = method.getAnnotation(IndexableHandlerType.class);
				if (indexableHandlerType == null) {
					continue;
				}
				if (indexableHandlerType.type().equals(indexable.getClass())) {
					return handler;
				}
			}
		}
		logger.warn("No handler for type : " + indexable);
		return null;
	}

}