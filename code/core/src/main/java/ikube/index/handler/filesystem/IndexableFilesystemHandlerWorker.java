package ikube.index.handler.filesystem;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.index.handler.IndexableHandler;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.schlichtherle.io.FileReader;

/**
 * This is the runnable class that will iterate over the file system and index the files, along with other threads.
 * 
 * @author Michael Couck
 * @since 02.01.2011
 * @version 01.00
 */
class IndexableFilesystemHandlerWorker implements Runnable {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/** The pattern of excluded paths, like /secure/ for example, or /images/. */
	private Pattern pattern;
	/**
	 * This is the dynamic stack of directories that the threads all have access to. While iterating over the file system, each thread will
	 * add directories to this stack. In this way each directory will only be read once even though there are multiple threads iterating
	 * over the file system.
	 */
	private Stack<File> directories;
	/** The index context for this index. */
	private IndexContext<?> indexContext;
	/**
	 * The file system configuration object that has the path to the resource and other properties that will beused by the file system
	 * worker, like whether to store the content in the index.
	 */
	private IndexableFileSystem indexableFileSystem;
	/** The 'parent' handler for this worker. */
	private IndexableHandler<IndexableFileSystem> indexableHandler;

	IndexableFilesystemHandlerWorker(IndexableHandler<IndexableFileSystem> indexableHandler, IndexContext<?> indexContext,
			IndexableFileSystem indexableFileSystem, Stack<File> directories) {
		this.indexableHandler = indexableHandler;
		this.indexContext = indexContext;
		this.indexableFileSystem = indexableFileSystem;
		this.directories = directories;
	}

	/**
	 * This method will iterate over the file system, adding directories to the stack that is shared amongst the worker threads, and index
	 * the files as they are read from the directories.
	 */
	public void run() {
		List<File> files = getBatch(indexableFileSystem, directories);
		do {
			for (File file : files) {
				try {
					if (handleZip(indexableFileSystem, file)) {
						continue;
					}
					this.handleFile(indexContext, indexableFileSystem, file);
				} catch (InterruptedException e) {
					logger.error("Thread terminated, and indexing stopped : ", e);
					return;
				} catch (Exception e) {
					logger.error("Exception handling file : " + file, e);
				}
			}
			files = getBatch(indexableFileSystem, directories);
			if (files.size() == 0) {
				break;
			}
		} while (true);
	}

	/**
	 * As the name suggests this method accesses a file, and hopefully indexes the data adding it to the index.
	 * 
	 * @param indexContext the context for this index
	 * @param indexableFileSystem the file system object for storing data during the indexing
	 * @param file the file to parse and index
	 * @throws InterruptedException
	 */
	protected void handleFile(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final File file)
			throws InterruptedException {
		InputStream inputStream = null;
		try {
			Document document = new Document();
			inputStream = new FileInputStream(file);
			handleFile(file, inputStream, document);
		} catch (InterruptedException e) {
			// This means that the scheduler has been forcefully destroyed
			throw e;
		} catch (Exception e) {
			logger.error("Exception occured while trying to index the file " + file.getAbsolutePath());
			if (logger.isDebugEnabled()) {
				logger.debug(null, e);
			}
		} finally {
			FileUtilities.close(inputStream);
		}
	}

