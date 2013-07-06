package ikube.action;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileSystemUtils;
import org.springframework.beans.factory.annotation.Value;

/**
 * This action checks that the disk is not full, the one where the indexes are, if it is then this instance will close down.
 * 
 * @author Michael Couck
 * @since 02.06.11
 * @version 01.00
 */
public class DiskFull extends Action<IndexContext<?>, Boolean> {

	@Value("${minimum.free.space}")
	private long minimumFreeSpace = 1000; // In megs
	@Value("${minimum.free.space.for.notifications}")
	private long minimumFreeSpaceForNotifications = 10000; // In megs

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
			if (freeSpaceMegabytes < minimumFreeSpace) {
				// We need to exit this server as the disk will crash
				String body = buildMessage("We have run out of disk space on this drive : ", indexesDirectory.toString(),
						"All indexing will be terminated immediately : ", freeSpaceMegabytes.toString());
				logger.error(subject + " " + body);
				sendNotification(subject, body);
				// Terminate all indexing
				ThreadUtilities.destroy();
				ThreadUtilities.cancellAllForkJoinPools();
				return Boolean.TRUE;
			}
			if (freeSpaceMegabytes < minimumFreeSpaceForNotifications) {
				String body = buildMessage("We have run out of disk space on this drive : ", indexesDirectory.toString(),
						"Please clean this disk, free space available : ", freeSpaceMegabytes.toString());
				logger.error(subject + " " + body);
				sendNotification(subject, body);
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	private String buildMessage(final String... strings) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String string : strings) {
			stringBuilder.append(string);
		}
		return stringBuilder.toString();
	}

}