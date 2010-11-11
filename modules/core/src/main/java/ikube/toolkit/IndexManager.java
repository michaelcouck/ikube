package ikube.toolkit;

import ikube.IConstants;
import ikube.model.IndexContext;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

public class IndexManager {

	private static Logger LOGGER = Logger.getLogger(IndexManager.class);

	public static synchronized IndexWriter openIndexWriter(IndexContext indexContext, long time) {
		IndexManager.closeIndexWriter(indexContext);

		String indexDirectoryPath = indexContext.getIndexDirectoryPath();
		String serverName = indexContext.getServerName();
		String indexDirectory = new StringBuilder(indexDirectoryPath).append(File.separator).append(time).append(File.separator).append(
				serverName).toString();
		File indexDirectoryFolder = FileUtilities.getFile(indexDirectory, Boolean.TRUE);
		LOGGER.info("Index directory time : " + time + ", date : " + new Date(time) + ", writing index to directory "
				+ indexDirectoryFolder);
		try {
			Directory directory = FSDirectory.open(indexDirectoryFolder);
			IndexWriter indexWriter = new IndexWriter(directory, IConstants.ANALYZER, true, MaxFieldLength.UNLIMITED);
			indexWriter.setUseCompoundFile(indexContext.isCompoundFile());
			indexWriter.setMaxBufferedDocs(indexContext.getBufferedDocs());
			indexWriter.setMaxFieldLength(indexContext.getMaxFieldLength());
			indexWriter.setMergeFactor(indexContext.getMergeFactor());
			indexWriter.setRAMBufferSizeMB(indexContext.getBufferSize());
			indexContext.setIndexWriter(indexWriter);
		} catch (CorruptIndexException e) {
			LOGGER.error("We expected a new index and got a corrupt one.", e);
			deleteIndex(indexDirectoryFolder);
		} catch (LockObtainFailedException e) {
			LOGGER.error("Failed to obtain the lock on the directory. Check the file system permissions.", e);
		} catch (IOException e) {
			LOGGER.error("IO exception detected opening the writer", e);
		} catch (Exception e) {
			LOGGER.error("Unexpected exception detected while initializing the IndexWriter", e);
		}
		return indexContext.getIndexWriter();
	}

	public static void closeIndexWriter(IndexContext indexContext) {
		if (indexContext != null && indexContext.getIndexWriter() != null) {
			IndexWriter indexWriter = indexContext.getIndexWriter();
			Directory directory = indexWriter.getDirectory();
			try {
				indexWriter.commit();
				indexWriter.optimize();
			} catch (CorruptIndexException e) {
				LOGGER.error("Corrput index : ", e);
			} catch (IOException e) {
				LOGGER.error("IO optimising the index : ", e);
			}
			try {
				indexWriter.close(Boolean.TRUE);
			} catch (Exception e) {
				LOGGER.error("Exception closing the index writer : ", e);
			}
			try {
				IndexWriter.unlock(directory);
			} catch (Exception e) {
				LOGGER.error("Exception releasing the lock on the index writer : ", e);
			}
			indexContext.setIndexWriter(null);
		}
	}

	private static void deleteIndex(File indexDirectoryFolder) {
		if (indexDirectoryFolder != null && indexDirectoryFolder.exists() && !indexDirectoryFolder.isFile()) {
			// Something went wrong so try to delete the index directory
			LOGGER.warn("Didn't initialise the index writer. Will try to delete the index directory.");
			FileUtilities.deleteFile(indexDirectoryFolder, 3);
		}
	}

}