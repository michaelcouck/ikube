package ikube.action;

import ikube.model.IndexContext;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;

/**
 * This action will re-open the indexes in the case it is a delta index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 22.06.13
 */
public class Reopen extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean internalExecute(final IndexContext<?> indexContext) {
		logger.info("Opening searcher on index : " + indexContext.getName());
		IndexSearcher oldIndexSearcher = indexContext.getMultiSearcher();
		boolean mustReopen = Boolean.FALSE;
		try {
			if (oldIndexSearcher == null || oldIndexSearcher.getIndexReader() == null) {
				mustReopen = Boolean.TRUE;
			} else {
				MultiReader oldMultiReader = (MultiReader) oldIndexSearcher.getIndexReader();
				CompositeReaderContext compositeReaderContext = oldMultiReader.getContext();
				for (final IndexReaderContext indexReaderContext : compositeReaderContext.children()) {
					IndexReader oldIndexReader = indexReaderContext.reader();
					IndexReader newIndexReader = DirectoryReader.openIfChanged((DirectoryReader) oldIndexReader);
					if (newIndexReader != null) {
						mustReopen = Boolean.TRUE;
						break;
					}
				}
			}
			if (mustReopen) {
				new Open().execute(indexContext);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return Boolean.TRUE;
	}

}