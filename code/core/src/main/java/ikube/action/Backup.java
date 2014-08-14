package ikube.action;

import ikube.IConstants;
import ikube.model.IndexContext;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import static ikube.action.index.IndexManager.*;
import static ikube.toolkit.FileUtilities.getFile;

/**
 * This class backs up the index to a place on the network in the case where the index becomes corrupted.
 * 
 * @author Michael Couck
 * @since 08-04-2011
 * @version 01.00
 */
public class Backup extends Action<IndexContext, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean internalExecute(final IndexContext indexContext) {
		String indexDirectoryPath = getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = getLatestIndexDirectory(indexDirectoryPath);
		if (latestIndexDirectory == null || !latestIndexDirectory.exists()) {
			logger.warn("No index created for context : " + indexContext.getName());
			return Boolean.FALSE;
		}
		String indexDirectoryPathBackup = getIndexDirectoryPathBackup(indexContext);
		File indexDirectoryBackup = getFile(indexDirectoryPathBackup, Boolean.TRUE);
		if (indexDirectoryBackup == null || !indexDirectoryBackup.exists()) {
			logger.warn("Backup directory could not be created : " + indexDirectoryBackup);
		}
		if (indexDirectoryPath.equals(indexDirectoryPathBackup)) {
			// The index and the backup are the same to save disk space
			return Boolean.TRUE;
		}
		String latestIndexDirectoryBackupPath = indexDirectoryPathBackup + IConstants.SEP + latestIndexDirectory.getName();
		File latestIndexDirectoryBackup = getFile(latestIndexDirectoryBackupPath, Boolean.TRUE);
		logger.info("Backing up index from : " + latestIndexDirectory + ", to : " + latestIndexDirectoryBackup);
		try {
            // Copy the index to the designated place on the network
            FileFilter fileFilter = new FileFilter() {
                @Override
                public boolean accept(final File file) {
                    try {
                        return file.exists() && file.canRead() && !FileUtils.isSymlink(file);
                    } catch (final IOException e) {
                        return Boolean.FALSE;
                    }
                }
            };
			FileUtils.copyDirectory(latestIndexDirectory, latestIndexDirectoryBackup, fileFilter, Boolean.TRUE);
			logger.info("Backed up index from : " + latestIndexDirectory + ", to : " + latestIndexDirectoryBackup);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return Boolean.TRUE;
	}

}