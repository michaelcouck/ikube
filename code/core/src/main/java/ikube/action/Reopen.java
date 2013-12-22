package ikube.action;

import ikube.model.IndexContext;

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
		boolean opened = Boolean.FALSE;
		if (indexContext.isDelta()) {
			// int docs = numDocs(indexContext.getMultiSearcher());
			opened = openOnFile(indexContext);
			// int moreDocs = numDocs(indexContext.getMultiSearcher());
			// logger.info("Docs : " + docs + ", " + moreDocs);
		}
		return opened;
	}

	/**
	 * This method will just get a count of all the documents in the multi searcher.
	 * 
	 * @param multiSearcher the searcher to count the documents in
	 * @return the number of documents in all the readers in the searcher
	 */
	int numDocs(final MultiSearcher multiSearcher) {
		int numDocs = 0;
		if (multiSearcher != null) {
			Searchable[] searchables = multiSearcher.getSearchables();
			for (int i = 0; searchables != null && i < searchables.length; i++) {
				Searchable searchable = searchables[i];
				IndexSearcher indexSearcher = (IndexSearcher) searchable;
				IndexReader indexReader = indexSearcher.getIndexReader();
				numDocs += indexReader.numDocs();
			}
		}
		return numDocs;
	}

}