package ikube.action.index.handler.filesystem;

import ikube.action.index.IndexManager;
import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystemLog;
import ikube.toolkit.FileUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

import org.apache.lucene.document.Document;

/**
 * This handler is a custom handler for the BPost. It will index log files in a particular directory, and unlike the {@link IndexableFilesystemHandler} which
 * indexes files file by file, this handler will index log files line by line.
 * 
 * @author Michael Couck
 * @since 08.02.2011
 * @version 01.00
 */
public class IndexableFilesystemLogHandler extends IndexableHandler<IndexableFileSystemLog> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ForkJoinTask<?> handleIndexableForked(final IndexContext<?> indexContext, final IndexableFileSystemLog indexable) throws Exception {
		IResourceProvider<File> fileSystemResourceProvider = new IResourceProvider<File>() {

			private List<File> resources;

			{
				String directoryPath = indexable.getPath();
				File directory = FileUtilities.getFile(directoryPath, Boolean.TRUE);
				resources = new ArrayList<>();
				getLogFiles(directory);
			}

			@Override
			public File getResource() {
				if (resources.isEmpty()) {
					return null;
				}
				return resources.remove(0);
			}

			@Override
			public void setResources(final List<File> resources) {
				this.resources = resources;
			}

			private void getLogFiles(final File directory) {
				File[] logFiles = directory.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						// We need the directories of the log files below this point
						logger.info("Looking for logs files : " + pathname);
						return pathname.getName().contains("log") || pathname.isDirectory();
					}
				});
				if (logFiles != null) {
					for (File logFile : logFiles) {
						if (logFile.isDirectory()) {
							getLogFiles(logFile);
							continue;
						}
						logger.info("Adding logs file : " + logFile);
						resources.add(logFile);
					}
				}
			}

		};
		return getRecursiveAction(indexContext, indexable, fileSystemResourceProvider);
	}

	@Override
	protected List<?> handleResource(final IndexContext<?> indexContext, final IndexableFileSystemLog indexableFileSystemLog, final Object resource) {
		logger.info("Handling resource : " + resource + ", thread : " + Thread.currentThread().hashCode());
		handleFile(indexContext, indexableFileSystemLog, (File) resource);
		return null;
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
				IndexManager.addStringField(fileFieldName, logFile.getName(), indexableFileSystem, document);
				IndexManager.addStringField(pathFieldName, logFile.getAbsolutePath(), indexableFileSystem, document);
				IndexManager.addStringField(lineFieldName, stringLineNumber, indexableFileSystem, document);
				IndexManager.addStringField(contentFieldName, line, indexableFileSystem, document);
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