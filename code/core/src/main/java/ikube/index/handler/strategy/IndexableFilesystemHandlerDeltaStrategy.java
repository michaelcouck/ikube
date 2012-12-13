package ikube.index.handler.strategy;

import ikube.IConstants;
import ikube.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.search.Search;
import ikube.search.SearchMulti;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Searcher;

/**
 * This is the delta strategy for the file system handler. Essentially what this class should do is to check to see if the document/file
 * being processed already exists in the current index. If it does, and the time stamp and the length are the same then return a false
 * indicator, meaning that the handler should not add this document to the index.
 * 
 * @author Michael Couck
 * @since 12.12.12
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class IndexableFilesystemHandlerDeltaStrategy extends AStrategy<IndexableFileSystem, File> {

	public IndexableFilesystemHandlerDeltaStrategy(final IStrategy<IndexableFileSystem, File> nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean preProcess(final IndexableFileSystem indexableFileSystem, final File file) {
		Search search = getSearch(indexableFileSystem, file);
		ArrayList<HashMap<String, String>> results = search.execute();
		boolean foundFile = results.size() >= 1;
		return foundFile & (nextStrategy != null ? nextStrategy.preProcess(indexableFileSystem, file) : true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean postProcess(final IndexableFileSystem indexableFileSystem, final File file) {
		return nextStrategy != null ? nextStrategy.postProcess(indexableFileSystem, file) : true;
	}

	Search getSearch(final IndexableFileSystem indexableFileSystem, final File file) {
		IndexContext<?> indexContext = (IndexContext<?>) indexableFileSystem.getParent();
		Searcher searcher = indexContext.getMultiSearcher();
		Analyzer analyzer = indexContext.getAnalyzer() != null ? indexContext.getAnalyzer() : IConstants.ANALYZER;
		Search search = new SearchMulti(searcher, analyzer);
		search.setFirstResult(0);
		search.setMaxResults(1);
		search.setFragment(Boolean.FALSE);
		search.setSearchField(indexableFileSystem.getPathFieldName(), indexableFileSystem.getNameFieldName(),
				indexableFileSystem.getLastModifiedFieldName(), indexableFileSystem.getLengthFieldName());
		search.setSearchString(file.getAbsolutePath(), file.getName(), Long.toString(file.lastModified()), Long.toString(file.length()));
		return search;
	}

}