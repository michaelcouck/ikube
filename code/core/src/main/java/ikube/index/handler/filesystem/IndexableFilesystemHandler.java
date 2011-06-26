package ikube.index.handler.filesystem;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.index.handler.IndexableHandler;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * This class indexes a file share on the network. It is multi threaded but not cluster load balanced. Each thread will iterate over each
 * file, this seems faster than trying to divide the files up among the threads logically, and acts as a natural load balancing between the
 * threads.
 * 
 * @author Cristi Bozga
 * @author Michael Couck
 * @since 29.11.10
 * @version 02.00<br>
 *          Updated this class to persist the files and to be multi threaded to improve the performance.
 */
public class IndexableFilesystemHandler extends IndexableHandler<IndexableFileSystem> {

	private IDataBase dataBase;

	@Override
	public List<Thread> handle(final IndexContext<?> indexContext, final IndexableFileSystem indexable) throws Exception {
		if (dataBase == null) {
			dataBase = ApplicationContextManager.getBean(IDataBase.class);
		}
		// We shouldn't have to do this but we want to use the same files
		// again and again in the tests, I know, really bad, but I don't have 100
		// terabytes to spare for the data
		List<ikube.model.File> list = dataBase.find(ikube.model.File.class, 0, 1000);
		do {
			dataBase.removeBatch(list);
			list = dataBase.find(ikube.model.File.class, 0, 1000);
		} while (list.size() > 0);

		final File baseFile = new File(indexable.getPath());
		final Pattern pattern = getPattern(indexable.getExcludedPattern());
		if (isExcluded(baseFile, pattern)) {
			logger.warn("Base directory excluded : " + baseFile);
			return null;
		}
		// Add all the files to the database
		final Set<File> filesDone = new TreeSet<File>();
		handleFolder(indexContext, indexable, baseFile, getPattern(indexable.getExcludedPattern()), filesDone);
		// Now start the threads indexing the files from the database
		List<Thread> threads = new ArrayList<Thread>();
		String name = this.getClass().getSimpleName();
		for (int i = 0; i < getThreads(); i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					List<ikube.model.File> dbFiles = getBatch(indexable);
					do {
						for (ikube.model.File dbFile : dbFiles) {
							if (dbFile.getUrl() == null) {
								logger.warn("DB file url null : ");
								continue;
							}
							handleFile(indexContext, indexable, dbFile);
						}
						dbFiles = getBatch(indexable);
					} while (!dbFiles.isEmpty());
				}
			}, name + "." + i);
			thread.start();
			threads.add(thread);
		}
		return threads;
	}

	protected synchronized List<ikube.model.File> getBatch(final IndexableFileSystem indexable) {
		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put(IConstants.NAME, indexable.getParent().getName());
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
	 * @param indexContext
	 *            the index context for the index
	 * @param indexableFileSystem
	 *            the file system object for storing data during the indexing
	 * @param folder
	 *            the folder that we are iterating over
	 * @param excludedPattern
	 *            the excluded patterns
	 */
	protected void handleFolder(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final File folder,
			final Pattern excludedPattern, final Set<File> filesDone) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (isExcluded(file, excludedPattern)) {
					continue;
				}
				if (file.isDirectory()) {
					handleFolder(indexContext, indexableFileSystem, file, excludedPattern, filesDone);
				} else {
					addFile(indexableFileSystem, file, filesDone);
				}
			}
		}
	}

	protected boolean addFile(final IndexableFileSystem indexable, File file, final Set<File> filesDone) {
		filesDone.add(file);
		if (filesDone.size() >= indexable.getBatchSize()) {
			logger.info("Persisting files : " + filesDone.size());
			List<ikube.model.File> filesToPersist = new ArrayList<ikube.model.File>();
			for (File toPersistFile : filesDone) {
				long urlId = HashUtilities.hash(toPersistFile.getAbsolutePath());
				ikube.model.File dbFile = new ikube.model.File();
				dbFile.setName(indexable.getParent().getName());
				dbFile.setIndexed(Boolean.FALSE);
				dbFile.setUrl(toPersistFile.getAbsolutePath());
				dbFile.setUrlId(urlId);
				filesToPersist.add(dbFile);
			}
			dataBase.persistBatch(filesToPersist);
			filesDone.clear();
		}
		return Boolean.FALSE;
	}

	/**
	 * As the name suggests this method accesses a file, and hopefully indexes the data adding it to the index.
	 * 
	 * @param indexContext
	 *            the context for this index
	 * @param indexableFileSystem
	 *            the file system object for storing data during the indexing
	 * @param file
	 *            the file to parse and index
	 */
	protected void handleFile(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final ikube.model.File dbFile) {
		File file = new File(dbFile.getUrl());
		try {
			Document document = new Document();
			indexableFileSystem.setCurrentFile(file);

			// TODO - this can be very large so we have to use a reader if necessary
			ByteArrayOutputStream byteArrayOutputStream = FileUtilities.getContents(file, (int) indexContext.getMaxReadLength());
			InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

			// Read the first bit so we can see what type of data this is
			byte[] bytes = new byte[1024];
			if (inputStream.markSupported()) {
				inputStream.mark(Short.MAX_VALUE);
				inputStream.read(bytes);
				inputStream.reset();
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
			addDocument(indexContext, indexableFileSystem, document);
		} catch (Exception e) {
			logger.error("Exception occured while trying to index the file " + file.getAbsolutePath(), e);
		}
	}

	protected Pattern getPattern(final String pattern) {
		return Pattern.compile(pattern != null ? pattern : "");
	}

	protected boolean isExcluded(final File file, final Pattern pattern) {
		// If it does not exist, we can't read it or directory excluded with the pattern
		return file == null || !file.exists() || !file.canRead() || pattern.matcher(file.getName()).matches();
	}

}
