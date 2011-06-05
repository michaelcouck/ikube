package ikube.index.handler.filesystem;

import ikube.index.IndexManager;
import ikube.index.handler.IndexableHandler;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 * @version 01.00
 */
public class IndexableFilesystemHandler extends IndexableHandler<IndexableFileSystem> {

	@Override
	public List<Thread> handle(final IndexContext indexContext, final IndexableFileSystem indexable) throws Exception {
		List<Thread> threads = new ArrayList<Thread>();
		try {
			// We need to check the cluster to see if this indexable is already handled by
			// one of the other servers. The file system is very fast and there is no need to
			// cluster the indexing
			final File baseFile = new File(indexable.getPath());
			final Pattern pattern = getPattern(indexable.getExcludedPattern());
			if (isExcluded(baseFile, pattern)) {
				logger.warn("Base directory excluded : " + baseFile);
				return null;
			}
			final List<Long> filesDone = new ArrayList<Long>();
			for (int i = 0; i < getThreads(); i++) {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						if (baseFile.isDirectory()) {
							handleFolder(indexContext, indexable, baseFile, pattern, filesDone);
						} else {
							handleFile(indexContext, indexable, baseFile);
						}
					}
				});
				thread.start();
				threads.add(thread);
			}
		} catch (Exception e) {
			logger.error("Exception indexing the file share : " + indexable, e);
		}
		return threads;
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
	protected void handleFolder(final IndexContext indexContext, final IndexableFileSystem indexableFileSystem, final File folder,
			final Pattern excludedPattern, final List<Long> filesDone) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (isExcluded(file, excludedPattern)) {
					continue;
				}
				if (file.isDirectory()) {
					handleFolder(indexContext, indexableFileSystem, file, excludedPattern, filesDone);
				} else {
					if (isDone(file, filesDone)) {
						continue;
					}
					if (logger.isDebugEnabled()) {
						logger.debug("Visiting file : " + file + ", " + Thread.currentThread().hashCode());
					}
					handleFile(indexContext, indexableFileSystem, file);
				}
			}
		}
	}

	protected synchronized boolean isDone(File file, final List<Long> filesDone) {
		try {
			long hash = HashUtilities.hash(file.getAbsolutePath());
			int insertionPoint = Collections.binarySearch(filesDone, hash);
			if (insertionPoint >= 0) {
				return Boolean.TRUE;
			}
			insertionPoint = Math.abs(insertionPoint) + 1;
			if (insertionPoint > filesDone.size()) {
				insertionPoint = filesDone.size();
				Collections.sort(filesDone);
			}
			filesDone.add(insertionPoint, hash);
			// logger.info("File : " + hash + ", " + contains + ", " + filesDone.hashCode() + ", " + filesDone);
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
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
	protected void handleFile(final IndexContext indexContext, final IndexableFileSystem indexableFileSystem, final File file) {
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

			Store store = indexableFileSystem.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexableFileSystem.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexableFileSystem.isVectored() ? TermVector.YES : TermVector.NO;

			String pathFieldName = indexableFileSystem.getPathFieldName();
			String nameFieldName = indexableFileSystem.getNameFieldName();
			String modifiedFieldName = indexableFileSystem.getLastModifiedFieldName();
			String lengthFieldName = indexableFileSystem.getLengthFieldName();
			String contentFieldName = indexableFileSystem.getContentFieldName();

			// The path
			IndexManager.addStringField(pathFieldName, file.getAbsolutePath(), document, store, analyzed, termVector);
			// The name
			IndexManager.addStringField(nameFieldName, file.getName(), document, store, analyzed, termVector);
			// Last modified
			IndexManager.addStringField(modifiedFieldName, Long.toString(file.lastModified()), document, store, analyzed, termVector);
			// Length
			IndexManager.addStringField(lengthFieldName, Long.toString(file.length()), document, store, analyzed, termVector);
			// Content
			IndexManager.addStringField(contentFieldName, parsedOutputStream.toString(), document, store, analyzed, termVector);
			// And to the index
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
