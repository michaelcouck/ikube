package ikube.index.handler.filesystem;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.model.IndexableFileSystemWiki;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * @author Michael Couck
 * @since 20.04.2012
 * @version 01.00
 */
public class IndexableFilesystemWikiHandler extends IndexableHandler<IndexableFileSystemWiki> {

	/** This is the start and end tags for the xml data, one per page essentially. */
	private static final String PAGE_START = "<revision>";
	private static final String PAGE_FINISH = "</revision>";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handle(final IndexContext<?> indexContext, final IndexableFileSystemWiki indexable) throws Exception {
		List<Future<?>> futures = new ArrayList<Future<?>>();
		String filePath = indexable.getPath();
		File directory = FileUtilities.getFile(filePath, Boolean.TRUE);
		final File[] bZip2Files = FileUtilities.findFiles(directory, new String[] { "bz2" });
		final Iterator<File> iterator = new ArrayList<File>(Arrays.asList(bZip2Files)).iterator();
		for (int i = 0; i < getThreads(); i++) {
			Runnable runnable = new Runnable() {
				public void run() {
					synchronized(iterator) {
						while (iterator.hasNext()) {
							File bZip2File = iterator.next();
							logger.info("Indexing compressed file : " + bZip2File);
							handleFile(indexContext, indexable, bZip2File);
						}
					}
				}
			};
			Future<?> future = ThreadUtilities.submit(runnable);
			futures.add(future);
		}
		return futures;
	}

	/**
	 * This method will take a Bzip2 file, read it, decompress it gradually. Parse the contents, extracting the revision data for the Wiki
	 * which is in <revision> tags. Each revision will then be added to the index as a unique document.
	 * 
	 * @param indexContext
	 *            the index context for the index
	 * @param indexableFileSystem
	 *            the file system object, i.e. the path to the bzip file
	 * @param file
	 *            the Bzip2 file with the Wiki data in it
	 */
	protected void handleFile(final IndexContext<?> indexContext, final IndexableFileSystemWiki indexableFileSystem, final File file) {
		// Get the wiki history file
		FileInputStream fileInputStream = null;
		BZip2CompressorInputStream bZip2CompressorInputStream = null;
		try {
			long start = System.currentTimeMillis();
			int read = -1;
			fileInputStream = new FileInputStream(file);
			bZip2CompressorInputStream = new BZip2CompressorInputStream(fileInputStream);
			byte[] bytes = new byte[1024 * 1024 * 10];
			StringBuilder stringBuilder = new StringBuilder();
			int counter = 0;
			// Read a chunk
			while ((read = bZip2CompressorInputStream.read(bytes)) > -1 && counter < indexableFileSystem.getMaxRevisions()) {
				String string = new String(bytes, 0, read, Charset.forName(IConstants.ENCODING));
				stringBuilder.append(string);
				// Parse the <revision> tags
				while (true) {
					int startOffset = stringBuilder.indexOf(PAGE_START);
					int endOffset = stringBuilder.indexOf(PAGE_FINISH);
					if (startOffset == -1 || endOffset == -1) {
						break;
					}
					if (endOffset <= startOffset) {
						startOffset = endOffset;
					}
					endOffset += PAGE_FINISH.length();
					String content = stringBuilder.substring(startOffset, endOffset);
					stringBuilder.delete(startOffset, endOffset);
					// LOGGER.info("String buffer size : " + stringBuilder.length());
					// Add the documents to the index
					handleRevision(indexContext, indexableFileSystem, content);
					counter++;
					if (counter % 10000 == 0) {
						long duration = System.currentTimeMillis() - start;
						double perSecond = counter / (duration / 1000);
						logger.info("Revisions done : " + counter + ", " + file.getName() + ", " + perSecond);
					}
				}
			}
		} catch (InterruptedException e) {
			logger.error("Coitus interruptus... : " + file, e);
			throw new RuntimeException(e);
		} catch (Exception e) {
			logger.error("Exception reading and uncompressing the zip file : " + file, e);
		} finally {
			FileUtilities.close(fileInputStream);
			FileUtilities.close(bZip2CompressorInputStream);
		}
	}

	/**
	 * This method will read a log file line by line and add a document to the Lucene index for each line.
	 * 
	 * @param indexContext
	 *            the context for this log file set
	 * @param indexableFileSystem
	 *            the log file, i.e. the directory where the log files are on the network
	 * @param logFile
	 *            and the individual log file that we will index
	 * @throws Exception
	 */
	private void handleRevision(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final String content)
			throws Exception {
		Store store = indexableFileSystem.isStored() ? Store.YES : Store.NO;
		Index analyzed = indexableFileSystem.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED_NO_NORMS;
		TermVector termVector = indexableFileSystem.isVectored() ? TermVector.YES : TermVector.NO;

		Document document = new Document();
		// String pathFieldName = indexableFileSystem.getPathFieldName();
		String contentFieldName = indexableFileSystem.getContentFieldName();
		// IndexManager.addStringField(pathFieldName, indexableFileSystem.getPath(), document, Store.YES, Index.NO, TermVector.NO);
		IndexManager.addStringField(contentFieldName, content, document, store, analyzed, termVector);
		addDocument(indexContext, indexableFileSystem, document);
	}

}