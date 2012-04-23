package ikube.index.handler.filesystem;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Couck
 * @since 20.04.2012
 * @version 01.00
 */
public class IndexableFilesystemWikiHandler extends IndexableHandler<IndexableFileSystem> {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexableFilesystemWikiHandler.class);

	/** This is the start and end tags for the xml data, one per page essentially. */
	private static final String PAGE_START = "<revision>";
	private static final String PAGE_FINISH = "</revision>";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handle(final IndexContext<?> indexContext, final IndexableFileSystem indexable) throws Exception {
		List<Future<?>> futures = new ArrayList<Future<?>>();
		try {
			Runnable runnable = new Runnable() {
				public void run() {
					String filePath = indexable.getPath();
					File file = FileUtilities.getFile(filePath, Boolean.FALSE);
					handleFile(indexContext, indexable, file);
				}
			};
			futures.add(ThreadUtilities.submit(runnable));
		} catch (Exception e) {
			logger.error("Exception starting the file system indexer threads : ", e);
		}
		return futures;
	}

	/**
	 * This method will take a Bzip2 file, read it, unpack it gradually. Parse the contents, extracting the revision data for the Wiki which
	 * is in <revision> tags. Each revision will then be added to the index as a unique document.
	 * 
	 * @param indexContext the index context for the index
	 * @param indexableFileSystem the fiel system object, i.e. the path to the bzip file
	 * @param file the Bzip2 file with the Wiki data in it
	 */
	private void handleFile(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final File file) {
		// Get the wiki history file
		FileInputStream fileInputStream = null;
		BZip2CompressorInputStream bZip2CompressorInputStream = null;
		try {
			int read = -1;
			fileInputStream = new FileInputStream(file);
			bZip2CompressorInputStream = new BZip2CompressorInputStream(fileInputStream);
			byte[] bytes = new byte[1024 * 1024];
			StringBuilder stringBuilder = new StringBuilder();
			// Read a chunk
			while ((read = bZip2CompressorInputStream.read(bytes)) > -1) {
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
					// Add the documents to the index
					handleRevision(indexContext, indexableFileSystem, content);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception reading and uncompressing the zip file : " + file, e);
		} finally {
			FileUtilities.close(fileInputStream);
			FileUtilities.close(bZip2CompressorInputStream);
		}
	}

	/**
	 * This method will read a log file line by line and add a document to the Lucene index for each line.
	 * 
	 * @param indexContext the context for this log file set
	 * @param indexableFileSystem the log file, i.e. the directory where the log files are on the network
	 * @param logFile and the individual log file that we will index
	 */
	private void handleRevision(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final String content) {
		Store store = indexableFileSystem.isStored() ? Store.YES : Store.NO;
		Index analyzed = indexableFileSystem.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
		TermVector termVector = indexableFileSystem.isVectored() ? TermVector.YES : TermVector.NO;
		try {
			Document document = new Document();
			String pathFieldName = indexableFileSystem.getPathFieldName();
			String contentFieldName = indexableFileSystem.getContentFieldName();
			IndexManager.addStringField(pathFieldName, indexableFileSystem.getPath(), document, Store.YES, Index.ANALYZED, TermVector.YES);
			IndexManager.addStringField(contentFieldName, content, document, store, analyzed, termVector);
			addDocument(indexContext, indexableFileSystem, document);
		} catch (Exception e) {
			logger.error("Exception reading log file : ", e);
		}
	}

}