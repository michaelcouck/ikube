package ikube.action;

import ikube.model.IndexContext;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.Directory;

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
		if (indexContext.isDelta() && indexContext.getMultiSearcher() == null) {
			ArrayList<Searchable> searchers = new ArrayList<Searchable>();
			IndexWriter[] indexWriters = indexContext.getIndexWriters();
			if (indexWriters != null && indexWriters.length > 0) {
				for (final IndexWriter indexWriter : indexWriters) {
					Directory directory = indexWriter.getDirectory();
					IndexReader reader;
					try {
						reader = IndexReader.open(directory, Boolean.TRUE);
						IndexSearcher indexSearcher = new IndexSearcher(reader);
						searchers.add(indexSearcher);
					} catch (CorruptIndexException e) {
						logger.error("Index corrupt while indexing delta : ", e);
					} catch (IOException e) {
						logger.error("IOException while indexing delta : ", e);
					}
				}
			}
			return open(indexContext, searchers);
		}
		return Boolean.TRUE;
	}

}