package ikube.action;

import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.Logging;

import java.util.Iterator;
import java.util.List;

import org.apache.lucene.index.IndexWriter;

/**
 * @author Michael Couck
 * @since 26.12.12
 * @version 01.00
 */
public class IndexDelta extends AIndex {

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean executeInternal(final IndexContext<?> indexContext) throws Exception {
		if (!indexContext.isDelta()) {
			return Boolean.TRUE;
		}
		String indexName = indexContext.getIndexName();
		List<Indexable<?>> indexables = indexContext.getIndexables();
		ikube.model.Action action = null;
		try {
			if (indexables != null) {
				// Start the indexing for this server
				IndexWriter[] indexWriters = IndexManager.openIndexWriterDelta(indexContext);
				indexContext.setIndexWriters(indexWriters);
				Iterator<Indexable<?>> iterator = indexables.iterator();
				action = executeIndexables(indexContext, iterator);
			}
			return Boolean.TRUE;
		} finally {
			// We'll try to close the writer, even though it should already be closed
			IndexManager.closeIndexWriter(indexContext);
			indexContext.setIndexWriters();
			stop(action);
			logger.debug(Logging.getString("Finished indexing : ", indexName));
		}
	}

}