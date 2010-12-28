package ikube.index.handler.filesystem;

import ikube.cluster.IClusterManager;
import ikube.index.IndexManager;
import ikube.index.handler.IndexableHandler;
import ikube.index.handler.IndexableHandlerType;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * This class indexes a file share on the network. It is not multi threaded as file access is generally faster than parsing anyway so
 * performance would degrade if this class is multi-threaded due to the cluster synchronization overhead.
 * 
 * @author Cristi Bozga
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableFilesystemHandler extends IndexableHandler<IndexableFileSystem> {

	@Override
	@IndexableHandlerType(type = IndexableFileSystem.class)
	public List<Thread> handle(final IndexContext indexContext, final IndexableFileSystem indexable) throws Exception {
		try {
			// We need to check the cluster to see if this indexable is already handled by
			// one of the other servers. The file system is very fast and there is no need to
			// cluster the indexing
			boolean isHandled = isHandled(indexContext, indexable);
			if (isHandled) {
				return null;
			}
			File baseFile = new File(indexable.getPath());
			Pattern pattern = getPattern(indexable.getExcludedPattern());
			if (isExcluded(baseFile, pattern)) {
				logger.warn("Base directory excluded : " + baseFile);
				return null;
			}
			if (baseFile.isDirectory()) {
				handleFolder(indexContext, indexable, baseFile, pattern);
			} else {
				handleFile(indexContext, indexable, baseFile);
			}
		} catch (Exception e) {
			logger.error("Exception indexing the file share : " + indexable, e);
		}
		return null;
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
	protected void handleFolder(final IndexContext indexContext, IndexableFileSystem indexableFileSystem, File folder,
			Pattern excludedPattern) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (isExcluded(file, excludedPattern)) {
					continue;
				}
				logger.debug("Visiting file : " + file);
				if (file.isDirectory()) {
					handleFolder(indexContext, indexableFileSystem, file, excludedPattern);
				} else {
					handleFile(indexContext, indexableFileSystem, file);
				}
			}
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
	protected void handleFile(final IndexContext indexContext, IndexableFileSystem indexableFileSystem, File file) {
		try {
			Document document = new Document();
			indexableFileSystem.setCurrentFile(file);

			// TODO - this can be very large so we have to use a reader if necessary
			InputStream inputStream = new ByteArrayInputStream(FileUtilities.getContents(file).toByteArray());

			byte[] bytes = new byte[1024];
			if (inputStream.markSupported()) {
				inputStream.mark(Integer.MAX_VALUE);
				inputStream.read(bytes);
				inputStream.reset();
			}

			IParser parser = ParserProvider.getParser(file.getName(), bytes);
			OutputStream parsedOutputStream = parser.parse(inputStream, new ByteArrayOutputStream());

			Store store = indexableFileSystem.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexableFileSystem.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexableFileSystem.isVectored() ? TermVector.YES : TermVector.NO;

			// The path
			IndexManager.addStringField(indexableFileSystem.getPathFieldName(), file.getAbsolutePath(), document, store, analyzed,
					termVector);
			// The name
			IndexManager.addStringField(indexableFileSystem.getNameFieldName(), file.getName(), document, store, analyzed, termVector);
			// Last modified
			IndexManager.addStringField(indexableFileSystem.getLastModifiedFieldName(), Long.toString(file.lastModified()), document,
					store, analyzed, termVector);
			// Length
			IndexManager.addStringField(indexableFileSystem.getLengthFieldName(), Long.toString(file.length()), document, store, analyzed,
					termVector);
			// Content
			IndexManager.addStringField(indexableFileSystem.getContentFieldName(), parsedOutputStream.toString(), document, store,
					analyzed, termVector);
			// And to the index
			indexContext.getIndex().getIndexWriter().addDocument(document);
		} catch (Exception e) {
			logger.error("Exception occured while trying to index the file " + file.getAbsolutePath(), e);
		}
	}

	protected Pattern getPattern(String pattern) {
		return Pattern.compile(pattern);
	}

	protected boolean isExcluded(File file, Pattern pattern) {
		// If it does not exist, we can't read it or directory excluded with the pattern
		return file == null || !file.exists() || !file.canRead() || pattern.matcher(file.getName()).matches();
	}

	protected boolean isHandled(IndexContext indexContext, IndexableFileSystem indexableFileSystem) {
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		boolean isHandled = clusterManager.isHandled(indexableFileSystem.getName(), indexContext.getIndexName());
		return isHandled;
	}

}
