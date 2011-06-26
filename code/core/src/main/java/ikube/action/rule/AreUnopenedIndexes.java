package ikube.action.rule;

import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.Logging;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class AreUnopenedIndexes implements IRule<IndexContext<?>> {

	private static final transient Logger LOGGER = Logger.getLogger(AreUnopenedIndexes.class);

	public boolean evaluate(final IndexContext<?> indexContext) {
		MultiSearcher searcher = indexContext.getIndex() != null ? indexContext.getIndex().getMultiSearcher() : null;
		Searchable[] searchables = searcher != null ? searcher.getSearchables() : null;
		if (searchables == null) {
			return new AreIndexesCreated().evaluate(indexContext);
		}
		File baseIndexDirectory = new File(IndexManager.getIndexDirectoryPath(indexContext));
		File[] timeIndexDirectories = baseIndexDirectory.listFiles();
		IRule<File[]> areDirectoriesEqual = new AreDirectoriesEqual();
		IRule<File> directoryExistsAndNotLocked = new DirectoryExistsAndNotLocked();
		File[] files = new File[2];
		if (timeIndexDirectories == null || timeIndexDirectories.length == 0) {
			return Boolean.FALSE;
		}
		for (File timeIndexDirectory : timeIndexDirectories) {
			File[] serverIndexDirectories = timeIndexDirectory.listFiles();
			if (serverIndexDirectories == null) {
				continue;
			}
			for (File serverIndexDirectory : serverIndexDirectories) {
				boolean indexAlreadyOpen = Boolean.FALSE;
				for (Searchable searchable : searchables) {
					IndexSearcher indexSearcher = (IndexSearcher) searchable;
					IndexReader indexReader = indexSearcher.getIndexReader();
					FSDirectory fsDirectory = (FSDirectory) indexReader.directory();
					File indexDirectory = fsDirectory.getFile();
					files[0] = serverIndexDirectory;
					files[1] = indexDirectory;
					if (areDirectoriesEqual.evaluate(files)) {
						indexAlreadyOpen = directoryExistsAndNotLocked.evaluate(serverIndexDirectory);
						break;
					}
				}
				if (!indexAlreadyOpen) {
					LOGGER.debug(Logging.getString("Found new index directory : ", serverIndexDirectory, " will try to re-open : "));
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

}