	protected void handleFile(final File file, final InputStream inputStream, final Document document) throws Exception {
		ByteArrayInputStream byteInputStream = null;
		ByteArrayOutputStream byteOutputStream = null;
		try {
			int length = Math.min((int) indexableFileSystem.getMaxReadLength(), (int) file.length());
			byte[] byteBuffer = new byte[length];
			int read = inputStream.read(byteBuffer, 0, byteBuffer.length);

			byteInputStream = new ByteArrayInputStream(byteBuffer, 0, read);
			byteOutputStream = new ByteArrayOutputStream();

			IParser parser = ParserProvider.getParser(file.getName(), byteBuffer);
			String parsedContent = parser.parse(byteInputStream, byteOutputStream).toString();

			Store store = indexableFileSystem.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexableFileSystem.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexableFileSystem.isVectored() ? TermVector.YES : TermVector.NO;

			String pathFieldName = indexableFileSystem.getPathFieldName();
			String nameFieldName = indexableFileSystem.getNameFieldName();
			String modifiedFieldName = indexableFileSystem.getLastModifiedFieldName();
			String lengthFieldName = indexableFileSystem.getLengthFieldName();
			String contentFieldName = indexableFileSystem.getContentFieldName();

			IndexManager.addStringField(pathFieldName, file.getAbsolutePath(), document, Store.YES, analyzed, termVector);
			IndexManager.addStringField(nameFieldName, file.getName(), document, Store.YES, analyzed, termVector);
			IndexManager.addStringField(modifiedFieldName, Long.toString(file.lastModified()), document, Store.YES, analyzed, termVector);
			IndexManager.addStringField(lengthFieldName, Long.toString(file.length()), document, Store.YES, analyzed, termVector);
			IndexManager.addStringField(contentFieldName, parsedContent, document, store, analyzed, termVector);
			indexableHandler.addDocument(indexContext, indexableFileSystem, document);

			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException("Interrupted...");
			}
			Thread.sleep(indexContext.getThrottle());
		} finally {
			FileUtilities.close(byteInputStream);
			FileUtilities.close(byteOutputStream);
		}
	}

	protected boolean handleZip(IndexableFileSystem indexableFileSystem, File file) throws Exception {
		// We have to unpack the zip files
		if (!indexableFileSystem.isUnpackZips()) {
			return Boolean.FALSE;
		}
		boolean isZip = IConstants.ZIP_JAR_WAR_EAR_PATTERN.matcher(file.getName()).matches();
		boolean isFile = file.isFile();
		boolean isTrueFile = de.schlichtherle.io.File.class.isAssignableFrom(file.getClass());
		boolean isZipAndFile = isZip && (isFile || isTrueFile);
		if (isZipAndFile) {
			de.schlichtherle.io.File trueZipFile = new de.schlichtherle.io.File(file);
			File[] files = trueZipFile.listFiles();
			for (File innerFile : files) {
				try {
					handleFile(innerFile);
					handleZip(indexableFileSystem, innerFile);
				} catch (Exception e) {
					logger.error("Exception reading inner file : " + innerFile, e);
				}
			}
		}
		return Boolean.TRUE;
	}

	protected void handleFile(final File file) throws Exception {
		FileReader fileReader = null;
		try {
			if (file.isDirectory()) {
				for (File innerFile : file.listFiles()) {
					handleFile(innerFile);
				}
			} else {
				fileReader = new FileReader((de.schlichtherle.io.File) file);
				InputStream inputStream = new ReaderInputStream(fileReader);
				handleFile(file, inputStream, new Document());
			}
		} finally {
			FileUtilities.close(fileReader);
		}
	}

	protected List<File> getBatch(IndexableFileSystem indexableFileSystem, Stack<File> directories) {
		Pattern pattern = getPattern(indexableFileSystem.getExcludedPattern());
		List<File> fileBatch = new ArrayList<File>();
		if (!directories.isEmpty()) {
			File directory = directories.pop();
			if (directory.isFile()) {
				fileBatch.add(directory);
			}
			// Get the files for our directory that we are going to index
			File[] files = directory.listFiles();
			if (files != null && files.length > 0) {
				for (File file : files) {
					if (isExcluded(file, pattern)) {
						continue;
					}
					if (file.isDirectory()) {
						// Put all the directories on the stack
						directories.push(file);
					} else {
						if (file.isFile() && file.canRead()) {
							// We'll do this file ourselves
							fileBatch.add(file);
						}
					}
				}
			}
			if (fileBatch.isEmpty()) {
				// Means that there were no files in this directory
				return getBatch(indexableFileSystem, directories);
			}
		}
		logger.info("Directories : " + directories.size() + ", doing files : " + fileBatch.size());
		return fileBatch;
	}

	protected synchronized Pattern getPattern(final String pattern) {
		if (this.pattern == null) {
			this.pattern = Pattern.compile(pattern != null ? pattern : "");
		}
		return this.pattern;
	}

	/**
	 * This method checks to see if the file can be read, that it exists and that it is not in the excluded pattern defined in the
	 * configuration.
	 * 
	 * @param file the file to check for inclusion in the processing
	 * @param pattern the pattern that excludes explicitly files and folders
	 * @return whether this file is included and can be processed
	 */
	protected synchronized boolean isExcluded(final File file, final Pattern pattern) {
		// If it does not exist, we can't read it or directory excluded with the pattern
		return file == null || !file.exists() || !file.canRead() || pattern.matcher(file.getName()).matches();
	}
}