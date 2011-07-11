package ikube.action.rule;

import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;

import java.io.File;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.FSDirectory;

/**
 * This class checks whether the index that is open is the latest index, i.e. whether there is a new index that should be opened.
 * 
 * @author Michael Couck
 * @since 11.06.11
 * @version 01.00
 */
public class IsNewIndexCreated extends ARule<IndexContext<?>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		MultiSearcher searcher = indexContext.getIndex() != null ? indexContext.getIndex().getMultiSearcher() : null;
		Searchable[] searchables = searcher != null ? searcher.getSearchables() : null;
		if (searchables == null) {
			return new AreIndexesCreated().evaluate(indexContext);
		}
		File baseIndexDirectory = new File(IndexManager.getIndexDirectoryPath(indexContext));
		File[] timeIndexDirectories = baseIndexDirectory.listFiles();
		if (timeIndexDirectories == null || timeIndexDirectories.length == 0) {
			return Boolean.FALSE;
		}
		String baseIndexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(baseIndexDirectoryPath);
		if (latestIndexDirectory == null || !latestIndexDirectory.exists()) {
			return Boolean.FALSE;
		}

		IRule<File[]> areDirectoriesEqual = new AreDirectoriesEqual();
		IRule<File> directoryExistsAndNotLocked = new DirectoryExistsAndNotLocked();

		File[] serverIndexDirectories = latestIndexDirectory.listFiles();
		boolean indexAlreadyOpen = Boolean.FALSE;
		for (File serverIndexDirectory : serverIndexDirectories) {
			for (Searchable searchable : searchables) {
				IndexSearcher indexSearcher = (IndexSearcher) searchable;
				IndexReader indexReader = indexSearcher.getIndexReader();
				FSDirectory fsDirectory = (FSDirectory) indexReader.directory();
				File indexDirectory = fsDirectory.getFile();
				if (areDirectoriesEqual.evaluate(new File[] { serverIndexDirectory, indexDirectory })) {
					indexAlreadyOpen = directoryExistsAndNotLocked.evaluate(serverIndexDirectory);
					break;
				}
			}
			if (!indexAlreadyOpen) {
				logger.debug(Logging.getString("Found new index directory : ", serverIndexDirectory, " will try to re-open : "));
				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}

}