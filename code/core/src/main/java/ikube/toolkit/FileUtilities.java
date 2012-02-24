package ikube.toolkit;

import ikube.IConstants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
	 * Deletes all files recursively, that have the specified pattern in the path. Note that this is dangerous and you really need to know
	 * what files are in the directory that you feed this method. There is no turning back, these files will be completely deleted, no
	 * re-cycle bin and all that.
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
			// Concatenate the 'any character' regular expression to the string
			// pattern
			builder.append(".*(").append(stringPattern).append(").*");
		}
		return Pattern.compile(builder.toString());
	}

	/**
	 * Finds files with the specified pattern only in the folder specified in the parameter list, i.e. not recursively.
	 * 
	 * @param folder the folder to look for files in
	 * @param stringPatterns the pattern to look for in the file path
	 * @return an array of files with the specified pattern in the path
	 */
	public static File[] findFiles(final File folder, final String[] stringPatterns) {
		final Pattern pattern = getPattern(stringPatterns);
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File file) {
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
	 * @param folder the folder to start looking through
	 * @param stringPatterns the patterns to look for in the file paths
	 * @return the first file that was encountered that has the specified pattern(s) in it
	 */
	public static File findFileRecursively(final File folder, final String... stringPatterns) {
		List<File> files = FileUtilities.findFilesRecursively(folder, new ArrayList<File>(), stringPatterns);
		return !files.isEmpty() ? files.get(0) : null;
	}

	public static File findFileRecursively(final File folder, final boolean directory, final String... stringPatterns) {
		List<File> files = FileUtilities.findFilesRecursively(folder, new ArrayList<File>(), stringPatterns);
		for (File file : files) {
			if (directory) {
				if (file.isDirectory()) {
					return file;
				}
			} else {
				if (file.isFile()) {
					return file;
				}
			}
		}
		return null;
	}

	/**
	 * This method will look through all the files in the top level folder, and all the sub folders, adding files to the list when they
	 * match the patterns that are provided.
	 * 
	 * @param folder the folder to start looking through
	 * @param stringPatterns the patterns to match the file paths with
	 * @param files the files list to add all the files to
	 * @return the list of files that match the patterns
	 */
	public static List<File> findFilesRecursively(final File folder, final List<File> files, final String... stringPatterns) {
		if (folder.isDirectory()) {
			File[] folderFiles = FileUtilities.findFiles(folder, stringPatterns);
			files.addAll(Arrays.asList(folderFiles));
			File[] childFolders = folder.listFiles();
			for (File childFolder : childFolders) {
				findFilesRecursively(childFolder, files, stringPatterns);
			}
		}
		return files;
	}

	/**
	 * Deletes the file/folder recursively. If the file cannot be deleted then the file is set to delete on exit of the JVM, which doesn't
	 * generally work of course, but we try anyway.
	 * 
	 * @param file the file/folder to delete
	 * @param maxRetryCount the number of times to re-try the delete operation
	 */
	public static boolean deleteFile(final File file, final int maxRetryCount) {
		return FileUtilities.deleteFile(file, maxRetryCount, 0);
	}

	/**
	 * Gets a single file. First looking to find it, if it can not be found then it is created.
	 * 
	 * @param filePath the path to the file that is requested
	 * @param directory whether the file is a directory of a file
	 * @return
	 */
	public static synchronized File getFile(final String filePath, final boolean directory) {
		try {
			if (filePath == null) {
				return null;
			}
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
	 * This method gets the latest index directory. Index directories are defined by:<br>
	 * 
	 * 1) The path to the index on the file system<br>
	 * 2) The name of the index<br>
	 * 3) The time(as a long) that the index was created 4) The ip address of the server that created the index<br>
	 * 
	 * The result of this is something like ./indexes/ikube/123456789/127.0.0.1. This method will return the directory
	 * ./indexes/ikube/123456789. In other words the timestamp directory, not the individual server index directories.
	 * 
	 * @param baseIndexDirectoryPath the base path to the indexes, i.e. the ./indexes part
	 * @return the latest time stamped directory at this path, in other words the ./indexes/ikube/123456789 directory. Note that there is no
	 *         Lucene index at this path, the Lucene index is still in the server ip address directory in this time stamp directory, i.e. at
	 *         ./indexes/ikube/123456789/127.0.0.1
	 */
	public static synchronized File getLatestIndexDirectory(final String baseIndexDirectoryPath) {
		try {
			File baseIndexDirectory = FileUtilities.getFile(baseIndexDirectoryPath, Boolean.TRUE);
			LOGGER.debug("Base index directory : " + baseIndexDirectory);
			return getLatestIndexDirectory(baseIndexDirectory, null);
		} finally {
			FileUtilities.class.notifyAll();
		}
	}

	protected static synchronized File getLatestIndexDirectory(final File file, final File latestSoFar) {
		if (file == null) {
			return latestSoFar;
		}
		File latest = latestSoFar;
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			for (File child : children) {
				if (FileUtilities.isDigits(child.getName())) {
					if (latest == null) {
						latest = child;
					}
					long oneTime = Long.parseLong(child.getName());
					long twoTime = Long.parseLong(latest.getName());
					latest = oneTime > twoTime ? child : latest;
				} else {
					latest = getLatestIndexDirectory(child, latest);
				}
			}
		}
		return latest;
	}

	/**
	 * Verifies that all the characters in a string are digits, ie. the string is a number.
	 * 
	 * @param string the string to verify for digit data
	 * @return whether every character in a string is a digit
	 */
	public static boolean isDigits(final String string) {
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
	 * @param file the file to write to
	 * @param bytes the byte data to write
	 */
	public static void setContents(final String filePath, final byte[] bytes) {
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
			close(fileOutputStream);
		}
	}

	/**
	 * Writes the contents of a byte array to a file.
	 * 
	 * @param file the file to write to
	 * @param bytes the byte data to write
	 */
	public static void setContents(final String filePath, final String string) {
		File file = FileUtilities.getFile(filePath, Boolean.FALSE);
		FileOutputStream fileOutputStream = null;
		OutputStreamWriter outputStreamWriter = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			outputStreamWriter = new OutputStreamWriter(fileOutputStream, IConstants.ENCODING);
			outputStreamWriter.write(string);
		} catch (FileNotFoundException e) {
			LOGGER.error("File " + file + " not found", e);
		} catch (IOException e) {
			LOGGER.error("IO exception writing file contents", e);
		} finally {
			close(fileOutputStream);
			close(outputStreamWriter);
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
	 * This method will read the contents of a file from the end, reading the number of bytes specified in the parameter list.
	 * 
	 * @param file the file to read from the end
	 * @param bytesToRead the number of bytes to read
	 * @return the byte array with the bytes read, could be empty if there is no data in the file or if there is an exception
	 */
	public static ByteArrayOutputStream getContentsFromEnd(final File file, final long bytesToRead) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		FileInputStream fileInputStream = null;
		try {
			long length = file.length();
			fileInputStream = new FileInputStream(file);
			ByteBuffer byteBuffer = ByteBuffer.allocate((int) bytesToRead);
			long position = length - bytesToRead;
			if (position < 0) {
				position = 0;
			}
			fileInputStream.getChannel().read(byteBuffer, position);
			byteArrayOutputStream.write(byteBuffer.array());
		} catch (Exception e) {
			LOGGER.error("Exception reading from the end of the file : ", e);
		} finally {
			close(fileInputStream);
		}
		return byteArrayOutputStream;
	}

	/**
	 * Reads the contents of the file and returns the contents in a byte array form.
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

	/**
	 * This function will copy files or directories from one location to another. note that the source and the destination must be mutually
	 * exclusive. This function can not be used to copy a directory to a sub directory of itself. The function will also have problems if
	 * the destination files already exist.
	 * 
	 * @param src A File object that represents the source for the copy
	 * @param dest A File object that represents the destination for the copy.
	 */
	public static void copyFiles(File src, File dest, String... patterns) {
		// Check to ensure that the source is valid...
		if (src == null || !src.exists()) {
			LOGGER.warn("Source file/directory does not exist : " + src);
			return;
		} else if (!src.canRead()) { // check to ensure we have rights to the source...
			LOGGER.warn("Source file/directory not readable : " + src);
			return;
		}
		Pattern pattern = getPattern(patterns);
		if (pattern.matcher(src.getAbsolutePath()).matches()) {
			LOGGER.info("Not copying file : " + src);
			return;
		}
		// is this a directory copy?
		if (src.isDirectory()) {
			if (!dest.exists()) { // does the destination already exist?
				// if not we need to make it exist if possible (note this is
				// mkdirs not mkdir)
				if (!dest.mkdirs()) {
					LOGGER.warn("Could not create the new destination directory : " + dest);
				} else {
					LOGGER.info("Created directory : " + dest);
				}
			}
			// get a listing of files...
			String children[] = src.list();
			// copy all the files in the list.
			for (int i = 0; i < children.length; i++) {
				File childSrc = new File(src, children[i]);
				File childDest = new File(dest, children[i]);
				copyFiles(childSrc, childDest, patterns);
			}
		} else {
			// This was not a directory, so lets just copy the file
			copyFile(src, dest);
		}
	}

	public static void copyFile(File in, File out) {
		if (!in.exists() || !in.canRead()) {
			LOGGER.warn("Can't copy file : " + in);
			return;
		}
		if (!out.getParentFile().exists()) {
			if (!out.getParentFile().mkdirs()) {
				LOGGER.info("Didn't create parent directories : " + out.getParentFile().getAbsolutePath());
			}
		}
		if (!out.exists()) {
			try {
				out.createNewFile();
			} catch (IOException e) {
				LOGGER.error("Exception creating new file : " + out.getAbsolutePath(), e);
			}
		}
		LOGGER.debug("Copying file : " + in + ", to : " + out);
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		FileInputStream fileInputStream = null;
		FileOutputStream fileOutputStream = null;
		try {
			fileInputStream = new FileInputStream(in);
			inChannel = fileInputStream.getChannel();
			fileOutputStream = new FileOutputStream(out);
			outChannel = fileOutputStream.getChannel();
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (Exception e) {
			LOGGER.error("Exception copying file : " + in + ", to : " + out, e);
		} finally {
			close(fileInputStream);
			close(fileOutputStream);
			if (inChannel != null) {
				try {
					inChannel.close();
				} catch (Exception e) {
					LOGGER.error("Exception closing input channel : ", e);
				}
			}
			if (outChannel != null) {
				try {
					outChannel.close();
				} catch (Exception e) {
					LOGGER.error("Exception closing output channel : ", e);
				}
			}
		}
	}

	private static final int BUFFER = 2048;

	/**
	 * This method unzips a file to a folder in the user directory with the name of the file.
	 * 
	 * @param zipFile the zip file to unpack, this file name sill be used for the folder to unzip to
	 * @return the folder where the file was unpacked to
	 */
	public static File unzip(String zipFile, String toDir) {
		File file;
		ZipFile zip;
		try {
			file = new File(zipFile);
			zip = new ZipFile(file);
		} catch (Exception e) {
			LOGGER.error("Exception opening zip file : " + zipFile, e);
			return null;
		}
		File destinationFolder = null;
		try {
			Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
			// Process each entry
			destinationFolder = FileUtilities.getFile(toDir + IConstants.SEP + file.getName(), Boolean.TRUE);
			while (zipFileEntries.hasMoreElements()) {
				// Grab a zip file entry
				ZipEntry entry = zipFileEntries.nextElement();
				if (entry.isDirectory()) {
					continue;
				}
				String destinationPath = destinationFolder.getAbsolutePath() + IConstants.SEP + entry.getName();
				File destinationFile = FileUtilities.getFile(destinationPath, Boolean.FALSE);
				// LOGGER.info("Unzipped file : " +
				// destinationFile.getAbsolutePath());
				InputStream inputStream = null;
				OutputStream outputStream = null;
				OutputStream destinationOutputStream = null;
				try {
					if (!entry.isDirectory()) {
						LOGGER.info("Unzipping entry : " + entry);
						inputStream = new BufferedInputStream(zip.getInputStream(entry));
						int currentByte;
						// establish buffer for writing file
						byte data[] = new byte[BUFFER];
						// write the current file to disk
						outputStream = new FileOutputStream(destinationFile);
						destinationOutputStream = new BufferedOutputStream(outputStream, BUFFER);
						// read and write until last byte is encountered
						while ((currentByte = inputStream.read(data, 0, BUFFER)) != -1) {
							destinationOutputStream.write(data, 0, currentByte);
						}
					}
				} catch (Exception e) {
					LOGGER.error("Exception extracting zip entry : " + entry, e);
				} finally {
					close(inputStream);
					close(outputStream);
					close(destinationOutputStream);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception extracting zip file : " + zipFile + ", " + zip, e);
		} finally {
			try {
				zip.close();
			} catch (Exception e) {
				LOGGER.error("Exception closing zip file : " + zip, e);
			}
		}
		return destinationFolder;
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
		try {
			if (outputStream != null) {
				outputStream.close();
			}
		} catch (Exception e) {
			LOGGER.error("Exception closing stream : " + outputStream, e);
		}
	}

}