package ikube.action.rule;

import ikube.index.IndexManager;
import ikube.model.IndexContext;

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
@SuppressWarnings("deprecation")
public class IsNewIndexCreated extends ARule<IndexContext<?>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		MultiSearcher searcher = indexContext.getMultiSearcher();
		Searchable[] searchables = searcher != null ? searcher.getSearchables() : null;

		String baseIndexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(baseIndexDirectoryPath);

		if (searchables != null) {
			IRule<File[]> areDirectoriesEqual = new AreDirectoriesEqual();
			IRule<File> directoryExistsAndNotLocked = new DirectoryExistsAndNotLocked();
			for (Searchable searchable : searchables) {
				IndexSearcher indexSearcher = (IndexSearcher) searchable;
				IndexReader indexReader = indexSearcher.getIndexReader();
				FSDirectory fsDirectory = (FSDirectory) indexReader.directory();
				File serverIndexDirectoryFromSearcher = fsDirectory.getFile();
				if (serverIndexDirectoryFromSearcher != null) {
					File latestIndexDirectoryFromSearcher = serverIndexDirectoryFromSearcher.getParentFile();
					if (areDirectoriesEqual.evaluate(new File[] { latestIndexDirectory, latestIndexDirectoryFromSearcher })) {
						File[] serverIndexDirectories = latestIndexDirectory.listFiles();
						for (File serverIndexDirectory : serverIndexDirectories) {
							if (directoryExistsAndNotLocked.evaluate(serverIndexDirectory)) {
								// Here we have found that the latest index is indeed the same as
								// the index in the searchable, so we return false, i.e. no new index
								// found
								return Boolean.FALSE;
							}
						}
					}
				}
			}
		}

		return new AreIndexesCreated().evaluate(indexContext);
	}

}