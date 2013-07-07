package ikube.action;

import ikube.model.IndexContext;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;

/**
 * This action will re-open the indexes in the case it is a delta index.
 * 
 * @author Michael Couck
 * @since 22.06.13
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class Reopen extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean internalExecute(final IndexContext<?> indexContext) throws Exception {
		if (indexContext.isDelta()) {
			new Open().execute(indexContext);
			logger.info("Reopening : " + indexContext.getName());
			MultiSearcher multiSearcher = indexContext.getMultiSearcher();
			if (multiSearcher != null && multiSearcher.getSearchables() != null) {
				for (final Searchable searchable : multiSearcher.getSearchables()) {
					logger.info("        : Num docs : " + ((IndexSearcher) searchable).getIndexReader().numDocs());
				}
			}
		}
		return Boolean.TRUE;
	}

}