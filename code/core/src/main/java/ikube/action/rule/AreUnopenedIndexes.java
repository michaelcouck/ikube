package ikube.action.rule;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;

import java.io.File;
import java.util.List;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.CompositeReaderContext;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;

/**
 * This rule checks whether there are indexes that are created but are not yet opened. This typically needs to be checked if an index is still in the process of
 * being generated. In this case when the index is finished being created the searcher should be opened on all the index directories.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class AreUnopenedIndexes extends ARule<IndexContext<?>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		IndexSearcher searchers = indexContext.getMultiSearcher();
		if (searchers == null) {
			return new AreIndexesCreated().evaluate(indexContext);
		}
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
		if (latestIndexDirectory == null || latestIndexDirectory.listFiles() == null || latestIndexDirectory.listFiles().length == 0) {
			return Boolean.FALSE;
		}
		MultiReader multiReader = (MultiReader) searchers.getIndexReader();
		CompositeReaderContext compositeReaderContext = multiReader.getContext();
		List<AtomicReaderContext> atomicReaderContexts = compositeReaderContext.leaves();
		logger.debug("Checking latest index directory for new indexes : {} ", latestIndexDirectory);
		return atomicReaderContexts.size() != latestIndexDirectory.listFiles().length;
	}

}
