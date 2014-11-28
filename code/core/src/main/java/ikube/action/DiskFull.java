package ikube.action;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.apache.commons.io.FileSystemUtils;
import org.springframework.beans.factory.annotation.Value;

/**
 * This action checks that the disk is not full, the one where the indexes are, if it is then this instance will close down.
 * 
 * @author Michael Couck
 * @since 02.06.11
 * @version 01.00
 */
public class DiskFull extends Action<IndexContext, Boolean> {

	@Value("${minimum.free.space}")
	private long minimumFreeSpace = 1000; // In megs
	@Value("${minimum.free.space.for.notifications}")
	private long minimumFreeSpaceForNotifications = 10000; // In megs

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean internalExecute(final IndexContext indexContext) {
		File indexesDirectory = FILE.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		if (indexesDirectory == null || !indexesDirectory.exists() || !indexesDirectory.isDirectory()) {
			return Boolean.FALSE;
		}
		String drive = null;
		String directoryPath = indexesDirectory.getAbsolutePath();
		String osName = System.getProperty("os.name").toLowerCase();
		if (directoryPath.startsWith(IConstants.SEP + IConstants.SEP) || directoryPath.startsWith(IConstants.BCK_SEP + IConstants.BCK_SEP)) {
			// This drive is on the network we can't check the space
		} else if (osName.contains("linux") || osName.contains("nix")) {
			// This is *nix
			drive = getNixRoot(directoryPath);
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
				THREAD.destroy();
				THREAD.cancelAllForkJoinPools();
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

	/**
	 * This method will look for the path of the index and compare it to the mounted partitions on nix*. The reason for this is that in the case of a path on a
	 * mounted partition, we don't want the size of the root partition, we specifically want the partition size, but we need to search for it in the mount
	 * command because there isn't anything in Java to get the mounted volume information.
	 * 
	 * For example if the path of the index directory is /media/usb/indexes, the root should be something like /dev/sdb1. When we feed this into the df -Thk
	 * command we'll get the 'real' values for the mounted partition, not the root volume.
	 * 
	 * @param path the path of the file/directory that we want the mounted (or root) volume for
	 * @return the volume where the folder/file is on the file system, could even be on the network in *nix
	 */
	String getNixRoot(final String path) {
		Process ps = null;
		BufferedReader reader = null;
		try {
			// First check if this path is on a mounted partition
			String rootPartition = null;
			Runtime rt = Runtime.getRuntime();
			ps = rt.exec("mount");
			InputStream in = ps.getInputStream();
			reader = new BufferedReader(new InputStreamReader(in));
			String line = reader.readLine();
			if (line != null) {
				do {
					StringTokenizer stringTokenizer = new StringTokenizer(line, " ");
					String firstToken = stringTokenizer.nextToken();
					for (String token = stringTokenizer.nextToken(); stringTokenizer.hasMoreTokens(); token = stringTokenizer.nextToken()) {
						if (path.contains(token)) {
							rootPartition = firstToken;
							break;
						}
					}
					line = reader.readLine();
				} while (line != null);
			}
			// If not on a mounted partition then this must be on the root partition
			if (rootPartition == null) {
				rootPartition = IConstants.SEP;
			}
			return rootPartition;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (ps != null) {
				ps.destroy();
			}
			if (reader != null) {
				FILE.close(reader);
			}
		}
	}

	private String buildMessage(final String... strings) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String string : strings) {
			stringBuilder.append(string);
		}
		return stringBuilder.toString();
	}

}