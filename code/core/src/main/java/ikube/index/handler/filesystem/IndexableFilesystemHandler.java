package ikube.index.handler.filesystem;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.index.handler.IndexableHandler;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;

/**
 * This class indexes a file share on the network. It is multi threaded but not cluster load balanced. First the files are iterated over and
 * added to the database. Then the crawler threads are started and they will get batches of files, process them, then set the indexed flag
 * for the files and merge them back to the database. This prevents too many threads iterating over the file system which seems to be the
 * bottleneck in the process.
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

	private static final String STRING_PATTERN = ".*(\\.zip\\Z).*|.*(\\.jar\\Z).*|.*(\\.war\\Z).*|.*(\\.ear\\Z).*|.*(\\.gz\\Z).*|.*(\\.sar\\Z).*|.*(\\.tar\\Z).*";
	private static final Pattern ZIP_JAR_WAR_EAR_PATTERN = Pattern.compile(STRING_PATTERN);
	// private static final String JAVA_IO_TEMP_DIR = System.getProperty("java.io.tmpdir");
	private static final String TO_DIR = "/tmp/unzipped";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handle(final IndexContext<?> indexContext, final IndexableFileSystem indexable) throws Exception {
		final File baseFile = new File(indexable.getPath());
		final Pattern pattern = getPattern(indexable.getExcludedPattern());
		// Add all the files to the database
		final Set<File> batchedFiles = new TreeSet<File>();
		Future<?> fileSystemFuture = ThreadUtilities.submit(new Runnable() {
			public void run() {
				iterateFileSystem(dataBase, indexContext, indexable, baseFile, pattern, batchedFiles);
				// Persist the last of the files in the list
				if (batchedFiles.size() > 0) {
					persistFilesBatch(dataBase, indexable, batchedFiles);
				}
			}
		});
		ThreadUtilities.waitForFuture(fileSystemFuture, Integer.MAX_VALUE);
		// Now start the threads indexing the files from the database
		List<Future<?>> futures = new ArrayList<Future<?>>();
		try {
			for (int i = 0; i < getThreads(); i++) {
				Runnable runnable = new Runnable() {
					public void run() {
						IndexableFileSystem indexableFileSystem = (IndexableFileSystem) SerializationUtilities.clone(indexable);
						List<ikube.model.File> dbFiles = getBatch(dataBase, indexableFileSystem);
						do {
							for (ikube.model.File dbFile : dbFiles) {
								if (dbFile.getUrl() == null) {
									logger.warn("DB file url null : ");
									continue;
								}
								try {
									handleFile(indexContext, indexableFileSystem, dbFile);
								} catch (InterruptedException e) {
									logger.error("Thread terminated, and indexing stopped : ", e);
									return;
								}
							}
							dbFiles = getBatch(dataBase, indexableFileSystem);
						} while (!dbFiles.isEmpty());
					}
				};
				Future<?> future = ThreadUtilities.submit(runnable);
				futures.add(future);
			}
		} catch (Exception e) {
			logger.error("Exception starting the file system indexer threads : ", e);
		}
		return futures;
	}

	/**
	 * This method will get the next batch of files from the database that are not yet processed.
	 * 
	 * @param dataBase the database for the persistence
	 * @param indexable the file system configuration object
	 * @return the list of files still to be indexed, could be empty if there are not more files
	 */
	private synchronized List<ikube.model.File> getBatch(final IDataBase dataBase, final IndexableFileSystem indexable) {
		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put(IConstants.NAME, indexable.getName());
			parameters.put(IConstants.INDEXED, Boolean.FALSE);
			List<ikube.model.File> dbFiles = dataBase.find(ikube.model.File.class, ikube.model.File.SELECT_FROM_FILE_BY_NAME_AND_INDEXED,
					parameters, 0, indexable.getBatchSize());
			for (ikube.model.File dbFile : dbFiles) {
				dbFile.setIndexed(Boolean.TRUE);
			}
			dataBase.mergeBatch(dbFiles);
			logger.info("Doing files : " + dbFiles.size());
			return dbFiles;
		} finally {
			notifyAll();
		}
	}

	/**
	 * As the name suggests this method handles a folder. Iterates over the files and folders in the folder recursively indexing the files
	 * as they are encountered.
	 * 
	 * @param indexContext the index context for the index
	 * @param indexableFileSystem the file system object for storing data during the indexing
	 * @param folder the folder that we are iterating over
	 * @param excludedPattern the excluded patterns
	 */
	protected void iterateFileSystem(final IDataBase dataBase, final IndexContext<?> indexContext, final IndexableFileSystem indexable,
			final File folder, final Pattern excludedPattern, final Set<File> batchedFiles) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (isExcluded(file, excludedPattern)) {
					continue;
				}
				if (file.isDirectory()) {
					iterateFileSystem(dataBase, indexContext, indexable, file, excludedPattern, batchedFiles);
				} else {
					batchedFiles.add(file);
					if (batchedFiles.size() >= indexable.getBatchSize()) {
						persistFilesBatch(dataBase, indexable, batchedFiles);
					}
				}
			}
		}
	}

	/**
	 * This method will persist the batch of files that have been accumulated by iterating over the file system.
	 * 
	 * @param dataBase the database for the persistence
	 * @param indexable the file system base configuration object
	 * @param batchedFiles the batch of files that have been collected by iterating over the file system
	 */
	protected void persistFilesBatch(final IDataBase dataBase, final IndexableFileSystem indexable, final Set<File> batchedFiles) {
		logger.info("Persisting files : " + batchedFiles.size());
		List<ikube.model.File> filesToPersist = new ArrayList<ikube.model.File>();
		for (File toPersistFile : batchedFiles) {
			long urlId = HashUtilities.hash(toPersistFile.getAbsolutePath());
			ikube.model.File dbFile = new ikube.model.File();
			dbFile.setName(indexable.getName());
			dbFile.setIndexed(Boolean.FALSE);
			dbFile.setUrl(toPersistFile.getAbsolutePath());
			dbFile.setUrlId(urlId);
			filesToPersist.add(dbFile);
		}
		dataBase.persistBatch(filesToPersist);
		batchedFiles.clear();
	}

	protected void persistFileBatchTemporary(final IDataBase dataBase, final IndexableFileSystem indexable, final Set<File> batchedFiles) {
		logger.info("Persisting temporary files : " + batchedFiles.size());
		List<ikube.model.File> filesToPersist = new ArrayList<ikube.model.File>();
		for (File toPersistFile : batchedFiles) {
			long urlId = HashUtilities.hash(toPersistFile.getAbsolutePath());
			ikube.model.File dbFile = new ikube.model.File();
			dbFile.setName(indexable.getName());
			dbFile.setIndexed(Boolean.FALSE);
			dbFile.setUrl(toPersistFile.getAbsolutePath());
			dbFile.setUrlId(urlId);
			filesToPersist.add(dbFile);
		}
		dataBase.persistBatch(filesToPersist);
		batchedFiles.clear();
	}

	/**
	 * As the name suggests this method accesses a file, and hopefully indexes the data adding it to the index.
	 * 
	 * @param indexContext the context for this index
	 * @param indexableFileSystem the file system object for storing data during the indexing
	 * @param file the file to parse and index
	 * @throws InterruptedException
	 */
	protected void handleFile(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem,
			final ikube.model.File dbFile) throws InterruptedException {
		File file = new File(dbFile.getUrl());
		try {
			// We have to unpack the zip files
			boolean isZipAndFile = ZIP_JAR_WAR_EAR_PATTERN.matcher(file.getName()).matches() && file.isFile();
			// logger.info("Doing file : " + isZipAndFile + ", " + file.getAbsolutePath());
			if (isZipAndFile) {
				File unzippedToFolder = FileUtilities.unzip(file.getAbsolutePath(), "." + TO_DIR);
				logger.warn("Unzipping : " + file.getAbsolutePath() + " to " + unzippedToFolder);
				if (unzippedToFolder != null && unzippedToFolder.exists() && unzippedToFolder.isFile()) {
					Pattern excludedPattern = getPattern(indexableFileSystem.getExcludedPattern());
					Set<File> batchedFiles = new TreeSet<File>();
					iterateFileSystem(dataBase, indexContext, indexableFileSystem, unzippedToFolder, excludedPattern, batchedFiles);
					persistFilesBatch(dataBase, indexableFileSystem, batchedFiles);
				}
			}

			// logger.info("Db file : " + dbFile);
			Document document = new Document();
			indexableFileSystem.setCurrentFile(file);

			ByteArrayOutputStream byteArrayOutputStream = FileUtilities.getContents(file, (int) indexContext.getMaxReadLength());
			InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

			// Read the first bit so we can see what type of data this is
			byte[] bytes = new byte[1024];
			if (inputStream.markSupported()) {
				inputStream.mark(Short.MAX_VALUE);
				int read = inputStream.read(bytes);
				if (read > 0) {
					inputStream.reset();
				}
			}

			IParser parser = ParserProvider.getParser(file.getName(), bytes);
			OutputStream parsedOutputStream = parser.parse(inputStream, new ByteArrayOutputStream());
			String parsedContent = parsedOutputStream.toString();

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
			addDocument(indexContext, document);

			Thread.sleep(indexContext.getThrottle());
		} catch (InterruptedException e) {
			// This means that the scheduler has been forcefully destroyed
			throw e;
		} catch (IOException e) {
			logger.error("Exception indexing file : " + file, e);
		} catch (Exception e) {
			logger.error("Exception occured while trying to index the file " + file.getAbsolutePath(), e);
		} finally {
			String filePath = file.getAbsolutePath();
			StringUtils.replace(filePath, "\\", "/");
			boolean isFileAndInTemp = file.isFile() && filePath.contains(TO_DIR);
			logger.warn("Java temp : " + isFileAndInTemp + ", " + filePath);
			if (isFileAndInTemp) {
				logger.warn("Deleting file : " + filePath);
				FileUtilities.deleteFile(file, 1);
			}
		}
	}

	protected Pattern getPattern(final String pattern) {
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
	protected boolean isExcluded(final File file, final Pattern pattern) {
		// If it does not exist, we can't read it or directory excluded with the pattern
		return file == null || !file.exists() || !file.canRead() || pattern.matcher(file.getName()).matches();
	}

	@Override
	public void addDocument(IndexContext<?> indexContext, Document document) throws CorruptIndexException, IOException {
		indexContext.getIndex().getIndexWriter().addDocument(document);
	}

}
