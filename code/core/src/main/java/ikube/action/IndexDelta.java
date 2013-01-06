package ikube.action;

import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.util.Arrays;
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
		List<Indexable<?>> indexables = indexContext.getIndexables();
		logger.info("Index delta : " + indexables.size());
		// Start the indexing for this server
		IndexWriter[] indexWriters = IndexManager.openIndexWriterDelta(indexContext);
		indexContext.setIndexWriters(indexWriters);
		logger.info("Index delta : " + Arrays.deepToString(indexWriters) + ", " + indexContext);
		Iterator<Indexable<?>> iterator = indexables.iterator();
		executeIndexables(indexContext, iterator);
		return Boolean.TRUE;
	}

}