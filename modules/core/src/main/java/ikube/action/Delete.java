package ikube.action;

import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * This action deletes the old indexes. There should always be one index, and potentially an index that is being generated. Any other index
 * files should be deleted. 
 * 
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Delete extends Action {

	@Override
	public Boolean execute(IndexContext indexContext) {
		File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		File[] contextIndexDirectories = baseIndexDirectory.listFiles();
		if (contextIndexDirectories == null) {
			return Boolean.FALSE;
		}
		if (contextIndexDirectories.length == 0) {
			return Boolean.FALSE;
		}
		for (File contextIndexDirectory : contextIndexDirectories) {
			if (!contextIndexDirectory.getName().equals(indexContext.getIndexName())) {
				continue;
			}
			File[] timeIndexDirectories = contextIndexDirectory.listFiles();
			if (timeIndexDirectories == null) {
				continue;
			}
			if (timeIndexDirectories.length <= 1) {
				return Boolean.FALSE;
			}
			Arrays.sort(timeIndexDirectories, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			// Check if the last index directory is locked
			boolean latestIndexDirectoryIsLocked = Boolean.FALSE;
			File latestIndexDirectory = timeIndexDirectories[timeIndexDirectories.length - 1];
			File[] serverIndexDirectories = latestIndexDirectory.listFiles();
			for (File serverIndexDirectory : serverIndexDirectories) {
				if (directoryExistsAndIsLocked(serverIndexDirectory)) {
					latestIndexDirectoryIsLocked = Boolean.TRUE;
					break;
				}
			}
			int indexesToRemain = latestIndexDirectoryIsLocked ? 2 : 1;
			// We delete all the indexes except the last one, i.e. the latest index.
			// In the case that there is an index running then the latest index directory will
			// be locked, in that case we leave the last two indexes, i.e. the the latest index
			// and the index being created
			int indexesToDelete = timeIndexDirectories.length - indexesToRemain;
			if (indexesToDelete == 0) {
				return Boolean.FALSE;
			}
			for (int i = 0; i < indexesToDelete; i++) {
				File indexToDelete = timeIndexDirectories[i];
				logger.info("Deleting index directory : " + indexToDelete);
				FileUtilities.deleteFile(indexToDelete, 1);
			}
		}
		return Boolean.TRUE;
	}

}