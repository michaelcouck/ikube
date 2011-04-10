package ikube.action.rule;

import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * This is the base class for rules, just with common functionality that could be used by many rules.
 * 
 * @author Michael Couck
 * @since 10.04.11
 * @version 01.00
 * @param <T>
 */
public abstract class ARule<T> implements IRule<T> {

	protected Logger logger = Logger.getLogger(this.getClass());

	/**
	 * This method goes through all the server index directories in the latest index directory and checks that each index is created and not
	 * corrupt. All indexes that are still locked are ignored.
	 * 
	 * @param baseIndexDirectoryPath
	 *            the path to the base index directory
	 * @return whether all the server indexes are created and not corrupt
	 */
	protected boolean indexesExist(String baseIndexDirectoryPath) {
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(baseIndexDirectoryPath);
		if (latestIndexDirectory == null) {
			return Boolean.FALSE;
		}
		File[] serverIndexDirectories = latestIndexDirectory.listFiles();
		if (serverIndexDirectories == null || serverIndexDirectories.length == 0) {
			return Boolean.FALSE;
		}
		for (File serverIndexDirectory : serverIndexDirectories) {
			Directory directory = null;
			try {
				directory = FSDirectory.open(serverIndexDirectory);
				if (IndexWriter.isLocked(directory)) {
					continue;
				}
				if (!IndexReader.indexExists(directory)) {
					return Boolean.FALSE;
				}
			} catch (IOException e) {
				logger.error("Exception checking the index directories : ", e);
			} finally {
				if (directory != null) {
					try {
						directory.close();
					} catch (Exception e) {
						logger.error("Exception closing the directory : ", e);
					}
				}
			}
		}
		return Boolean.TRUE;
	}

	/**
	 * This method checks to see that the index has not passed it's validity period, i.e. that the age of the index, determined by it's
	 * folder name, is not older than the max age that is defined in the index context for the index.
	 * 
	 * @param indexContext
	 *            the index context to check for up to date index(es)
	 * @param indexDirectoryPath
	 *            the index directory path to the indexes for this context, could be the back indexes too of course
	 * @return whether the index defined by the index path is current
	 */
	protected boolean isIndexCurrent(IndexContext indexContext, String indexDirectoryPath) {
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexDirectoryPath);
		if (latestIndexDirectory == null) {
			return Boolean.FALSE;
		}
		String indexDirectoryName = latestIndexDirectory.getName();
		long indexDirectoryTime = Long.parseLong(indexDirectoryName);
		long currentTime = System.currentTimeMillis();
		long indexAge = currentTime - indexDirectoryTime;
		if (indexAge > indexContext.getMaxAge()) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

}
