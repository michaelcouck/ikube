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

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the runnable class that will iterate over the file system and index the files, along with other threads.
 * 
 * @author Michael Couck
 * @since 02.01.2011
 * @version 01.00
 */
class IndexableFilesystemHandlerWorker implements Runnable {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Stack<File> directories;
	private IndexContext<?> indexContext;
	private IndexableFileSystem indexableFileSystem;
	private ByteArrayInputStream byteInputStream;
	private ByteArrayOutputStream byteOutputStream;
	private IndexableHandler<IndexableFileSystem> indexableHandler;

	IndexableFilesystemHandlerWorker(IndexableHandler<IndexableFileSystem> indexableHandler, IndexContext<?> indexContext,
			IndexableFileSystem indexableFileSystem, Stack<File> directories) {
		this.indexableHandler = indexableHandler;
		this.indexContext = indexContext;
		this.indexableFileSystem = indexableFileSystem;
		this.directories = directories;
	}

	public void run() {
		try {
			List<File> files = getBatch(indexableFileSystem, directories);
			if (files != null) {
				do {
					for (File file : files) {
						try {
							if (unzip(indexableFileSystem, file)) {
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
				} while (files != null && files.size() > 0);
			}
		} catch (Exception e) {
			logger.error("Exception in worker : ", e);
		} finally {
			// indexableFileSystem.setByteBuffer(null);
		}
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
		Document document = null;
		String parsedContent = null;
		InputStream inputStream = null;
		try {
			document = new Document();

			inputStream = new FileInputStream(file);

			int length = Math.min((int) indexableFileSystem.getMaxReadLength(), (int) file.length());
			byte[] byteBuffer = new byte[length];
			int read = inputStream.read(byteBuffer, 0, byteBuffer.length);

			byteInputStream = new ByteArrayInputStream(byteBuffer, 0, read);
			byteOutputStream = new ByteArrayOutputStream();

			IParser parser = ParserProvider.getParser(file.getName(), byteBuffer);
			parsedContent = parser.parse(byteInputStream, byteOutputStream).toString();

			Store store = indexableFileSystem.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexableFileSystem.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexableFileSystem.isVectored() ? TermVector.YES : TermVector.NO;

			String pathFieldName = indexableFileSystem.getPathFieldName();
			String nameFieldName = indexableFileSystem.getNameFieldName();
			String modifiedFieldName = indexableFileSystem.getLastModifiedFieldName();
			String lengthFieldName = indexableFileSystem.getLengthFieldName();
			String contentFieldName = indexableFileSystem.getContentFieldName();

			IndexManager.addStringField(pathFieldName, file.getAbsolutePath(), document, store, analyzed, termVector);
			IndexManager.addStringField(nameFieldName, file.getName(), document, store, analyzed, termVector);
			IndexManager.addStringField(modifiedFieldName, Long.toString(file.lastModified()), document, store, analyzed, termVector);
			IndexManager.addStringField(lengthFieldName, Long.toString(file.length()), document, store, analyzed, termVector);
			IndexManager.addStringField(contentFieldName, parsedContent, document, store, analyzed, termVector);
			indexableHandler.addDocument(indexContext, indexableFileSystem, document);

			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException("Interrupted...");
			}
			Thread.sleep(indexContext.getThrottle());
		} catch (InterruptedException e) {
			// This means that the scheduler has been forcefully destroyed
			throw e;
		} catch (Exception e) {
			logger.error("Exception occured while trying to index the file " + file.getAbsolutePath(), e);
		} finally {
			FileUtilities.close(inputStream);
			FileUtilities.close(byteInputStream);
			FileUtilities.close(byteOutputStream);
			String filePath = file.getAbsolutePath();
			StringUtils.replace(filePath, "\\", "/");
			boolean isFileAndInTemp = file.isFile() && filePath.contains(IConstants.TMP_UNZIPPED_FOLDER);
			if (isFileAndInTemp) {
				logger.warn("Deleting file : " + filePath);
				FileUtilities.deleteFile(file, 1);
			}
		}
	}

	private boolean unzip(IndexableFileSystem indexableFileSystem, File file) {
		// We have to unpack the zip files
		if (indexableFileSystem.isUnpackZips()) {
			boolean isZipAndFile = IConstants.ZIP_JAR_WAR_EAR_PATTERN.matcher(file.getName()).matches() && file.isFile();
			if (isZipAndFile) {
				File unzippedToFolder = FileUtilities.unzip(file.getAbsolutePath(), "." + IConstants.TMP_UNZIPPED_FOLDER);
				if (unzippedToFolder != null && unzippedToFolder.exists() && unzippedToFolder.isDirectory()) {
					logger.info("Added unzipped folder : " + unzippedToFolder.getAbsolutePath());
					directories.add(unzippedToFolder);
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	protected synchronized List<File> getBatch(IndexableFileSystem indexableFileSystem, Stack<File> directories) {
		try {
			Pattern pattern = getPattern(indexableFileSystem.getExcludedPattern());
			List<File> results = new ArrayList<File>();
			if (!directories.isEmpty()) {
				File directory = directories.pop();
				if (directory != null) {
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
								// We'll do this file ourselves
								results.add(file);
							}
						}
					}
					if (results.isEmpty()) {
						return getBatch(indexableFileSystem, directories);
					}
					logger.info("Directories : " + directories.size());
					logger.info("Doing files : " + results.size());
					return results;
				}
			}
			return null;
		} finally {
			notifyAll();
		}
	}

	protected synchronized Pattern getPattern(final String pattern) {
		return Pattern.compile(pattern != null ? pattern : "");
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
