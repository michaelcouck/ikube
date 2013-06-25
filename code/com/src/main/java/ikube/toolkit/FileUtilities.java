package ikube.toolkit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public final class FileUtilities {

	private static final Logger LOGGER = Logger.getLogger(FileUtilities.class);

	/**
	 * Singularity.
	 */
	private FileUtilities() {
		// Documented
	}

	/**
	 * This method looks through all the files defined in the folder in the parameter list, recursively, and gets the first one that matches the pattern.
	 * 
	 * @param folder the folder to start looking through
	 * @param stringPatterns the patterns to look for in the file paths
	 * @return the first file that was encountered that has the specified pattern(s) in it
	 */
	public static final File findFileRecursively(final File folder, final String... stringPatterns) {
		List<File> files = findFilesRecursively(folder, new ArrayList<File>(), stringPatterns);
		return !files.isEmpty() ? files.get(0) : null;
	}

	/**
	 * This method will recursively look for a directory in the file system starting at the specified abstract file position and return the first one that is
	 * encountered.
	 * 
	 * @param folder the folder to start looking for the patterns
	 * @param stringPatterns the patterns of the folder to look for
	 * @return the first folder that satisfies the patterns specified
	 */
	public static final File findDirectoryRecursively(final File folder, final String... stringPatterns) {
		List<File> files = findFilesRecursively(folder, new ArrayList<File>(), stringPatterns);
		Iterator<File> fileIterator = files.iterator();
		while (fileIterator.hasNext()) {
			if (!fileIterator.next().isDirectory()) {
				fileIterator.remove();
			}
		}
		return !files.isEmpty() ? files.get(0) : null;
	}

	/**
	 * This method looks through all the files defined in the folder in the parameter list, recursively, and gets the first one that matches the pattern.
	 * 
	 * @param folder the folder to start looking through
	 * @param stringPatterns the patterns to look for in the file paths
	 * @param upDirectories the number of directories to go up before starting the search, i.e. the parent and grandparent directories
	 * @return the first file that was encountered that has the specified pattern(s) in it
	 */
	public static final File findFileRecursively(final File folder, final int upDirectories, final String... stringPatterns) {
		int directories = upDirectories;
		File upFolder = folder;
		do {
			upFolder = upFolder.getParentFile();
		} while (--directories > 0 && upFolder != null);
		List<File> files = findFilesRecursively(upFolder, new ArrayList<File>(), stringPatterns);
		return !files.isEmpty() ? files.get(0) : null;
	}

	/**
	 * This method will look through all the files in the top level folder, and all the sub folders, adding files to the list when they match the patterns that
	 * are provided.
	 * 
	 * @param folder the folder to start looking through
	 * @param stringPatterns the patterns to match the file paths with
	 * @param files the files list to add all the files to
	 * @return the list of files that match the patterns
	 */
	public static final List<File> findFilesRecursively(final File folder, final List<File> files, final String... stringPatterns) {
		if (folder != null && folder.isDirectory()) {
			File[] folderFiles = findFiles(folder, stringPatterns);
			if (folderFiles != null) {
				files.addAll(Arrays.asList(folderFiles));
				File[] childFolders = folder.listFiles();
				for (File childFolder : childFolders) {
					findFilesRecursively(childFolder, files, stringPatterns);
				}
			}
		}
		return files;
	}

	/**
	 * Finds files with the specified pattern only in the folder specified in the parameter list, i.e. not recursively.
	 * 
	 * @param folder the folder to look for files in
	 * @param stringPatterns the pattern to look for in the file path
	 * @return an array of files with the specified pattern in the path
	 */
	public static final File[] findFiles(final File folder, final String... stringPatterns) {
		final Pattern pattern = getPattern(stringPatterns);
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File file) {
				String pathName = file.getAbsolutePath();
				boolean match = pattern.matcher(pathName).matches();
				return match;
			}
		});
		return files;
	}

	/**
	 * Gets a single file. First looking to find it, if it can not be found then it is created.
	 * 
	 * @param filePath the path to the file that is requested
	 * @return the found or newly created {@link File} or <code>null</code> if something went wrong.
	 */
	public static final synchronized File getOrCreateFile(final String filePath) {
		return (filePath == null) ? null : getOrCreateFile(new File(filePath));
	}

	/**
	 * Gets a single directory. First looking to find it, if it can not be found then it is created.
	 * 
	 * @param filePath the path to the directory that is requested
	 * @return the found or newly created {@link File} or <code>null</code> if something went wrong.
	 */
	public static final synchronized File getOrCreateDirectory(final String filePath) {
		return (filePath == null) ? null : getOrCreateDirectory(new File(filePath));
	}

	/**
	 * Gets a single file. First looking to find it, if it can not be found then it is created.
	 * 
	 * @param file the file that is requested
	 * @return the found or newly created {@link File} or <code>null</code> if something went wrong.
	 */
	public static final synchronized File getOrCreateFile(final File file) {
		try {
			if (file.exists() && file.isFile()) {
				return file;
			}
			File parent = file.getParentFile();
			parent = getOrCreateDirectory(parent.getAbsolutePath());
			if (parent != null) {
				try {
					LOGGER.debug("creating file " + file.getAbsolutePath());
					boolean created = file.createNewFile();
					if (created && file.exists()) {
						return file;
					}
				} catch (IOException e) {
					LOGGER.error("Exception creating file : " + file, e);
				}
			}
			return null;
		} finally {
			FileUtilities.class.notifyAll();
		}
	}

	/**
	 * Gets a single directory. First looking to find it, if it can not be found then it is created.
	 * 
	 * @param file the directory that is requested
	 * @return the found or newly created {@link File} or <code>null</code> if something went wrong.
	 */
	public static final synchronized File getOrCreateDirectory(final File file) {
		try {
			if (file.exists() && file.isDirectory()) {
				return file;
			}
			LOGGER.debug("creating directory " + file.getAbsolutePath());
			boolean created = file.mkdirs();
			if (created && file.exists()) {
				return file;
			}
			return null;
		} finally {
			FileUtilities.class.notifyAll();
		}
	}

	/**
	 * Gets all the content from the file and puts it into a string, assuming the default encoding for the platform and file contents are in fact strings.
	 * 
	 * @param file the file to read into a string
	 * @return the contents of the file or null if there was an exception reading the file
	 */
	public static final String getContent(final File file) {
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			byte[] bytes = new byte[(int) file.length()];
			fileInputStream.read(bytes);
			fileInputStream.close();
			return new String(bytes);
		} catch (Exception e) {
			LOGGER.error("Exception creating file : " + file, e);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (Exception e) {
					LOGGER.error("Exception closing the file stream : " + file, e);
				}
			}
		}
		return null;
	}

	/**
	 * Deletes all files recursively, that have the specified pattern in the path. Note that this is dangerous and you really need to know what files are in the
	 * directory that you feed this method. There is no turning back, these files will be completely deleted, no re-cycle bin and all that.
	 * 
	 * @param file the top level directory or file to start looking into
	 * @param stringPatterns the patterns to look for in the file paths
	 */
	public static void deleteFiles(final File file, final String... stringPatterns) {
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
	 * @param stringPatterns the regular expression patterns
	 * @return the pattern generated from the strings
	 */
	public static Pattern getPattern(final String... stringPatterns) {
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
		return Pattern.compile(builder.toString());
	}
	
	public static boolean deleteFile(final File file) {
		return deleteFile(file, 1);
	}

	/**
	 * Deletes the file/folder recursively. If the file cannot be deleted then the file is set to delete on exit of the JVM, which doesn't generally work of
	 * course, but we try anyway.
	 * 
	 * @param file the file/folder to delete
	 * @param maxRetryCount the number of times to re-try the delete operation
	 */
	public static boolean deleteFile(final File file, final int maxRetryCount) {
		return FileUtilities.deleteFile(file, maxRetryCount, 0);
	}

	protected static void makeReadWrite(final File file) {
		file.setReadable(true, false);
		file.setWritable(true, false);
	}

	protected static boolean deleteFile(final File file, final int maxRetryCount, final int retryCount) {
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
		}
		LOGGER.debug("Retrying count : " + retryCount + ", file : " + file);
		return deleteFile(file, maxRetryCount, retryCount + 1);
	}

	/**
	 * Writes the contents of a byte array to a file.
	 * 
	 * @param file the file to write to
	 * @param bytes the byte data to write
	 */
	public static void setContents(final String filePath, final byte[] bytes) {
		File file = FileUtilities.getFile(filePath, Boolean.FALSE);
		setContents(file, bytes);
	}

	/**
	 * Gets a single file. First looking to find it, if it can not be found then it is created.
	 * 
	 * @param filePath the path to the file that is requested
	 * @param directory whether the file is a directory of a file
	 * @return
	 */
	public static synchronized File getFile(final String filePath, final boolean directory) {
		if (filePath == null) {
			return null;
		}
		File file = null;
		try {
			file = new File(filePath);
			if (directory) {
				file = getOrCreateDirectory(file);
				if (file.exists()) {
					makeReadWrite(file);
				} else {
					LOGGER.warn("Didn't create directory/file : " + file);
				}
			} else {
				file = getOrCreateFile(file);
				if (!file.exists()) {
					makeReadWrite(file);
				} else {
					LOGGER.warn("Didn't create directory/file : " + file);
				}
			}
			return file;
		} finally {
			FileUtilities.class.notifyAll();
		}
	}

	/**
	 * Writes the contents of a byte array to the file.
	 * 
	 * @param outputFile the file to write to
	 * @param bytes the data to write to the file
	 */
	public static final void setContents(final File outputFile, final byte[] bytes) {
		FileOutputStream fileOutputStream = null;
		try {
			outputFile.setReadable(true);
			outputFile.setWritable(true, false);
			fileOutputStream = new FileOutputStream(outputFile);
			fileOutputStream.write(bytes, 0, bytes.length);
		} catch (FileNotFoundException e) {
			LOGGER.error("File " + outputFile + " not found", e);
		} catch (IOException e) {
			LOGGER.error("IO exception writing file contents", e);
		} finally {
			close(fileOutputStream);
		}
	}

	/**
	 * Reads the contents of the file and returns the contents in a byte array form.
	 * 
	 * @param file the file to read the contents from
	 * @return the file contents in a byte array output stream
	 */
	public static ByteArrayOutputStream getContents(final File file, final int maxReadLength) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			LOGGER.error("No file by that name : " + file, e);
		} catch (Exception e) {
			LOGGER.error("General error accessing the file : " + file, e);
		}
		return getContents(inputStream, maxReadLength);
	}

	public static String getContents(final File file, String encoding) {
		StringBuilder builder = new StringBuilder();
		Reader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(file), encoding);
			int read = -1;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) > -1) {
				builder.append(new String(chars, 0, read));
			}
		} catch (Exception e) {
			LOGGER.error("Exception reading stream : " + reader, e);
		} finally {
			close(reader);
		}
		return builder.toString();
	}

	/**
	 * Reads the contents of the file and returns the contents in a byte array form.
	 * 
	 * @param inputStream the file to read the contents from
	 * @param maxLength the maximum number of bytes to read into the buffer
	 * @return the file contents in a byte array output stream
	 */
	public static ByteArrayOutputStream getContents(final InputStream inputStream, final long maxLength) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		if (inputStream == null) {
			return byteArrayOutputStream;
		}
		try {
			byte[] bytes = new byte[1024];
			int read = inputStream.read(bytes);
			while (read > -1 && byteArrayOutputStream.size() < maxLength) {
				byteArrayOutputStream.write(bytes, 0, read);
				read = inputStream.read(bytes);
			}
		} catch (Exception e) {
			LOGGER.error("Exception accessing the file contents : " + inputStream, e);
		} finally {
			close(inputStream);
		}
		return byteArrayOutputStream;
	}

	/**
	 * Reads the contents of the stream and returns the contents in a byte array form.
	 * 
	 * @param inputStream the file to read the contents from
	 * @param outputStream the output stream to write the data to
	 * @param maxLength the maximum number of bytes to read into the buffer
	 */
	public static void getContents(final InputStream inputStream, final OutputStream outputStream, final long maxLength) {
		if (inputStream == null) {
			return;
		}
		try {
			int total = 0;
			byte[] bytes = new byte[1024];
			int read = inputStream.read(bytes);
			while (read > -1 && total < maxLength) {
				total += read;
				outputStream.write(bytes, 0, read);
				read = inputStream.read(bytes);
			}
		} catch (Exception e) {
			LOGGER.error("Exception accessing the stream contents.", e);
		} finally {
			close(inputStream);
		}
	}

	public static void getContents(final Reader reader, final OutputStream outputStream, final long maxLength) {
		if (reader == null) {
			return;
		}
		try {
			int total = 0;
			char[] chars = new char[1024];
			int read = reader.read(chars);
			while (read > -1 && total < maxLength) {
				total += read;
				byte[] bytes = new byte[read];
				for (int i = 0; i < read; i++) {
					bytes[i] = (byte) chars[i];
				}
				outputStream.write(bytes, 0, bytes.length);
				read = reader.read(chars);
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
	
	public static void close(Reader reader) {
		if (reader != null) {
			try {
				reader.close();
			} catch (Exception e) {
				LOGGER.error("Exception closing the reader : " + reader, e);
			}
		}
	}

	public static void close(Writer writer) {
		if (writer != null) {
			try {
				writer.flush();
			} catch (Exception e) {
				LOGGER.error("Exception closing the writer : " + writer, e);
			}
			try {
				writer.close();
			} catch (Exception e) {
				LOGGER.error("Exception closing the writer : " + writer, e);
			}
		}
	}

	public static void close(InputStream inputStream) {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (Exception e) {
			LOGGER.error("Exception closing stream : " + inputStream, e);
		}
	}

	public static void close(OutputStream outputStream) {
		if (outputStream != null) {
			try {
				outputStream.flush();
			} catch (Exception e) {
				LOGGER.error("Exception flushing stream : " + outputStream, e);
			}
			try {
				outputStream.close();
			} catch (Exception e) {
				LOGGER.error("Exception closing stream : " + outputStream, e);
			}
		}
	}

	public static String cleanFilePath(final String path) {
		String indexDirectoryPath = StringUtils.replace(path, "/./", "/");
		indexDirectoryPath = StringUtils.replace(indexDirectoryPath, "\\.\\", "/");
		indexDirectoryPath = StringUtils.replace(indexDirectoryPath, "\\", "/");
		return indexDirectoryPath;
	}

}