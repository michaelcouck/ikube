package ikube.action;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * This action cleans old indexes that are corrupt or partially deleted. This class will also delete the files that were unpacked by the
 * indexing of the file system, i.e. the zip and jar files for example.
 * 
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Clean<E, F> extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean execute(final IndexContext<?> indexContext) {
		ikube.model.Action action = null;
		try {
			action = start(indexContext.getIndexName(), "");
			String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
			File baseIndexDirectory = FileUtilities.getFile(indexDirectoryPath, Boolean.TRUE);
			File[] timeIndexDirectories = baseIndexDirectory.listFiles();
			if (timeIndexDirectories == null || timeIndexDirectories.length == 0) {
				return Boolean.FALSE;
			}
			// Check all the directories to see if they are partially deleted or if
			// they appear not to be complete or corrupt then delete them
			for (File timeIndexDirectory : timeIndexDirectories) {
				File[] serverIndexDirectories = timeIndexDirectory.listFiles();
				if (serverIndexDirectories == null || serverIndexDirectories.length == 0) {
					// return Boolean.FALSE;
					continue;
				}
				processDirectories(indexContext, serverIndexDirectories);
			}
			FileUtilities.deleteFile(new File(IConstants.TMP_UNZIPPED_FOLDER), 1);
			return Boolean.TRUE;
		} finally {
			stop(action);
		}
	}

	private void processDirectories(IndexContext<?> indexContext, File... serverIndexDirectories) {
		for (File serverIndexDirectory : serverIndexDirectories) {
			Directory directory = null;
			boolean locked = Boolean.FALSE;
			boolean shouldDelete = Boolean.FALSE;
			try {
				directory = FSDirectory.open(serverIndexDirectory);
				locked = IndexWriter.isLocked(directory);
				if (locked) {
					// We assume that there are no other servers working so this directory
					// has been locked and the server is dead, we will unlock the index, and perhaps
					// try to optimize it too
					IndexWriter.unlock(directory);
					locked = IndexWriter.isLocked(directory);
				}
				if (locked) {
					logger.warn("Directory still locked : " + serverIndexDirectory);
				}
				// else {
				// // Open the index writer then close it which will optimise the index
				// IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, serverIndexDirectory, Boolean.FALSE);
				// indexContext.getIndex().setIndexWriter(indexWriter);
				// IndexManager.closeIndexWriter(indexContext);
				// }
				if (!IndexReader.indexExists(directory) || locked) {
					// Try to delete the directory
					shouldDelete = Boolean.TRUE;
				}
			} catch (IOException e) {
				logger.error("Directory : " + serverIndexDirectory + " not ok, will try to delete : ", e);
				shouldDelete = Boolean.TRUE;
			} catch (Exception e) {
				logger.error("Directory : " + serverIndexDirectory + " not ok, will try to delete : ", e);
				shouldDelete = Boolean.TRUE;
			} finally {
				if (directory != null) {
					try {
						directory.close();
					} catch (Exception e) {
						logger.error("Exception closing the directory : ", e);
					}
				}
				if (shouldDelete && !locked) {
					try {
						logger.warn("Deleting directory : " + serverIndexDirectory + ", as it either corrupt, or partially deleted : ");
						FileUtilities.deleteFile(serverIndexDirectory, 1);
					} catch (Exception e) {
						logger.error("Exception purging corrupt or partly deleted index directory : " + serverIndexDirectory, e);
					}
				}
			}
		}
	}

}