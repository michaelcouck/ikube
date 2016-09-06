package ikube.toolkit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class contains common utilities for files like deleting directories recursively, creating
 * files and directories and searching for files and directories recursively, before the {@link org.apache.commons.io.FileUtils}
 * from Apache was available.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 25-01-2011
 */
public final class FILE {

    private static final Logger LOGGER = LoggerFactory.getLogger(FILE.class);

    /**
     * This method looks through all the files defined in the folder in the parameter
     * list, recursively, and gets the first one that matches the pattern.
     *
     * @param folder         the folder to start looking through
     * @param stringPatterns the patterns to look for in the file paths
     * @return the first file that was encountered that has the specified pattern(s) in it
     */
    public static File findFileRecursively(final File folder, final String... stringPatterns) {
        List<File> files = findFilesRecursively(folder, new ArrayList<File>(), stringPatterns);
        return !files.isEmpty() ? files.get(0) : null;
    }

    /**
     * This method will recursively look for a directory in the file system starting at
     * the specified abstract file position and return the first one that is encountered.
     *
     * @param folder         the folder to start looking for the patterns
     * @param stringPatterns the patterns of the folder to look for
     * @return the first folder that satisfies the patterns specified
     */
    public static File findDirectoryRecursively(final File folder, final String... stringPatterns) {
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
     * This method looks through all the files defined in the folder in the
     * parameter list, recursively, and gets the first one that matches the pattern.
     *
     * @param folder         the folder to start looking through
     * @param stringPatterns the patterns to look for in the file paths
     * @param upDirectories  the number of directories to go up before starting the search,
     *                       i.e. the parent and grandparent directories
     * @return the first file that was encountered that has the specified pattern(s) in it
     */
    public static File findFileRecursively(final File folder, final int upDirectories, final String... stringPatterns) {
        File upFolder = moveUpDirectories(folder, upDirectories);
        List<File> files = findFilesRecursively(upFolder, new ArrayList<File>(), stringPatterns);
        return !files.isEmpty() ? files.get(0) : null;
    }

    /**
     * This method will first walk backwards through the directories before doing a
     * search for the directory pattern specified.
     *
     * @param folder         the folder to start looking through
     * @param upDirectories  the number of directories to go up before searching
     * @param stringPatterns the patterns to look for in the file paths
     * @return the directory that matches the pattern startup from a higher directory
     */
    static File findDirectoryRecursively(final File folder, final int upDirectories, final String... stringPatterns) {
        File upFolder = moveUpDirectories(folder, upDirectories);
        return findDirectoryRecursively(upFolder, stringPatterns);
    }

    /**
     * This method will first walk backwards through the directories, looking for a parent with a
     * specific name, before doing a search for the directory/file pattern specified.
     *
     * @param folder         the folder to start looking through
     * @param toDirectory    the name of the parent directory to start looking for the file,
     *                       i.e. move up to this directory before starting to search
     * @param stringPatterns the patterns to look for in the file paths
     * @return the directory that matches the pattern startup from a higher directory
     */
    @SuppressWarnings("unused")
    public static File findDirectoryRecursivelyUp(final File folder, final String toDirectory, final String... stringPatterns) {
        File upFolder = moveUpDirectories(folder, toDirectory);
        return findDirectoryRecursively(upFolder, stringPatterns);
    }

    public static File moveUpDirectories(final File folder, final int upDirectories) {
        if (upDirectories == 0) {
            return folder;
        }
        int directories = upDirectories;
        String upFolderPath = cleanFilePath(folder.getAbsolutePath());
        File upFolder = new File(upFolderPath);
        do {
            upFolder = upFolder.getParentFile();
        } while (--directories > 0 && upFolder != null);
        return upFolder;
    }

    static File moveUpDirectories(final File folder, final String toFolder) {
        if (folder.getName().equals(toFolder)) {
            return folder;
        }
        File upFolder = moveUpDirectories(folder, 1);
        return moveUpDirectories(upFolder, toFolder);
    }

    /**
     * This method returns the relative file/folder, parent or grandparent folder of the file specified.
     *
     * @param file the file to get the relative parent from
     * @param path the path relative to the current file. This typically contains ../../ segments, and for each segment that
     *             is encountered we will move one directory higher
     * @return the relative path
     */
    public static File relative(final File file, final String path) {
        int upDirectories = 0;
        String strippedPath = path;
        while (strippedPath.contains("../")) {
            strippedPath = strippedPath.replaceFirst("../", "");
            upDirectories++;
        }
        File baseFolder = moveUpDirectories(file, upDirectories);
        return new File(baseFolder, strippedPath);
    }

    /**
     * This method will look through all the files in the top level folder, and all
     * the sub folders, adding files to the list when they match the patterns that are provided.
     *
     * @param folder         the folder to start looking through
     * @param stringPatterns the patterns to match the file paths with
     * @param files          the files list to add all the files to
     * @return the list of files that match the patterns
     */
    public static List<File> findFilesRecursively(final File folder, final List<File> files, final String... stringPatterns) {
        if (folder != null && folder.isDirectory()) {
            File[] folderFiles = findFiles(folder, stringPatterns);
            if (folderFiles != null) {
                files.addAll(Arrays.asList(folderFiles));
            }
            File[] childFolders = folder.listFiles();
            if (childFolders != null) {
                for (final File childFolder : childFolders) {
                    findFilesRecursively(childFolder, files, stringPatterns);
                }
            }
        }
        return files;
    }

    /**
     * This method, similar to the {@link FILE#findFilesRecursively(File, List, String...)}
     * will just move up a few directories before searching for
     * the patterns.
     *
     * @param folder         the start folder
     * @param upDirectories  how many directories to move up before searching
     * @param files          the list of files that will collect the files found
     * @param stringPatterns the patterns to search for in the directories
     * @return the list of files found for the patterns in the directories
     */
    static List<File> findFilesRecursively(final File folder, final int upDirectories, final List<File> files, final String... stringPatterns) {
        File startFolder = moveUpDirectories(folder, upDirectories);
        return findFilesRecursively(startFolder, files, stringPatterns);
    }

    /**
     * Finds files with the specified pattern only in the folder specified in the parameter list,
     * i.e. not recursively.
     *
     * @param folder         the folder to look for files in
     * @param stringPatterns the pattern to look for in the file path
     * @return an array of files with the specified pattern in the path
     */
    static File[] findFiles(final File folder, final String... stringPatterns) {
        final Pattern pattern = getPattern(stringPatterns);
        return folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File file) {
                String pathName = file.getAbsolutePath();
                return pattern.matcher(pathName).matches();
            }
        });
    }

    /**
     * Gets a single file. First looking to find it, if it can not be found then it is created.
     *
     * @param filePath the path to the file that is requested
     * @return the found or newly created {@link File} or <code>null</code> if something went wrong.
     */
    public static synchronized File getOrCreateFile(final String filePath) {
        return (filePath == null) ? null : getOrCreateFile(new File(filePath));
    }

    /**
     * Gets a single directory. First looking to find it, if it can not be found then it is created.
     *
     * @param filePath the path to the directory that is requested
     * @return the found or newly created {@link File} or <code>null</code> if something went wrong.
     */
    public static synchronized File getOrCreateDirectory(final String filePath) {
        return (filePath == null) ? null : getOrCreateDirectory(new File(filePath));
    }

    /**
     * Gets a single file. First looking to find it, if it can not be found then it is created.
     *
     * @param file the file that is requested
     * @return the found or newly created {@link File} or <code>null</code> if something went wrong.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static synchronized File getOrCreateFile(final File file) {
        try {
            if (file.exists() && file.isFile()) {
                return file;
            }
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.exists()) {
                    parent = getOrCreateDirectory(parent);
                }
            }
            if (parent != null) {
                try {
                    String parentPath = cleanFilePath(parent.getAbsolutePath());
                    File createdFile = new File(parentPath, file.getName());
                    LOGGER.debug("Creating file : " + file.getAbsolutePath());
                    createdFile.createNewFile();
                    return createdFile;
                } catch (final IOException e) {
                    LOGGER.error("Exception creating file : " + file, e);
                }
            }
            return file;
        } finally {
            FILE.class.notifyAll();
        }
    }

    /**
     * Gets a single directory. First looking to find it, if it can not be found then it is created.
     *
     * @param directory the directory that is requested
     * @return the found or newly created {@link File} or <code>null</code> if something went wrong.
     */
    public static synchronized File getOrCreateDirectory(final File directory) {
        try {
            if (directory.exists() && directory.isDirectory()) {
                return directory;
            }
            String directoryPath = cleanFilePath(directory.getPath());
            LOGGER.debug("Creating directory : " + directoryPath);
            File createdDirectory = new File(directoryPath);
            boolean created = createdDirectory.mkdirs();
            if (!created || !directory.exists()) {
                LOGGER.warn("Couldn't create directory(ies) " + directory.getAbsolutePath());
            }
            return createdDirectory;
        } finally {
            FILE.class.notifyAll();
        }
    }

    /**
     * Gets all the content from the file and puts it into a string,
     * assuming the default encoding for the platform and file contents are in fact strings.
     *
     * @param file the file to read into a string
     * @return the contents of the file or null if there was an exception reading the file
     */
    public static String getContent(final File file) {
        // FileInputStream fileInputStream = null;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            // fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            int read = fileInputStream.read(bytes);
            return new String(bytes, 0, read);
        } catch (final Exception e) {
            LOGGER.error("Exception getting contents from file : " + file, e);
        }
        return "";
    }

    /**
     * This method will find the file(s) with the specified name patterns, iteratively through all the
     * directories specified, then get the contents of the file and return it.
     *
     * @param folder         the folder to start looking for the file
     * @param stringPatterns the name patterns of the file
     * @return the contents of the first file found that matches the pattern
     */
    static String findFileRecursivelyAndGetContents(final File folder, final String... stringPatterns) {
        return getContent(findFileRecursively(folder, stringPatterns));
    }

    /**
     * Deletes all files recursively, that have the specified pattern in the path. Note
     * that this is dangerous and you really need to know what files are in the directory that
     * you feed this method. There is no turning back, these files will be completely deleted, no
     * re-cycle bin and all that.
     *
     * @param file           the top level directory or file to start looking into
     * @param stringPatterns the patterns to look for in the file paths
     */
    public static void deleteFiles(final File file, final String... stringPatterns) {
        // If this is the 'dot' folder then there is probably something wrong, just return, not
        // doing this will result in all the files from the working directory being deleted, which is
        // almost 99.99999% of the time the desired result
        if (cleanFilePath(file.getPath()).equals(cleanFilePath(new File(".").getAbsolutePath()))) {
            LOGGER.warn("Not deleting dot folder : " + file.getAbsolutePath());
            return;
        }
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
            FILE.deleteFile(file, 1);
        }
    }

    /**
     * Creates the pattern object from the regular expression patterns.
     *
     * @param stringPatterns the regular expression patterns
     * @return the pattern generated from the strings
     */
    private static Pattern getPattern(final String... stringPatterns) {
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
     * Deletes the file/folder recursively. If the file cannot be deleted then the
     * file is set to delete on exit of the JVM, which doesn't generally work of course,
     * but we try anyway.
     *
     * @param file          the file/folder to delete
     * @param maxRetryCount the number of times to re-try the delete operation
     */
    public static boolean deleteFile(final File file, final int maxRetryCount) {
        if (file == null) {
            return Boolean.FALSE;
        }
        return deleteFile(file, maxRetryCount, 0);
    }

    @SuppressWarnings("WeakerAccess")
    public static void makeReadWrite(final File file) {
        boolean readable = file.setReadable(true, false);
        boolean writable = file.setWritable(true, false);
        if (!readable || !writable) {
            LOGGER.info("Didn't set file : " + file + ", readable : " + readable + ", writable : " + writable);
        }
    }

    protected static boolean deleteFile(final File file, final int maxRetryCount, final int retryCount) {
        if (file == null || !file.exists()) {
            return Boolean.FALSE;
        }
        if (file.isDirectory()) {
            File children[] = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteFile(child, maxRetryCount, 0);
                }
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
     * @param filePath the file path to write to
     * @param bytes    the byte data to write
     */
    public static void setContents(final String filePath, final byte[] bytes) {
        File file = getFile(filePath, Boolean.FALSE);
        setContents(file, bytes);
    }

    /**
     * Writes the contents of a byte array to a file.
     *
     * @param file        the file to write to
     * @param inputStream the byte data to write
     */
    public static void setContents(final File file, final InputStream inputStream) {
        FileOutputStream fileOutputStream = null;
        try {
            makeReadWrite(file);
            fileOutputStream = new FileOutputStream(file);
            IOUtils.copyLarge(inputStream, fileOutputStream);
        } catch (final IOException e) {
            LOGGER.error("IO exception writing file contents", e);
        } finally {
            close(fileOutputStream);
        }
    }

    /**
     * This method will take a, potentially large input stream, and incrementally write it to the
     * output file path location.
     *
     * @param filePath    the output file path, the file need not be created already, i.e. it will create the file on the fly if necessary
     * @param inputStream the input stream to write to the file location
     */
    public static void setContents(final String filePath, final InputStream inputStream) {
        File file = FILE.getOrCreateFile(new File(filePath));
        // OutputStream outputStream = null;
        try (OutputStream outputStream = new FileOutputStream(file)) {
            // outputStream = new FileOutputStream(file);
            IOUtils.copyLarge(inputStream, outputStream);
        } catch (final IOException e) {
            throw new RuntimeException("Exception writing the file to the", e);
        }
    }

    /**
     * Gets a single file. First looking to find it, if it can not be found then it is created.
     *
     * @param filePath  the path to the file that is requested
     * @param directory whether the file is a directory of a file
     * @return the file from the path, either from the file system or created on the fly
     */
    public static File getFile(final String filePath, final boolean directory) {
        if (filePath == null) {
            return null;
        }
        File file = new File(filePath);
        if (directory) {
            file = getOrCreateDirectory(file);
            if (file != null && file.exists()) {
                makeReadWrite(file);
            } else {
                LOGGER.warn("Didn't create directory/file : " + filePath);
            }
        } else {
            file = getOrCreateFile(file);
            if (file != null && !file.exists()) {
                makeReadWrite(file);
            } else {
                LOGGER.warn("Didn't create directory/file : " + filePath);
            }
        }
        return file;
    }

    /**
     * Writes the contents of a byte array to the file.
     *
     * @param outputFile the file to write to
     * @param bytes      the data to write to the file
     */
    public static void setContents(final File outputFile, final byte[] bytes) {
        FileOutputStream fileOutputStream = null;
        try {
            makeReadWrite(outputFile);
            fileOutputStream = new FileOutputStream(outputFile);
            setContents(fileOutputStream, bytes);
        } catch (final FileNotFoundException e) {
            LOGGER.error("File " + outputFile + " not found", e);
        } finally {
            close(fileOutputStream);
        }
    }

    public static void setContents(final OutputStream outputStream, final byte[] bytes) {
        try {
            outputStream.write(bytes, 0, bytes.length);
        } catch (final IOException e) {
            LOGGER.error("IO exception writing file contents", e);
        }
    }

    /**
     * Reads the contents of the file and returns the contents in a byte array form.
     *
     * @param file the file to read the contents from
     * @return the file contents in a byte array output stream
     */
    public static ByteArrayOutputStream getContents(final File file, final long maxReadLength) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (final FileNotFoundException e) {
            LOGGER.error("No file by that name : " + file, e);
        } catch (final Exception e) {
            LOGGER.error("General error accessing the file : " + file, e);
        }
        return getContents(inputStream, maxReadLength);
    }

    public static String getContents(final File file, String encoding) {
        StringBuilder builder = new StringBuilder();
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file), encoding);
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) > -1) {
                builder.append(new String(chars, 0, read));
            }
        } catch (final Exception e) {
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
     * @param maxLength   the maximum number of bytes to read into the buffer
     * @return the file contents in a byte array output stream
     */
    public static ByteArrayOutputStream getContents(final InputStream inputStream, final long maxLength) {
        return getContents(inputStream, maxLength, Boolean.FALSE);
    }

    public static ByteArrayOutputStream getContents(final InputStream inputStream, final long maxLength, final boolean close) {
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
        } catch (final Exception e) {
            LOGGER.error("Exception accessing the file contents : " + inputStream, e);
        } finally {
            if (close) {
                close(inputStream);
            }
        }
        return byteArrayOutputStream;
    }

    /**
     * Reads the contents of the stream and returns the contents in a byte array form.
     *
     * @param inputStream  the file to read the contents from
     * @param outputStream the output stream to write the data to
     * @param maxLength    the maximum number of bytes to read into the buffer
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
        } catch (final Exception e) {
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
        } catch (final Exception e) {
            LOGGER.error("Exception accessing the file contents.", e);
        } finally {
            try {
                reader.close();
            } catch (final Exception e) {
                LOGGER.error("Exception closing input stream " + reader, e);
            }
        }
    }

    public static void close(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (final Exception e) {
                LOGGER.error("Exception closing the reader : " + reader, e);
            }
        }
    }

    public static void close(Writer writer) {
        if (writer != null) {
            try {
                writer.flush();
            } catch (final Exception e) {
                LOGGER.error("Exception closing the writer : " + writer, e);
            }
            try {
                writer.close();
            } catch (final Exception e) {
                LOGGER.error("Exception closing the writer : " + writer, e);
            }
        }
    }

    public static void close(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (final Exception e) {
            LOGGER.error("Exception closing stream : " + inputStream, e);
        }
    }

    public static void close(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.flush();
            } catch (final Exception e) {
                LOGGER.error("Exception flushing stream : " + outputStream, e);
            }
            try {
                outputStream.close();
            } catch (final Exception e) {
                LOGGER.error("Exception closing stream : " + outputStream, e);
            }
        }
    }

    /**
     * This method will clean the path, as some operating systems add their special
     * characters, back spaces and the like, that interfere with the normal working of the
     * file system.
     *
     * @param path the path to clea, perhaps something like 'file:C:\\path\\.\\some\\more'
     * @return the path that can be used as an absolute path on the file system
     */
    public static String cleanFilePath(final String path) {
        String filePath = path;
        filePath = StringUtils.replace(filePath, "/./", "/");
        // For windows we must clean the path of 'file:/' because getting the
        // parent then appends the user path for some reason too, returning something
        // like C:/tmp/user/directory/C:/path/to/directory
        filePath = StringUtils.replace(filePath, "file:", "");
        filePath = StringUtils.replace(filePath, "file:/", "");
        filePath = StringUtils.replace(filePath, "file:\\", "");
        filePath = StringUtils.replace(filePath, "\\.\\", "/");
        filePath = StringUtils.replace(filePath, "\\", "/");
        filePath = StringUtils.removeEnd(filePath, ".");
        return filePath;
    }

    /**
     * This is just a convenience method to find the file and clean the absolute path.
     *
     * @param folder  the folder to start looking for the patterns
     * @param pattern the pattern of the file sought after
     * @return the cleaned absolute path to the file
     */
    public static String findFileAndGetCleanedPath(final File folder, final String pattern) {
        File file = findFileRecursively(folder, pattern);
        if (file == null) {
            return null;
        }
        return cleanFilePath(file.getAbsolutePath());
    }

    /**
     * This method checks to see if the file can be read, that it exists
     * and that it is not in the excluded pattern defined in the configuration.
     *
     * @param file    the file to check for inclusion in the processing
     * @param pattern the pattern that excludes explicitly files and folders
     * @return whether this file is included and can be processed
     */
    public static synchronized boolean isExcluded(final File file, final Pattern pattern) {
        // If it does not exist, we can't read it or directory excluded with the pattern
        if (file == null) {
            return Boolean.TRUE;
        }
        if (!file.exists() || !file.canRead()) {
            return Boolean.TRUE;
        }
        if (StringUtils.isEmpty(file.getAbsolutePath())) {
            return Boolean.TRUE;
        }
        if (pattern == null) {
            return Boolean.FALSE;
        }
        String name = file.getName();
        String path = file.getAbsolutePath();
        boolean isNameExcluded = pattern.matcher(name).matches();
        boolean isPathExcluded = pattern.matcher(path).matches();
        boolean isSymLink = Boolean.FALSE;
        boolean exceptionReading = Boolean.FALSE;
        try {
            isSymLink = FileUtils.isSymlink(file);
        } catch (final IOException e) {
            exceptionReading = Boolean.TRUE;
            LOGGER.error("Oops... wasssuuppppp?", e);
        }
        // LOGGER.info("Name : " + isNameExcluded + ", " + isPathExcluded + ", " + isSymLink + ", " + exceptionReading);
        return isNameExcluded || isPathExcluded || isSymLink || exceptionReading;
    }

    /**
     * Singularity.
     */
    private FILE() {
        // Documented
    }

}