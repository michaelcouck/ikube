package ikube.action;

import ikube.model.IndexContext;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.Directory;

/**
 * This class takes the searcher and tries to close the searcher on the directory.
 * 
 * @author Michael Couck
 * @since 24.08.08
 * @version 01.00
 */
public class Close extends Action {

	@Override
	public Boolean execute(IndexContext indexContext) {
		// First check to see if there are any new indexes, if there are no
		// new indexes then we shouldn't close the searcher
		boolean shouldClose = shouldReopen(indexContext);
		if (!shouldClose) {
			logger.debug("Should close : " + shouldClose);
			return Boolean.FALSE;
		}
		MultiSearcher multiSearcher = indexContext.getIndex().getMultiSearcher();
		if (multiSearcher == null) {
			return Boolean.TRUE;
		}
		// Get all the searchables from the searcher and close them one by one
		Searchable[] searchables = multiSearcher.getSearchables();
		if (searchables != null && searchables.length > 0) {
			for (Searchable searchable : searchables) {
				try {
					IndexSearcher indexSearcher = (IndexSearcher) searchable;
					IndexReader reader = indexSearcher.getIndexReader();
					Directory directory = reader.directory();
					if (IndexWriter.isLocked(directory)) {
						IndexWriter.unlock(directory);
					}
					reader.close();
					searchable.close();
				} catch (Exception e) {
					logger.error("Exception trying to close the searcher", e);
				}
			}
		}
		// Set the searcher to null so the open action
		// will then be invoked to re-open the searcher
		// during the next iteration over the actions
		indexContext.getIndex().setMultiSearcher(null);
		return Boolean.TRUE;
	}

}