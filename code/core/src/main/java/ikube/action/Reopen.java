package ikube.action;

import ikube.model.IndexContext;

import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

/**
 * This action will re-open the indexes in the case it is a delta index.
 * 
 * @author Michael Couck
 * @since 22.06.13
 * @version 01.00
 */
public class Reopen extends Open {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean internalExecute(final IndexContext<?> indexContext) {
		if (indexContext.isDelta()) {
			logger.info("Opening searcher on index : " + indexContext.getName());
			IndexSearcher oldIndexSearcher = indexContext.getMultiSearcher();
			IndexReader indexReader = oldIndexSearcher.getIndexReader();
			try {
				IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader) indexReader);
				if (newReader != null) {
					IndexSearcher indexSearcher = new IndexSearcher(newReader);
					indexContext.setMultiSearcher(indexSearcher);
					oldIndexSearcher.getIndexReader().close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return Boolean.TRUE;
	}

}