package ikube.index.handler.filesystem;

import ikube.index.IndexManager;
import ikube.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystemLog;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * @author Michael Couck
 * @since 08.02.2011
 * @version 01.00
 */
public class IndexableFilesystemLogHandler extends IndexableHandler<IndexableFileSystemLog> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handle(final IndexContext<?> indexContext, final IndexableFileSystemLog indexable) throws Exception {
		List<Future<?>> futures = new ArrayList<Future<?>>();
		try {
			final IndexableFileSystemLog indexableFileSystem = (IndexableFileSystemLog) SerializationUtilities.clone(indexable);
			Runnable runnable = new Runnable() {
				public void run() {
					String directoryPath = indexableFileSystem.getPath();
					File directory = FileUtilities.getFile(directoryPath, Boolean.TRUE);
					File[] logFiles = directory.listFiles(new FileFilter() {
						@Override
						public boolean accept(File pathname) {
							return pathname.getName().contains("log");
						}
					});
					for (File logFile : logFiles) {
						logger.info("Indexing file : " + logFile);
						readFile(indexContext, indexableFileSystem, logFile);
					}
				}
			};
			Future<?> future = ThreadUtilities.submit(runnable);
			futures.add(future);
		} catch (Exception e) {
			logger.error("Exception starting the file system indexer threads : ", e);
		}
		return futures;
	}

	private void readFile(final IndexContext<?> indexContext, final IndexableFileSystemLog indexableFileSystem, final File logFile) {
		Reader reader = null;
		BufferedReader bufferedReader = null;
		Store store = indexableFileSystem.isStored() ? Store.YES : Store.NO;
		Index analyzed = indexableFileSystem.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
		TermVector termVector = indexableFileSystem.isVectored() ? TermVector.YES : TermVector.NO;
		int lineNumber = 0;
		try {
			reader = new FileReader(logFile);
			bufferedReader = new BufferedReader(reader);
			String line = bufferedReader.readLine();
			while (line != null) {
				Document document = new Document();
				String stringLineNumber = Integer.toString(lineNumber);
				String lineFieldName = indexableFileSystem.getLineFieldName();
				String contentFieldName = indexableFileSystem.getContentFieldName();
				IndexManager.addStringField(lineFieldName, stringLineNumber, document, store, analyzed, termVector);
				IndexManager.addStringField(contentFieldName, line, document, Store.YES, Index.ANALYZED, TermVector.YES);
				indexContext.getIndex().getIndexWriter().addDocument(document);
				line = bufferedReader.readLine();
				lineNumber++;
			}
		} catch (Exception e) {
			logger.error("Exception reading log file : ", e);
		} finally {
			FileUtilities.close(bufferedReader);
			FileUtilities.close(reader);
		}
		logger.info("Indexed lines : " + lineNumber);
	}

}