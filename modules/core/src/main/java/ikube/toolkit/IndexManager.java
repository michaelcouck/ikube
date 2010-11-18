package ikube.toolkit;

import ikube.IConstants;
import ikube.logging.Logging;
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

	public static synchronized IndexWriter openIndexWriter(String ip, IndexContext indexContext, long time) {
		try {
			StringBuilder builder = new StringBuilder();

			builder.append(indexContext.getIndexDirectoryPath());
			builder.append(File.separator);
			builder.append(time);
			builder.append(File.separator);
			builder.append(ip);
			builder.append(File.separator);
			builder.append(indexContext.getName());

			File indexDirectory = FileUtilities.getFile(builder.toString(), Boolean.TRUE);
			LOGGER.info(Logging.getString("Index directory time : ", time, ", date : ", new Date(time), ", writing index to directory ",
					indexDirectory));
			IndexWriter indexWriter = null;
			try {
				Directory directory = FSDirectory.open(indexDirectory);
				indexWriter = new IndexWriter(directory, IConstants.ANALYZER, true, MaxFieldLength.UNLIMITED);
				indexWriter.setUseCompoundFile(indexContext.isCompoundFile());
				indexWriter.setMaxBufferedDocs(indexContext.getBufferedDocs());
				indexWriter.setMaxFieldLength(indexContext.getMaxFieldLength());
				indexWriter.setMergeFactor(indexContext.getMergeFactor());
				indexWriter.setRAMBufferSizeMB(indexContext.getBufferSize());
				indexContext.setIndexWriter(indexWriter);
			} catch (CorruptIndexException e) {
				LOGGER.error("We expected a new index and got a corrupt one.", e);
				LOGGER.warn("Didn't initialise the index writer. Will try to delete the index directory.");
				closeIndexWriter(indexContext);
				FileUtilities.deleteFile(indexDirectory, 3);
			} catch (LockObtainFailedException e) {
				LOGGER.error("Failed to obtain the lock on the directory. Check the file system permissions.", e);
			} catch (IOException e) {
				LOGGER.error("IO exception detected opening the writer", e);
			} catch (Exception e) {
				LOGGER.error("Unexpected exception detected while initializing the IndexWriter", e);
			}
			return indexContext.getIndexWriter();
		} finally {
			IndexManager.class.notifyAll();
		}
	}

	public static synchronized void closeIndexWriter(IndexContext indexContext) {
		try {
			if (indexContext != null && indexContext.getIndexWriter() != null) {
				IndexWriter indexWriter = indexContext.getIndexWriter();
				closeIndexWriter(indexWriter);
				indexContext.setIndexWriter(null);
			}
		} finally {
			IndexManager.class.notifyAll();
		}
	}

	private static void closeIndexWriter(IndexWriter indexWriter) {
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
	}

}