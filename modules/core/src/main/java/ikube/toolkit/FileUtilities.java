package ikube.toolkit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class FileUtilities {

	private static Logger LOGGER = Logger.getLogger(FileUtilities.class);

	/**
	 * Deletes all files recursively, that have the specified pattern in the path. Note that this is dangerous and you really need to know
	 * what files are in the directory that you feed this method. There is no turning back, these files will be completely deelted, n
	 * re-cycle bin and all that.
	 * 
	 * @param file
	 *            the top level directory or file to start looking into
	 * @param stringPatterns
	 *            the patterns to look for in the file paths
	 */
	public static void deleteFiles(File file, String... stringPatterns) {
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles != null && childFiles.length > 0) {
				for (File childFile : childFiles) {
					deleteFiles(childFile, stringPatterns);
				}
			}
		}
		Pattern pattern = getPattern(stringPatterns);
		if (pattern.matcher(file.getName()).matches()) {
			// This deletes the file recursively
			FileUtilities.deleteFile(file, 1);
		}
	}

	/**
	 * Creates the pattern object from the regular expression patterns.
	 * 
	 * @param stringPatterns
	 *            the regular expression patterns
	 * @return the pattern generated from the strings
	 */
	public static Pattern getPattern(String... stringPatterns) {
		boolean first = Boolean.TRUE;
		StringBuilder builder = new StringBuilder();
		for (String stringPattern : stringPatterns) {
			if (!first) {
				// Or
				builder.append("|");
			} else {
				first = Boolean.FALSE;
			}
			// Concatenate the 'any character' regular expression to the string pattern
			builder.append(".*(").append(stringPattern).append(").*");
		}
		Pattern pattern = Pattern.compile(builder.toString());
		return pattern;
	}

	/**
	 * Finds files with the specified pattern only in the folder specified in the parameter list, i.e. not recursively.
	 * 
	 * @param folder
	 *            the folder to look for files in
	 * @param stringPatterns
	 *            the pattern to look for in the file path
	 * @return an array of files with the specified pattern in the path
	 */
	public static File[] findFiles(File folder, String[] stringPatterns) {
		final Pattern pattern = getPattern(stringPatterns);
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				String pathName = file.getName();
				if (pattern.matcher(pathName).matches()) {
					return Boolean.TRUE;
				}
				return Boolean.FALSE;
			}
		});
		return files;
	}

	/**
	 * This method looks through all the files defined in the folder in the parameter list, recursively, and gets the first one that matches
	 * the pattern.
	 * 
	 * @param folder
	 *            the folder to start looking through
	 * @param stringPatterns
	 *            the patterns to look for in the file paths
	 * @return the first file that was encountered that has the specified pattern(s) in it
	 */
	public static File findFile(File folder, String... stringPatterns) {
		List<File> files = FileUtilities.findFilesRecursively(folder, stringPatterns, new ArrayList<File>());
		return files.size() > 0 ? files.get(0) : null;
	}

	/**
	 * This method will look through all the files in the top level folder, and all the sub folders, adding files to the list when they
	 * match the patterns that are provided.
	 * 
	 * @param folder
	 *            the folder to start looking through
	 * @param stringPatterns
	 *            the patterns to match the file paths with
	 * @param files
	 *            the files list to add all the files to
	 * @return the list of files that match the patterns
	 */
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
	public static boolean deleteFile(File file, int maxRetryCount) {
		return FileUtilities.deleteFile(file, maxRetryCount, 0);
	}

	/**
	 * Gets a single file. First looking to find it, if it can not be found then it is created.
	 * 
	 * @param filePath
	 *            the path to the file that is requested
	 * @param directory
	 *            whether the file is a directory of a file
	 * @return
	 */
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

	protected static boolean deleteFile(File file, int maxRetryCount, int retryCount) {
		if (file == null || !file.exists()) {
			return Boolean.FALSE;
		}
		if (file.isDirectory()) {
			File children[] = file.listFiles();
			for (int j = 0; j < children.length; j++) {
				File child = children[j];
				deleteFile(child, maxRetryCount, 0);
			}
		}
		if (file.delete()) {
			return Boolean.TRUE;
		}
		if (retryCount >= maxRetryCount) {
			if (file.exists()) {
				LOGGER.debug("Couldn't delete file : " + file);
				LOGGER.debug("Will try to delete on exit : ");
				file.deleteOnExit();
			}
			return Boolean.FALSE;
		} else {
			LOGGER.debug("Retrying count : " + retryCount + ", file : " + file);
			return deleteFile(file, maxRetryCount, ++retryCount);
		}
	}

	public static synchronized File getLatestIndexDirectory(String baseIndexDirectoryPath) {
		try {
			File baseIndexDirectory = FileUtilities.getFile(baseIndexDirectoryPath, Boolean.TRUE);
			LOGGER.debug("Base index directory : " + baseIndexDirectory);
			return getLatestIndexDirectory(baseIndexDirectory, null);
		} finally {
			FileUtilities.class.notifyAll();
		}
	}

	protected static synchronized File getLatestIndexDirectory(File file, File latestSoFar) {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			for (File child : children) {
				if (FileUtilities.isDigits(child.getName())) {
					if (latestSoFar == null) {
						latestSoFar = child;
					}
					latestSoFar = getNewestIndexDirectory(child, latestSoFar);
				} else {
					latestSoFar = getLatestIndexDirectory(child, latestSoFar);
				}
			}
		}
		return latestSoFar;
	}

	protected static synchronized File getNewestIndexDirectory(File one, File two) {
		long oneTime = Long.parseLong(one.getName());
		long twoTime = Long.parseLong(two.getName());
		return oneTime > twoTime ? one : two;
	}

	/**
	 * Verifies that all the characters in a string are digits, ie. the string is a number.
	 * 
	 * @param string
	 *            the string to verify for digit data
	 * @return whether every character in a string is a digit
	 */
	public static boolean isDigits(String string) {
		if (string == null || string.trim().equals("")) {
			return false;
		}
		char[] chars = string.toCharArray();
		for (char c : chars) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
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
	 */
	public static ByteArrayOutputStream getContents(InputStream inputStream, long maxLength) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		if (inputStream == null) {
			return byteArrayOutputStream;
		}
		try {
			byte[] bytes = new byte[1024];
			int read;
			while ((read = inputStream.read(bytes)) > -1 && byteArrayOutputStream.size() < maxLength) {
				byteArrayOutputStream.write(bytes, 0, read);
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
		return byteArrayOutputStream;
	}

	/**
	 * Reads the contents of the file and returns the contents in a byte array form.
	 * 
	 * @param inputStream
	 *            the file to read the contents from
	 * @param outputStream
	 *            the output stream to write the data to
	 * @param maxLength
	 *            the maximum number of bytes to read into the buffer
	 */
	public static void getContents(InputStream inputStream, OutputStream outputStream, long maxLength) {
		if (inputStream == null) {
			return;
		}
		try {
			int read = -1;
			int total = 0;
			byte[] bytes = new byte[1024];
			while ((read = inputStream.read(bytes)) > -1 && total < maxLength) {
				total += read;
				outputStream.write(bytes, 0, read);
			}
		} catch (Exception e) {
			LOGGER.error("Exception accessing the stream contents.", e);
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
				LOGGER.error("Exception closing input stream " + inputStream, e);
			}
		}
	}

	public static void getContents(Reader reader, OutputStream outputStream, long maxLength) {
		if (reader == null) {
			return;
		}
		try {
			int read = -1;
			int total = 0;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) > -1 && total < maxLength) {
				total += read;
				byte[] bytes = new byte[read];
				for (int i = 0; i < read; i++) {
					bytes[i] = (byte) chars[i];
				}
				outputStream.write(bytes, 0, bytes.length);
			}
		} catch (Exception e) {
			LOGGER.error("Exception accessing the file contents.", e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				LOGGER.error("Exception closing input stream " + reader, e);
			}
		}
	}

}
