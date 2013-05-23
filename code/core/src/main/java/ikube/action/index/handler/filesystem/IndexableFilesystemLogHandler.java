package ikube.action.index.handler.filesystem;

import ikube.action.index.IndexManager;
import ikube.action.index.handler.IndexableHandler;
import ikube.action.index.handler.ResourceHandlerBase;
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
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This handler is a custom handler for the BPost. It will index log files in a particular directory, and unlike the
 * {@link IndexableFilesystemHandler} which indexes files file by file, this handler will index log files line by line.
 * 
 * @author Michael Couck
 * @since 08.02.2011
 * @version 01.00
 */
public class IndexableFilesystemLogHandler extends IndexableHandler<IndexableFileSystemLog> {

	@Autowired
	private ResourceHandlerBase<IndexableFileSystemLog> resourceHandler;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handleIndexable(final IndexContext<?> indexContext, final IndexableFileSystemLog indexable) throws Exception {
		List<Future<?>> futures = new ArrayList<Future<?>>();
		try {
			final IndexableFileSystemLog indexableFileSystem = (IndexableFileSystemLog) SerializationUtilities.clone(indexable);
			Runnable runnable = new Runnable() {
				public void run() {
					String directoryPath = indexableFileSystem.getPath();
					File directory = FileUtilities.getFile(directoryPath, Boolean.TRUE);
					indexLogs(directory);
				}

				private void indexLogs(final File directory) {
					File[] logFiles = directory.listFiles(new FileFilter() {
						@Override
						public boolean accept(File pathname) {
							// We need the directories of the log files below this point
							return pathname.getName().contains("log") || pathname.isDirectory();
						}
					});
					if (logFiles != null) {
						for (File logFile : logFiles) {
							if (logFile.isDirectory()) {
								indexLogs(logFile);
								continue;
							}
							logger.info("Indexing file : " + logFile);
							handleFile(indexContext, indexableFileSystem, logFile);
						}
					}
				}
			};
			futures.add(ThreadUtilities.submit(indexContext.getIndexName(), runnable));
		} catch (Exception e) {
			handleException(indexable, e);
		}
		return futures;
	}

	/**
	 * This method will read a log file line by line and add a document to the Lucene index for each line.
	 * 
	 * @param indexContext the context for this log file set
	 * @param indexableFileSystem the log file, i.e. the directory where the log files are on the network
	 * @param logFile and the individual log file that we will index
	 */
	private void handleFile(final IndexContext<?> indexContext, final IndexableFileSystemLog indexableFileSystem, final File logFile) {
		Reader reader = null;
		BufferedReader bufferedReader = null;
		Store store = indexableFileSystem.isStored() ? Store.YES : Store.NO;
		Index analyzed = indexableFileSystem.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
		TermVector termVector = indexableFileSystem.isVectored() ? TermVector.YES : TermVector.NO;
		int lineNumber = 1;
		try {
			reader = new FileReader(logFile);
			bufferedReader = new BufferedReader(reader);
			String line = bufferedReader.readLine();
			while (line != null) {
				Document document = new Document();
				String fileFieldName = indexableFileSystem.getFileFieldName();
				String pathFieldName = indexableFileSystem.getPathFieldName();
				String lineFieldName = indexableFileSystem.getLineFieldName();
				String stringLineNumber = Integer.toString(lineNumber);
				String contentFieldName = indexableFileSystem.getContentFieldName();
				IndexManager.addStringField(fileFieldName, logFile.getName(), document, Store.YES, Index.ANALYZED, TermVector.YES);
				IndexManager.addStringField(pathFieldName, logFile.getAbsolutePath(), document, Store.YES, Index.ANALYZED, TermVector.YES);
				IndexManager.addStringField(lineFieldName, stringLineNumber, document, Store.YES, Index.ANALYZED, TermVector.YES);
				IndexManager.addStringField(contentFieldName, line, document, store, analyzed, termVector);
				resourceHandler.handleResource(indexContext, indexableFileSystem, document, null);
				line = bufferedReader.readLine();
				lineNumber++;
				Thread.sleep(indexContext.getThrottle());
			}
		} catch (Exception e) {
			handleException(indexableFileSystem, e);
		} finally {
			FileUtilities.close(bufferedReader);
			FileUtilities.close(reader);
		}
		logger.info("Indexed lines : " + lineNumber);
	}

}