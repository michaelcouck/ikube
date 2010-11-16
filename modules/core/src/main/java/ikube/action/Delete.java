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
		File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		File[] indexDirectories = baseIndexDirectory.listFiles();
		if (indexDirectories == null || indexDirectories.length < 1) {
			return Boolean.FALSE;
		}
		String actionName = getClass().getName();
		if (getClusterManager().anyWorking(indexContext, actionName)) {
			return Boolean.FALSE;
		}
		try {
			List<File> indexDirectoriesList = Arrays.asList(indexDirectories);
			if (indexDirectoriesList.size() < 2) {
				return Boolean.FALSE;
			}
			getClusterManager().setWorking(indexContext, actionName, Boolean.TRUE, System.currentTimeMillis());
			Collections.sort(indexDirectoriesList, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			logger.debug("Index directories : " + indexDirectoriesList);
			// Check if the last directory has a lock
			File lastIndexDirectory = indexDirectoriesList.get(indexDirectoriesList.size() - 1);
			boolean lastIndexLocked = Boolean.FALSE;
			try {
				lastIndexLocked = isLocked(lastIndexDirectory);
			} catch (Exception e) {
				logger.error("Exception checking which directories to delete : ", e);
			}

			int indexDirectoriesListSize = indexDirectoriesList.size();
			int indexesToDelete = lastIndexLocked ? indexDirectoriesListSize - 2 : indexDirectoriesListSize - 1;
			for (File indexDirectory : indexDirectoriesList) {
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

	protected boolean isLocked(File indexDirectory) {
		File[] serverIndexDirectories = indexDirectory.listFiles();
		if (serverIndexDirectories == null || serverIndexDirectories.length == 0) {
			return Boolean.FALSE;
		}
		for (File serverIndexDirectory : serverIndexDirectories) {
			Directory directory = null;
			try {
				directory = FSDirectory.open(serverIndexDirectory);
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
		return Boolean.FALSE;
	}

}