package ikube.toolkit;

import ikube.IConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class FileUtilities {

	private static Logger LOGGER = Logger.getLogger(FileUtilities.class);

	public static void deleteFiles(File folder, String[] stringPatterns) {
		File[] tempFiles = FileUtilities.findFiles(folder, stringPatterns);
		for (File tempFile : tempFiles) {
			FileUtilities.deleteFile(tempFile, 3);
		}
	}

	public static File[] findFiles(File folder, String[] stringPatterns) {
		final Pattern[] patterns = new Pattern[stringPatterns.length];
		int index = 0;
		for (String stringPattern : stringPatterns) {
			StringBuilder builder = new StringBuilder(".*(").append(stringPattern).append(").*");
			patterns[index++] = Pattern.compile(builder.toString());
		}
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				String pathName = file.getName();
				// LOGGER.debug("Path name : " + pathName);
				for (Pattern pattern : patterns) {
					if (pattern.matcher(pathName).matches()) {
						return Boolean.TRUE;
					}
				}
				return Boolean.FALSE;
			}
		});
		return files;
	}

	public static File findFile(File folder, String[] stringPatterns, List<File> files) {
		files = FileUtilities.findFilesRecursively(folder, stringPatterns, files);
		return files.size() > 0 ? files.get(0) : null;
	}

	public static List<File> findFilesRecursively(File folder, String[] stringPatterns, List<File> files) {
		if (folder.isDirectory()) {
			File[] folderFiles = FileUtilities.findFiles(folder, stringPatterns);
			files.addAll(Arrays.asList(folderFiles));
			File[] childFolders = folder.listFiles();
			for (File childFolder : childFolders) {
				findFilesRecursively(childFolder, stringPatterns, files);
			}
		}
		return files;
	}

	/**
	 * Deletes the file/folder recursively. If the file cannot be deleted then the file is set to delete on exit of the JVM, which doesn't
	 * generally work of course, but we try anyway.
	 *
	 * @param file
	 *            the file/folder to delete
	 * @param maxRetryCount
	 *            the number of times to re-try the delete operation
	 */
	public static void deleteFile(File file, int maxRetryCount) {
		FileUtilities.deleteFile(file, maxRetryCount, 0);
	}

	public static synchronized File getFile(String filePath, boolean directory) {
		try {
			File file = new File(filePath);
			if (directory) {
				if (file.exists() && file.isDirectory()) {
					return file;
				}
				boolean created = file.mkdirs();
				if (created && file.exists()) {
					return file;
				}
			} else {
				if (file.exists() && file.isFile()) {
					return file;
				}
				File parent = file.getParentFile();
				parent = FileUtilities.getFile(parent.getAbsolutePath(), Boolean.TRUE);
				if (parent != null) {
					try {
						boolean created = file.createNewFile();
						if (created && file.exists()) {
							return file;
						}
					} catch (IOException e) {
						LOGGER.error("Exception creating file : " + file, e);
					}
				}
			}
			return null;
		} finally {
			FileUtilities.class.notifyAll();
		}
	}

	protected void deleteTempFiles() {
		// Delete all the temp reader files on the file system
		File tempFolder = new File(System.getProperty("java.io.tmpdir"));
		FileUtilities.deleteFiles(tempFolder, new String[] { IConstants.READER_FILE_SUFFIX });
	}

	protected static void deleteFile(File file, int maxRetryCount, int retryCount) {
		if (file == null || !file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			File children[] = file.listFiles();
			for (int j = 0; j < children.length; j++) {
				File child = children[j];
				deleteFile(child, maxRetryCount, 0);
			}
		}
		if (file.delete()) {
			// LOGGER.debug("Deleted file : " + file);
		} else {
			if (retryCount >= maxRetryCount) {
				if (file.exists()) {
					LOGGER.debug("Couldn't delete file : " + file);
					LOGGER.debug("Will try to delete on exit : ");
					file.deleteOnExit();
				}
			} else {
				LOGGER.debug("Retrying count : " + retryCount + ", file : " + file);
				deleteFile(file, maxRetryCount, ++retryCount);
			}
		}
	}

	public static synchronized File getLatestIndexDirectory(String baseIndexDirectoryPath) {
		try {
			File baseIndexDirectory = FileUtilities.getFile(baseIndexDirectoryPath, Boolean.TRUE);
			LOGGER.debug("Base index directory : " + baseIndexDirectory);
			File[] indexDirectories = baseIndexDirectory.listFiles();
			File latestIndexDirectory = null;
			for (File indexDirectory : indexDirectories) {
				LOGGER.debug("Index directory : " + indexDirectory);
				if (!indexDirectory.isDirectory()) {
					continue;
				}
				String indexDirectoryName = indexDirectory.getName();
				long indexDirectoryTime = Long.parseLong(indexDirectoryName);
				if (latestIndexDirectory == null) {
					latestIndexDirectory = indexDirectory;
					continue;
				}
				long latestIndexDirectoryTime = Long.parseLong(latestIndexDirectory.getName());
				if (indexDirectoryTime > latestIndexDirectoryTime) {
					latestIndexDirectory = indexDirectory;
				}
			}
			return latestIndexDirectory;
		} finally {
			FileUtilities.class.notifyAll();
		}
	}

	/**
	 * Writes the contents of a byte array to a file.
	 *
	 * @param file
	 *            the file to write to
	 * @param bytes
	 *            the byte data to write
	 */
	public static void setContents(String filePath, byte[] bytes) {
		File file = FileUtilities.getFile(filePath, Boolean.FALSE);
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(bytes, 0, bytes.length);
		} catch (FileNotFoundException e) {
			LOGGER.error("File " + file + " not found", e);
		} catch (IOException e) {
			LOGGER.error("IO exception writing file contents", e);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					LOGGER.error("Exception closing the output stream", e);
				}
			}
		}
	}

	/**
	 * Reads the contents of the file and returns the contents in a byte array form.
	 *
	 * @param file
	 *            the file to read the contents from
	 * @return the file contents in a byte array output stream
	 * @throws Exception
	 */
	public static ByteArrayOutputStream getContents(File file) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			LOGGER.error("No file by that name.", e);
		} catch (Exception e) {
			LOGGER.error("General error accessing the file " + file, e);
		}
		return getContents(inputStream, Integer.MAX_VALUE);
	}

	/**
	 * Reads the contents of the file and returns the contents in a byte array form.
	 *
	 * @param inputStream
	 *            the file to read the contents from
	 * @param maxLength
	 *            the maximum number of bytes to read into the buffer
	 * @return the file contents in a byte array output stream
	 * @throws Exception
	 */
	public static ByteArrayOutputStream getContents(InputStream inputStream, long maxLength) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if (inputStream == null) {
			return bos;
		}
		try {
			byte[] bytes = new byte[1024];
			int read;
			while ((read = inputStream.read(bytes)) > -1 && bos.size() < maxLength) {
				bos.write(bytes, 0, read);
			}
		} catch (Exception e) {
			LOGGER.error("Exception accessing the file contents.", e);
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
				LOGGER.error("Exception closing input stream " + inputStream, e);
			}
		}
		return bos;
	}

}
