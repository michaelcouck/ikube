package ikube.action;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

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

	/** The minimum space that we will accept to carry on, 1 gig. */
	private static final long MINIMUM_FREE_SPACE = 1000;
	/** The amount of space until we start sending notifications, 10 gig. */
	private static final long MINIMUM_FREE_SPACE_FOR_NOTIFICATIONS = MINIMUM_FREE_SPACE * 10;

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean internalExecute(final IndexContext<?> indexContext) {
		File indexesDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		if (indexesDirectory == null || !indexesDirectory.exists() || !indexesDirectory.isDirectory()) {
			return Boolean.FALSE;
		}
		String drive = null;
		String directoryPath = indexesDirectory.getAbsolutePath();
		if (directoryPath.startsWith(IConstants.SEP + IConstants.SEP) || directoryPath.startsWith(IConstants.BCK_SEP + IConstants.BCK_SEP)) {
			// This drive is on the network we can't check the space
		} else if (directoryPath.startsWith(IConstants.SEP)) {
			// This is unix, just get the space on the drive
			drive = directoryPath;
		} else {
			// Windows
			char driveCharacter = indexesDirectory.getAbsolutePath().charAt(0);
			drive = driveCharacter + ":";
		}
		if (drive != null) {
			Long freeSpaceKilobytes = null;
			try {
				freeSpaceKilobytes = FileSystemUtils.freeSpaceKb(drive);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			Long freeSpaceMegabytes = freeSpaceKilobytes / 1000;
			String subject = "No more disk space on server!";
			if (freeSpaceMegabytes < MINIMUM_FREE_SPACE) {
				// We need to exit this server as the disk will crash
				String body = buildMessage("We have run out of disk space on this drive : ", indexesDirectory.toString(),
						"All indexing will be terminated immediately : ", freeSpaceMegabytes.toString());
				logger.error(subject + " " + body);
				sendNotification(subject, body);
				// Terminate all indexing
				ThreadUtilities.destroy();
				// System.exit(0);
				return Boolean.TRUE;
			}
			if (freeSpaceMegabytes < MINIMUM_FREE_SPACE_FOR_NOTIFICATIONS) {
				String body = buildMessage("We have run out of disk space on this drive : ", indexesDirectory.toString(),
						"Please clean this disk, free space available : ", freeSpaceMegabytes.toString());
				logger.error(subject + " " + body);
				sendNotification(subject, body);
				return Boolean.TRUE;
			}
		}
		return Boolean.TRUE;
	}

	private String buildMessage(final String... strings) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String string : strings) {
			stringBuilder.append(string);
		}
		return stringBuilder.toString();
	}

}