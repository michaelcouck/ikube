package ikube.action;

import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * This action cleans old indexes that are corrupt or partially deleted.
 * 
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Clean extends Action<IndexContext, Boolean> {

	@Override
	public Boolean execute(final IndexContext indexContext) {
		String indexDirectoryPath = indexContext.getIndexDirectoryPath() + File.separator + indexContext.getIndexName();
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
				return Boolean.FALSE;
			}
			for (File serverIndexDirectory : serverIndexDirectories) {
				boolean shouldDelete = Boolean.FALSE;
				Directory directory = null;
				try {
					directory = FSDirectory.open(serverIndexDirectory);
					if (IndexWriter.isLocked(directory)) {
						continue;
					}
					if (!IndexReader.indexExists(directory)) {
						// Try to delete the directory
						shouldDelete = Boolean.TRUE;
					}
				} catch (IOException e) {
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
					if (shouldDelete) {
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
		return Boolean.TRUE;
	}

}