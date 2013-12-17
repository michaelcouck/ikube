package ikube.action;

import ikube.model.IndexContext;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
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
public class Reopen extends Open {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean internalExecute(final IndexContext<?> indexContext) {
		MultiSearcher multiSearcher = indexContext.getMultiSearcher();
		if (indexContext.isDelta()) {
			if (multiSearcher == null) {
				openOnFile(indexContext);
			} else {
				Searchable[] searchables = multiSearcher.getSearchables();
				if (searchables != null && searchables.length > 0) {
					for (int i = 0; i < searchables.length; i++) {
						Searchable searchable = searchables[i];
						if (IndexSearcher.class.isAssignableFrom(searchable.getClass())) {
							IndexSearcher oldIndexSearcher = (IndexSearcher) searchable;
							IndexReader indexReader = oldIndexSearcher.getIndexReader();
							IndexReader newIndexReader = null;
							try {
								newIndexReader = IndexReader.openIfChanged(indexReader);
								if (newIndexReader != null) {
									logger.info("Re-opening reader on index : " + indexContext.getName());
									searchables[i] = new IndexSearcher(indexReader);
									oldIndexSearcher.close();
								}
							} catch (IOException e) {
								logger.error("Exception reopening the searcher on reader : " + indexReader + ", " + newIndexReader, e);
							}
						}
					}
				}
			}
		}
		return Boolean.TRUE;
	}

}