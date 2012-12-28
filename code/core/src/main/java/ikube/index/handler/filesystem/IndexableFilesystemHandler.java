package ikube.index.handler.filesystem;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.index.handler.IndexableHandler;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

import de.schlichtherle.io.FileReader;

/**
 * This class indexes a file share on the network. It is multi threaded but not cluster load balanced.
 * 
 * This class is optimised for performance, as such it is not very elegant.
 * 
 * @author Cristi Bozga
 * @author Michael Couck
 * @since 29.11.10
 * @version 02.00<br>
 *          Updated this class to persist the files and to be multi threaded to improve the performance.
 */
public class IndexableFilesystemHandler extends IndexableHandler<IndexableFileSystem> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handle(final IndexContext<?> indexContext, final IndexableFileSystem indexable) throws Exception {
		List<Future<?>> futures = new ArrayList<Future<?>>();
		try {
			final Stack<File> directories = new Stack<File>();
			directories.push(new File(indexable.getPath()));
			final Pattern pattern = getPattern(indexable.getExcludedPattern());
			for (int i = 0; i < getThreads(); i++) {
				final IndexableFileSystem indexableFileSystem = (IndexableFileSystem) SerializationUtilities.clone(indexable);
				Runnable runnable = new Runnable() {
					public void run() {
						List<File> files = getBatch(indexableFileSystem, directories, pattern);
						do {
							for (File file : files) {
								try {
									logger.info("Doing file : " + file);
									boolean handledZip = handleZip(indexContext, indexableFileSystem, file, pattern);
									if (handledZip) {
										// If this file was a zip and was already handled then just continue
										continue;
									}
									handleFile(indexContext, indexableFileSystem, file);
								} catch (InterruptedException e) {
									logger.error("Thread terminated, and indexing stopped : ", e);
									throw new RuntimeException(e);
								} catch (Exception e) {
									logger.error("Exception handling file : " + file, e);
								}
							}
							files = getBatch(indexableFileSystem, directories, pattern);
							if (files.size() == 0) {
								break;
							}
						} while (true);
					}
				};
				Future<?> future = ThreadUtilities.submit(indexContext.getIndexName(), runnable);
				futures.add(future);
			}
		} catch (Exception e) {
			logger.error("Exception executing the file system indexer threads : ", e);
		}
		return futures;
	}

	protected List<File> getBatch(final IndexableFileSystem indexableFileSystem, final Stack<File> directories, final Pattern pattern) {
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
				return getBatch(indexableFileSystem, directories, pattern);
			}
		}
		return fileBatch;
	}

	protected void processFile(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final File file)
			throws Exception {
		FileReader fileReader = null;
		try {
			if (file.isDirectory()) {
				for (File innerFile : file.listFiles()) {
					processFile(indexContext, indexableFileSystem, innerFile);
				}
			} else {
				fileReader = new FileReader((de.schlichtherle.io.File) file);
				InputStream inputStream = new ReaderInputStream(fileReader);
				addDocumentToIndex(indexContext, indexableFileSystem, file, inputStream, new Document());
			}
		} finally {
			FileUtilities.close(fileReader);
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
	public void handleFile(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final File file)
			throws InterruptedException {
		InputStream inputStream = null;
		try {
			Document document = new Document();
			inputStream = new FileInputStream(file);
			addDocumentToIndex(indexContext, indexableFileSystem, file, inputStream, document);
		} catch (InterruptedException e) {
			// This means that the scheduler has been forcefully destroyed
			throw e;
		} catch (CancellationException e) {
			// This means that the scheduler has been forcefully destroyed
			throw e;
		} catch (Exception e) {
			logger.error("Exception occured while trying to index the file " + file.getAbsolutePath() + ", " + e.getMessage());
			logger.debug(null, e);
		} finally {
			FileUtilities.close(inputStream);
		}
	}

	protected void addDocumentToIndex(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final File file,
			final InputStream inputStream, final Document document) throws Exception {
		ByteArrayInputStream byteInputStream = null;
		ByteArrayOutputStream byteOutputStream = null;
		try {
			int length = file.length() > 0 && file.length() < indexableFileSystem.getMaxReadLength() ? (int) file.length()
					: (int) indexableFileSystem.getMaxReadLength();
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
			// logger.info("Adding document : " + document);
			addDocument(indexContext, indexableFileSystem, document);

			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException("Interrupted...");
			}
			Thread.sleep(indexContext.getThrottle());
		} finally {
			FileUtilities.close(byteInputStream);
			FileUtilities.close(byteOutputStream);
		}
	}

	/**
	 * This method will handle the zip file, it will look into the zip and read the contents, then index them. IT will then return whether
	 * the file was a zip file.
	 * 
	 * @param indexableFileSystem the indexable for the file system
	 * @param file the file to handle if it is a zip
	 * @return whether the file was a zip and was handled
	 * @throws Exception
	 */
	protected boolean handleZip(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final File file,
			final Pattern pattern) throws Exception {
		// We have to unpack the zip files
		if (!indexableFileSystem.isUnpackZips()) {
			return Boolean.FALSE;
		}
		boolean isFile = file.isFile();
		boolean isZip = IConstants.ZIP_JAR_WAR_EAR_PATTERN.matcher(file.getName()).matches();
		boolean isTrueFile = de.schlichtherle.io.File.class.isAssignableFrom(file.getClass());
		boolean isZipAndFile = isZip && (isFile || isTrueFile);
		logger.info("Is zip and file : " + isZipAndFile);
		if (isZipAndFile) {
			de.schlichtherle.io.File trueZipFile = new de.schlichtherle.io.File(file);
			File[] files = trueZipFile.listFiles();
			logger.info("Compressed files : " + Arrays.deepToString(files));
			if (files != null) {
				for (File innerFile : files) {
					try {
						if (isExcluded(innerFile, pattern)) {
							continue;
						}
						logger.info("Indexing compressed file : " + innerFile);
						processFile(indexContext, indexableFileSystem, innerFile);
						handleZip(indexContext, indexableFileSystem, innerFile, pattern);
					} catch (Exception e) {
						logger.error("Exception reading inner file : " + innerFile, e);
					}
				}
			}
		}
		return isZipAndFile;
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
		if (file == null) {
			return Boolean.TRUE;
		}
		if (!file.exists() || !file.canRead()) {
			return Boolean.TRUE;
		}
		if (file.getName() == null || file.getAbsolutePath() == null) {
			return Boolean.TRUE;
		}
		boolean isNameExcluded = pattern.matcher(file.getName()).matches();
		boolean isPathExcluded = pattern.matcher(file.getAbsolutePath()).matches();
		boolean isExcluded = isNameExcluded || isPathExcluded;
		logger.info("Is excluded : " + isExcluded);
		return isExcluded;
	}

	protected synchronized Pattern getPattern(final String pattern) {
		return Pattern.compile(pattern != null ? pattern : "");
	}

}