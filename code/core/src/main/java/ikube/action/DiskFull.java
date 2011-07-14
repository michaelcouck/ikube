package ikube.action;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileSystemUtils;

/**
 * This action checks that the disk is not full, the one where the indexes are, if it is then this instance will close down.
 * 
 * @author Michael Couck
 * @since 02.06.11
 * @version 01.00
 */
public class DiskFull extends Action<IndexContext<?>, Boolean> {

	/** The minimum space that we will accept to carry on, 10 gig. */
	private static final long MINIMUM_FREE_SPACE = 10000;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean execute(final IndexContext<?> indexContext) {
		try {
			File indexesDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			if (!indexesDirectory.exists() || !indexesDirectory.isDirectory()) {
				return Boolean.FALSE;
			}
			String drive = null;
			if (indexesDirectory.getAbsolutePath().startsWith(IConstants.SEP)) {
				// This is unix, just get the space on the drive
				drive = IConstants.SEP;
			} else {
				// Windows
				char driveCharacter = indexesDirectory.getAbsolutePath().charAt(0);
				drive = driveCharacter + ":";
			}
			try {
				long freeSpaceKilobytes = FileSystemUtils.freeSpaceKb(drive);
				long freeSpaceMegabytes = freeSpaceKilobytes / 1000;
				logger.debug("Free space : " + freeSpaceMegabytes + ", " + MINIMUM_FREE_SPACE);
				if (freeSpaceMegabytes < MINIMUM_FREE_SPACE) {
					// We need to exit this server as the disk will crash
					String subject = "No more disk space on server!";
					String body = "We have run out of disk space on this driver : " + indexesDirectory;
					body += "This server will exit to save the machine : " + freeSpaceMegabytes;
					logger.fatal(subject);
					logger.fatal(body);
					sendNotification(subject, body);
					System.exit(0);
					return Boolean.TRUE;
				}
			} catch (IOException e) {
				logger.error("Exception looking for the free space : " + indexesDirectory, e);
			}
		} finally {
			getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getSimpleName(), "", Boolean.FALSE);
		}
		return Boolean.FALSE;
	}

}