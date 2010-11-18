package ikube.action;

import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Delete extends AAction<IndexContext, Boolean> {

	@Override
	public Boolean execute(IndexContext indexContext) {
		String actionName = getClass().getName();
		if (getClusterManager().anyWorking(actionName)) {
			return Boolean.FALSE;
		}
		File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		File[] timeIndexDirectories = baseIndexDirectory.listFiles();
		if (timeIndexDirectories == null || timeIndexDirectories.length < 2) {
			return Boolean.FALSE;
		}
		try {
			getClusterManager().setWorking(indexContext, actionName, Boolean.TRUE, System.currentTimeMillis());
			List<File> timeIndexDirectoriesList = Arrays.asList(timeIndexDirectories);
			Collections.sort(timeIndexDirectoriesList, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			logger.debug("Index directories : " + timeIndexDirectoriesList);
			// Check if the last directory has a lock
			File latestIndexDirectory = timeIndexDirectoriesList.get(timeIndexDirectoriesList.size() - 1);
			boolean lastIndexLocked = Boolean.FALSE;
			try {
				lastIndexLocked = isLocked(latestIndexDirectory);
			} catch (Exception e) {
				logger.error("Exception checking which directories to delete : ", e);
			}

			int indexDirectoriesListSize = timeIndexDirectoriesList.size();
			int indexesToDelete = lastIndexLocked ? indexDirectoriesListSize - 2 : indexDirectoriesListSize - 1;
			for (File indexDirectory : timeIndexDirectoriesList) {
				logger.debug("Indexes to delete : " + indexesToDelete + ", " + indexDirectory);
				if (--indexesToDelete < 0) {
					break;
				}
				logger.debug("Deleting index : " + indexDirectory);
				FileUtilities.deleteFile(indexDirectory, 1);
			}
		} finally {
			getClusterManager().setWorking(indexContext, null, Boolean.FALSE, 0);
		}
		return Boolean.TRUE;
	}

	protected boolean isLocked(File latestIndexDirectory) {
		File[] serverIndexDirectories = latestIndexDirectory.listFiles();
		if (serverIndexDirectories == null || serverIndexDirectories.length == 0) {
			return Boolean.FALSE;
		}
		for (File serverIndexDirectory : serverIndexDirectories) {
			File[] contextIndexDirectories = serverIndexDirectory.listFiles();
			if (contextIndexDirectories == null || contextIndexDirectories.length == 0) {
				continue;
			}
			for (File contextIndexDirectory : contextIndexDirectories) {
				Directory directory = null;
				try {
					directory = FSDirectory.open(contextIndexDirectory);
					if (IndexWriter.isLocked(directory)) {
						return Boolean.TRUE;
					}
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					if (directory != null) {
						try {
							IndexWriter.unlock(directory);
							directory.close();
						} catch (Exception e) {
							logger.error("Exception closing the directory : " + directory, e);
						}
					}
				}
			}
		}
		return Boolean.FALSE;
	}

}