package ikube.action;

import ikube.cluster.IClusterManager;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class AAction<E, F> implements IAction<E, F> {

	protected Logger logger = Logger.getLogger(this.getClass());
	private IClusterManager clusterManager;

	protected IClusterManager getClusterManager() {
		if (clusterManager == null) {
			clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		}
		return clusterManager;
	}

	protected boolean isIndexCurrent(IndexContext indexContext) {
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
		if (latestIndexDirectory == null) {
			return Boolean.FALSE;
		}
		String indexDirectoryName = latestIndexDirectory.getName();
		long indexDirectoryTime = Long.parseLong(indexDirectoryName);
		long currentTime = System.currentTimeMillis();
		long indexAge = currentTime - indexDirectoryTime;
		// logger.debug("Directory time : " + indexDirectoryTime + ", " + new Date(indexDirectoryTime) + ", index age : " + indexAge
		// + ", max age : " + indexContext.getMaxAge());
		if (indexAge < indexContext.getMaxAge()) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * This method returns whether the searcher should be re-opened on a new index. If there is a new index, or if there is an index added
	 * by another server then the searcher should be opened again.
	 *
	 * @param indexContext
	 *            the index context for the index
	 * @return whether the index should be re-opened
	 */
	protected boolean shouldReopen(IndexContext indexContext) {
		// If there is no searcher open then try to open one
		MultiSearcher multiSearcher = indexContext.getMultiSearcher();
		if (multiSearcher == null) {
			logger.debug("Multi searcher null, should try to reopen : ");
			return Boolean.TRUE;
		}
		// No searchables, also try to reopen an index searcher
		Searchable[] searchables = multiSearcher.getSearchables();
		if (searchables == null || searchables.length == 0) {
			logger.debug("No searchables open, should try to reopen : ");
			return Boolean.TRUE;
		}
		// First check that there is an index directory
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
		if (latestIndexDirectory == null) {
			logger.debug("Latest index directory null : ");
			return Boolean.FALSE;
		}
		long time = Long.parseLong(latestIndexDirectory.getName());
		if (System.currentTimeMillis() - time > indexContext.getMaxAge()) {
			logger.info("Index out of date : " + latestIndexDirectory);
			return Boolean.FALSE;
		}
		// Check that there are indexes in the index directory
		File[] serverIndexDirectories = latestIndexDirectory.listFiles();
		if (serverIndexDirectories == null || serverIndexDirectories.length == 0) {
			logger.warn(Logging.getString("Server directories null or empty, deleted perhaps : ", latestIndexDirectory, ", index name : ",
					indexContext.getIndexName()));
			return Boolean.FALSE;
		}
		// Now we check the latest server index directories against the directories
		// in the existing multi-searcher. If there are server index directories that are
		// not included in the searchables then try to close the multi searcher and
		// re-open it
		for (File serverIndexDirectory : serverIndexDirectories) {
			File[] indexDirectories = serverIndexDirectory.listFiles();
			for (File indexDirectory : indexDirectories) {
				// Now check that there is a searchable open on the server index directory
				boolean indexAlreadyOpen = Boolean.FALSE;
				for (Searchable searchable : searchables) {
					IndexSearcher indexSearcher = (IndexSearcher) searchable;
					IndexReader indexReader = indexSearcher.getIndexReader();
					FSDirectory fsDirectory = (FSDirectory) indexReader.directory();
					File searchIndexDirectory = fsDirectory.getFile();
					if (logger.isDebugEnabled()) {
						String indexPath = indexDirectory.getAbsolutePath();
						String searchPath = searchIndexDirectory.getAbsolutePath();
						String message = Logging.getString("Index directory : ", indexPath, ", index directory : ", searchPath);
						logger.debug(message);
					}
					if (directoriesEqual(indexDirectory, searchIndexDirectory)) {
						// indexAlreadyOpen = Boolean.TRUE;
						indexAlreadyOpen = directoryExistsAndNotLocked(indexDirectory);
						break;
					}
				}
				if (!indexAlreadyOpen) {
					logger.debug(Logging.getString("Found new index directory : ", indexDirectory, " will try to re-open : "));
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	private boolean directoryExistsAndNotLocked(File indexDirectory) {
		Directory directory = null;
		try {
			directory = FSDirectory.open(indexDirectory);
			boolean exists = IndexReader.indexExists(directory);
			boolean locked = IndexWriter.isLocked(directory);
			logger.info(Logging.getString("Server index directory : ", indexDirectory, ", exists : ", exists, ", locked : ", locked));
			// Could be that the index is still being written, unlikely, or
			// that there are directories in the base directory that are
			// not index directories
			if (exists && !locked) {
				return Boolean.TRUE;
			} else {
				logger.info("Non existant or locked directory found, will not open on this one yet : " + directory);
				// return Boolean.FALSE;
			}
		} catch (Exception e) {
			logger.error("Exception checking the directories : ", e);
			return Boolean.FALSE;
		} finally {
			try {
				directory.close();
			} catch (Exception e) {
				logger.error("Exception closing the directory : " + directory, e);
			}
		}
		return Boolean.FALSE;
	}

	private boolean directoriesEqual(File directoryOne, File directoryTwo) {
		// Just check the server directory name and the parent folder name, should be
		// something like '1234567890/jackal' or '1234567890/127.0.0.1'
		String nameOne = directoryOne.getName();
		String nameTwo = directoryTwo.getName();
		String parentNameOne = directoryOne.getParentFile().getName();
		String parentNameTwo = directoryTwo.getParentFile().getName();
		return nameOne.equals(nameTwo) && parentNameOne.equals(parentNameTwo);

	}

}