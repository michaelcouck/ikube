package ikube.action;

import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * TODO Implement this class. This action cleans old indexes that are corrupt or partially deleted.
 * 
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Clean extends Action {

	@Override
	public Boolean execute(final IndexContext indexContext) {
		String indexDirectoryPath = indexContext.getIndexDirectoryPath() + File.separator + indexContext.getIndexName();
		File baseIndexDirectory = FileUtilities.getFile(indexDirectoryPath, Boolean.TRUE);
		File[] timeIndexDirectories = baseIndexDirectory.listFiles();
		if (timeIndexDirectories == null) {
			return Boolean.FALSE;
		}
		if (timeIndexDirectories.length <= 1) {
			return Boolean.FALSE;
		}
		Arrays.sort(timeIndexDirectories, new Comparator<File>() {
			@Override
			public int compare(final File fileOne, final File fileTwo) {
				return fileOne.getName().compareTo(fileTwo.getName());
			}
		});
		// TODO Check all the directories to see if they are partially deleted or if
		// they appear not to be complete or corrupt then delete them
		return Boolean.TRUE;
	}

}