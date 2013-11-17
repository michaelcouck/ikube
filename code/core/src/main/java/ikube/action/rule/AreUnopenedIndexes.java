package ikube.action.rule;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.FSDirectory;

/**
 * This rule checks whether there are indexes that are created but are not yet opened. This typically needs to be checked if an index is still in the process of
 * being generated. In this case when the index is finished being created the searcher should be opened on all the index directories.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class AreUnopenedIndexes implements IRule<IndexContext<?>> {

	private static final transient Logger LOGGER = Logger.getLogger(AreUnopenedIndexes.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		MultiSearcher searcher = indexContext.getMultiSearcher();
		Searchable[] searchables = searcher != null ? searcher.getSearchables() : null;
		if (searchables == null) {
			return new AreIndexesCreated().evaluate(indexContext);
		}
		File baseIndexDirectory = new File(IndexManager.getIndexDirectoryPath(indexContext));
		File[] timeIndexDirectories = baseIndexDirectory.listFiles();
		IRule<File[]> areDirectoriesEqual = new AreDirectoriesEqual();
		IRule<File> directoryExistsAndNotLocked = new DirectoryExistsAndNotLocked();
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
					try {
						IndexSearcher indexSearcher = (IndexSearcher) searchable;
						IndexReader indexReader = indexSearcher.getIndexReader();
						FSDirectory fsDirectory = (FSDirectory) indexReader.directory();
						File indexDirectory = fsDirectory.getFile();
						if (areDirectoriesEqual.evaluate(new File[] { serverIndexDirectory, indexDirectory })) {
							indexAlreadyOpen = directoryExistsAndNotLocked.evaluate(serverIndexDirectory);
							break;
						}
					} catch (Exception e) {
						LOGGER.error("Error checking index directory : " + serverIndexDirectory, e);
					}
				}
				if (!indexAlreadyOpen) {
					LOGGER.debug("Found new index directory : " + serverIndexDirectory + " will try to re-open : ");
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

}
