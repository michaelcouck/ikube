package ikube.index;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

/**
 * This class opens and closes the Lucene index writer.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public final class IndexManager {

	private static final Logger LOGGER = Logger.getLogger(IndexManager.class);

	private IndexManager() {
	}

	/**
	 * This method opens a Lucene index writer, and if successful sets it in the index context where the handlers can access it and add
	 * documents to it during the index. The index writer is opened on a directory that will be the index path on the file system, the name
	 * of the index, then the
	 * 
	 * @param ip
	 *            the ip address of this machine
	 * @param indexContext
	 *            the index context to open the writer for
	 * @param time
	 *            the time stamp for the index directory. This can come from the system time but it can also come from another server. When
	 *            an index is started the server will publish the time it started the index. In this way we can check the timestamp for the
	 *            index, and if it is set then we use the cluster timestamp. As a result we write the index in the same 'timestamp'
	 *            directory
	 * @return the index writer opened for this index context or null if there was any exception opening the index
	 */
	public static synchronized IndexWriter openIndexWriter(final IndexContext indexContext, final long time, final String ip) {
		boolean delete = Boolean.FALSE;
		boolean exception = Boolean.FALSE;
		File indexDirectory = null;
		IndexWriter indexWriter = null;
		try {
			try {
				String indexDirectoryPath = getIndexDirectory(indexContext, time, ip);
				indexDirectory = FileUtilities.getFile(indexDirectoryPath, Boolean.TRUE);
				LOGGER.info(Logging.getString("Index directory time : ", time, "date : ", new Date(time), "writing index to directory ",
						indexDirectory.getAbsolutePath()));
				indexWriter = openIndexWriter(indexContext, indexDirectory);
				indexContext.getIndex().setIndexWriter(indexWriter);
			} catch (CorruptIndexException e) {
				LOGGER.error("We expected a new index and got a corrupt one.", e);
				LOGGER.warn("Didn't initialise the index writer. Will try to delete the index directory.");
				closeIndexWriter(indexContext);
				exception = Boolean.TRUE;
				delete = Boolean.TRUE;
			} catch (LockObtainFailedException e) {
				LOGGER.error("Failed to obtain the LOCK on the directory. Check the file system permissions.", e);
				exception = Boolean.TRUE;
			} catch (IOException e) {
				LOGGER.error("IO exception detected opening the writer", e);
				exception = Boolean.TRUE;
			} catch (Exception e) {
				LOGGER.error("Unexpected exception detected while initializing the IndexWriter", e);
				exception = Boolean.TRUE;
			}
			return indexWriter;
		} finally {
			if (exception) {
				closeIndexWriter(indexWriter);
			}
			if (delete && indexDirectory != null && indexDirectory.exists()) {
				FileUtilities.deleteFile(indexDirectory, 1);
			}
			IndexManager.class.notifyAll();
		}
	}

	public static synchronized IndexWriter openIndexWriter(IndexContext indexContext, File indexDirectory) throws Exception {
		Directory directory = null;
		if (indexContext.getInMemory()) {
			LOGGER.info("Index in memory : ");
			directory = new RAMDirectory();
		} else {
			directory = FSDirectory.open(indexDirectory);
		}
		indexContext.getIndex().setDirectory(directory);
		IndexWriter indexWriter = new IndexWriter(directory, IConstants.ANALYZER, true, MaxFieldLength.UNLIMITED);
		indexWriter.setUseCompoundFile(indexContext.isCompoundFile());
		indexWriter.setMaxBufferedDocs(indexContext.getBufferedDocs());
		indexWriter.setMaxFieldLength(indexContext.getMaxFieldLength());
		indexWriter.setMergeFactor(indexContext.getMergeFactor());
		indexWriter.setRAMBufferSizeMB(indexContext.getBufferSize());
		return indexWriter;
	}

	public static synchronized void closeIndexWriter(final IndexContext indexContext) {
		try {
			if (indexContext != null && indexContext.getIndex().getIndexWriter() != null) {
				IndexWriter indexWriter = indexContext.getIndex().getIndexWriter();
				closeIndexWriter(indexWriter);
				indexContext.getIndex().setIndexWriter(null);
			}
		} finally {
			IndexManager.class.notifyAll();
		}
	}

	public static void closeIndexWriter(final IndexWriter indexWriter) {
		Directory directory = indexWriter.getDirectory();
		LOGGER.info("Optimizing and closing the index : ");
		try {
			indexWriter.commit();
			indexWriter.optimize(Boolean.TRUE);
		} catch (NullPointerException e) {
			LOGGER.error("Null pointer, in the index writer : ");
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Stack trace : ", e);
			}
		} catch (CorruptIndexException e) {
			LOGGER.error("Corrput index : ", e);
		} catch (IOException e) {
			LOGGER.error("IO optimising the index : ", e);
		} catch (Exception e) {
			LOGGER.error("General exception comitting the index : ", e);
		}
		try {
			indexWriter.close(Boolean.TRUE);
		} catch (Exception e) {
			LOGGER.error("Exception closing the index writer : ", e);
		}
		try {
			if (directory != null) {
				if (IndexWriter.isLocked(directory)) {
					IndexWriter.unlock(directory);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception releasing the lock on the index writer : ", e);
		}
		LOGGER.info("Index optimized and closed : ");
	}

	public static String getIndexDirectory(final IndexContext indexContext, final long time, final String ip) {
		StringBuilder builder = new StringBuilder();
		builder.append(IndexManager.getIndexDirectoryPath(indexContext));
		builder.append(File.separator);
		builder.append(time); // Time
		builder.append(File.separator);
		builder.append(ip); // Ip
		return builder.toString();
	}

	public static String getIndexDirectoryPath(final IndexContext indexContext) {
		return getIndexDirectoryPath(indexContext, indexContext.getIndexDirectoryPath());
	}

	public static String getIndexDirectoryPathBackup(final IndexContext indexContext) {
		return getIndexDirectoryPath(indexContext, indexContext.getIndexDirectoryPathBackup());
	}

	private static String getIndexDirectoryPath(IndexContext indexContext, String indexDirectory) {
		StringBuilder builder = new StringBuilder();
		builder.append(indexDirectory); // Path
		builder.append(File.separator);
		builder.append(indexContext.getIndexName()); // Index name
		String indexDirectoryPath = StringUtils.replace(builder.toString(), "/./", "/");
		indexDirectoryPath = StringUtils.replace(indexDirectoryPath, "\\.\\", "/");
		indexDirectoryPath = StringUtils.replace(indexDirectoryPath, "\\", "/");
		return indexDirectoryPath;
	}

	public static void addStringField(final String fieldName, final String fieldContent, final Document document, final Store store,
			final Index analyzed, final TermVector termVector) {
		Field field = document.getField(fieldName);
		if (field == null) {
			field = new Field(fieldName, fieldContent, store, analyzed, termVector);
			document.add(field);
		} else {
			String fieldValue = field.stringValue();
			StringBuilder builder = new StringBuilder(fieldValue).append(' ').append(fieldContent);
			field.setValue(builder.toString());
		}
	}

	public static void addReaderField(final String fieldName, final Document document, final Store store, final TermVector termVector,
			final Reader reader) throws Exception {
		Field field = document.getField(fieldName);
		if (field == null) {
			field = new Field(fieldName, reader, termVector);
			document.add(field);
		} else {
			Reader fieldReader = field.readerValue();
			if (fieldReader == null) {
				fieldReader = new StringReader(field.stringValue());
			}
			File tempFile = File.createTempFile(Long.toString(System.nanoTime()), IConstants.READER_FILE_SUFFIX);
			Writer writer = new FileWriter(tempFile, false);
			char[] chars = new char[1024];
			int read = fieldReader.read(chars);
			while (read > -1) {
				writer.write(chars, 0, read);
				read = fieldReader.read(chars);
			}
			read = reader.read(chars);
			while (read > -1) {
				writer.write(chars, 0, read);
				read = reader.read(chars);
			}
			Reader finalReader = new FileReader(tempFile);
			// This is a string field, and could be stored so we check that
			if (store.isStored()) {
				// Remove the field and add it again
				document.removeField(fieldName);
				field = new Field(fieldName, finalReader, termVector);
				document.add(field);
			} else {
				field.setValue(finalReader);
			}
		}
	}

}