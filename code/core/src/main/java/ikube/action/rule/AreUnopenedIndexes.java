package ikube.action.rule;

import ikube.logging.Logging;
import ikube.model.IndexContext;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class AreUnopenedIndexes implements IRule<IndexContext> {

	private Logger logger = Logger.getLogger(this.getClass());
	private boolean expected;

	public boolean evaluate(IndexContext indexContext) {
		Searchable[] searchables = indexContext.getIndex().getMultiSearcher().getSearchables();
		File baseIndexDirectory = new File(indexContext.getIndexDirectoryPath() + File.separator + indexContext.getIndexName());
		File[] timeIndexDirectories = baseIndexDirectory.listFiles();
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
					IRule<File[]> areDirectoriesEqual = new AreDirectoriesEqual();
					if (areDirectoriesEqual.evaluate(new File[] { serverIndexDirectory, indexDirectory })) {
						IRule<File> directoryExistsAndNotLocked = new DirectoryExistsAndNotLocked();
						indexAlreadyOpen = directoryExistsAndNotLocked.evaluate(serverIndexDirectory);
						break;
					}
				}
				if (!indexAlreadyOpen) {
					logger.debug(Logging.getString("Found new index directory : ", serverIndexDirectory, " will try to re-open : "));
					return Boolean.TRUE == expected;
				}
			}
		}
		return Boolean.FALSE == expected;
	}

	@Override
	public void setExpected(boolean expected) {
		this.expected = expected;
	}

}
