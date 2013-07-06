package ikube.action;

import ikube.action.index.IndexManager;
import ikube.action.index.handler.IIndexableHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.Server;
import ikube.toolkit.Optimizer;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.IndexWriter;

/**
 * This class executes the handlers on the indexables, effectively creating the index. Each indexable has a handler that is implemented to handle it. Each
 * handler will return a list of threads that will do the indexing. The caller(in this case, this class) must then wait for the threads to finish.
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
	public boolean preExecute(final IndexContext<?> indexContext) throws Exception {
		logger.debug("Pre process action : " + this.getClass() + ", " + indexContext.getName());
		Server server = clusterManager.getServer();
		IndexWriter[] indexWriters = null;
		if (indexContext.isDelta()) {
			indexWriters = IndexManager.openIndexWriterDelta(indexContext);
		} else {
			IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), server.getAddress());
			indexWriters = new IndexWriter[] { indexWriter };
		}
		indexContext.setIndexWriters(indexWriters);
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean internalExecute(final IndexContext<?> indexContext) throws Exception {
		List<Indexable<?>> indexables = indexContext.getChildren();
		Iterator<Indexable<?>> iterator = new ArrayList(indexables).iterator();
		while (iterator.hasNext()) {
			// This is the current indexable name
			String indexableName = null;
			Indexable<?> indexable = iterator.next();
			// Update the action with the new indexable
			Server server = clusterManager.getServer();
			ikube.model.Action action = getAction(server, indexContext, indexableName);
			// Now we set the current indexable name in the action
			indexableName = indexable.getName();
			action.setIndexableName(indexableName);
			dataBase.merge(action);
			// Get the right handler for this indexable
			IIndexableHandler<Indexable<?>> handler = getHandler(indexable);
			// Execute the handler and wait for the threads to finish
			ForkJoinTask<?> forkJoinTask = handler.handleIndexableForked(indexContext, indexable);
			ThreadUtilities.executeForkJoinTasks(indexContext.getName(), indexable.getThreads(), forkJoinTask);
		}
		return Boolean.TRUE;
	}

	ikube.model.Action getAction(final Server server, final IndexContext<?> indexContext, final String indexableName) {
		for (final ikube.model.Action action : server.getActions()) {
			if (action.getActionName().equals(this.getClass().getSimpleName())) {
				if (!action.getIndexName().equals(indexContext.getName())) {
					continue;
				}
				if (StringUtils.isEmpty(indexableName)) {
					// We return the first action in the case the indexable name has not been set
					return action;
				} else {
					// Else we look for the action that has the current indexable name
					if (indexableName.equals(action.getIndexableName())) {
						return action;
					}
				}
			}
		}
		throw new RuntimeException("Action not found for class : " + this.getClass().getSimpleName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean postExecute(final IndexContext<?> indexContext) throws Exception {
		logger.debug("Post process action : " + this.getClass() + ", " + indexContext.getName());
		IndexManager.closeIndexWriters(indexContext);
		indexContext.setIndexWriters(new IndexWriter[0]);
		Optimizer.optimize(indexContext);
		return Boolean.TRUE;
	}

}