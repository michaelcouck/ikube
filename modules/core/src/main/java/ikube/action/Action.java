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
 * This is the base class for actions. Actions execute handlers on indexables. Actions may include opening the searcher on a new index,
 * indexing or deleting the old indexes. This class is intended to be sub-classed. Common methods in this base class is checking that the
 * index is current, i.e. has not expired and whether the searcher should be re-opened on the nex index.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class Action implements IAction<IndexContext, Boolean> {

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
		// No SEARCHABLES, also try to reopen an index searcher
		Searchable[] searchables = multiSearcher.getSearchables();
		if (searchables == null || searchables.length == 0) {
			logger.debug("No SEARCHABLES open, should try to reopen : ");
			return Boolean.TRUE;
		}
		if (!isIndexCurrent(indexContext)) {
			return Boolean.FALSE;
		}
		File baseIndexDirectory = new File(indexContext.getIndexDirectoryPath());
		File[] contextIndexDirectories = baseIndexDirectory.listFiles();
		if (contextIndexDirectories == null) {
			return Boolean.FALSE;
		}
		for (File contextIndexDirectory : contextIndexDirectories) {
			File[] timeIndexDirectories = contextIndexDirectory.listFiles();
			if (timeIndexDirectories == null) {
				continue;
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
						if (directoriesEqual(serverIndexDirectory, indexDirectory)) {
							indexAlreadyOpen = directoryExistsAndNotLocked(serverIndexDirectory);
							break;
						}
					}
					if (!indexAlreadyOpen) {
						logger.debug(Logging.getString("Found new index directory : ", serverIndexDirectory, " will try to re-open : "));
						return Boolean.TRUE;
					}
				}
			}
		}
		return Boolean.FALSE;
	}

	protected boolean directoryExistsAndNotLocked(File indexDirectory) {
		Directory directory = null;
		try {
			directory = FSDirectory.open(indexDirectory);
			boolean exists = IndexReader.indexExists(directory);
			boolean locked = IndexWriter.isLocked(directory);
			logger.info(Logging.getString("Server index directory : ", indexDirectory, ", exists : ", exists, ", locked : ", locked));
			if (exists && !locked) {
				return Boolean.TRUE;
			} else {
				logger.info("Non existant or locked directory found, will not open on this one yet : " + directory);
			}
		} catch (Exception e) {
			logger.error("Exception checking the directories : ", e);
		} finally {
			try {
				directory.close();
			} catch (Exception e) {
				logger.error("Exception closing the directory : " + directory, e);
			}
		}
		return Boolean.FALSE;
	}

	protected boolean directoryExistsAndIsLocked(File indexDirectory) {
		Directory directory = null;
		try {
			directory = FSDirectory.open(indexDirectory);
			boolean exists = IndexReader.indexExists(directory);
			boolean locked = IndexWriter.isLocked(directory);
			logger.info(Logging.getString("Server index directory : ", indexDirectory, ", exists : ", exists, ", locked : ", locked));
			if (exists && locked) {
				return Boolean.TRUE;
			} else {
				logger.info("Locked directory : " + directory);
			}
		} catch (Exception e) {
			logger.error("Exception checking the directories : ", e);
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
		if (directoryOne == null || directoryTwo == null) {
			return false;
		}
		String nameOne = directoryOne.getName();
		String nameTwo = directoryTwo.getName();
		String parentNameOne = directoryOne.getParentFile().getName();
		String parentNameTwo = directoryTwo.getParentFile().getName();
		return nameOne.equals(nameTwo) && parentNameOne.equals(parentNameTwo);
	}

}