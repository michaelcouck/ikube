package ikube.action;

import ikube.index.IndexManager;
import ikube.index.handler.IIndexableHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.Server;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.lucene.index.IndexWriter;

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
	public boolean preExecute(final IndexContext<?> indexContext) throws Exception {
		logger.info("Pre process action : " + this.getClass());
		Server server = clusterManager.getServer();
		long startTime = System.currentTimeMillis();
		// Start the indexing for this server
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, startTime, server.getAddress());
		IndexWriter[] indexWriters = new IndexWriter[] { indexWriter };
		indexContext.setIndexWriters(indexWriters);
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean internalExecute(final IndexContext<?> indexContext) throws Exception {
		List<Indexable<?>> indexables = indexContext.getIndexables();
		Iterator<Indexable<?>> iterator = new ArrayList(indexables).iterator();
		while (iterator.hasNext()) {
			ikube.model.Action action = null;
			try {
				Indexable<?> indexable = iterator.next();
				// Get the right handler for this indexable
				IIndexableHandler<Indexable<?>> handler = getHandler(indexable);
				action = start(indexContext.getIndexName(), indexable.getName());
				logger.info("Indexable : " + indexable.getName());
				// Execute the handler and wait for the threads to finish
				List<Future<?>> futures = handler.handleIndexable(indexContext, indexable);
				ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
			} catch (Exception e) {
				logger.error("Exception indexing data : " + indexContext.getIndexName(), e);
			} finally {
				stop(action);
			}
		}
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean postExecute(final IndexContext<?> indexContext) throws Exception {
		logger.info("Post process action : " + this.getClass());
		IndexManager.closeIndexWriters(indexContext);
		indexContext.setIndexWriters();
		return Boolean.TRUE;
	}

}