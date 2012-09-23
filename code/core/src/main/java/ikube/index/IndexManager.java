package ikube.index;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * This class opens and closes the Lucene index writer. There are also methods that get the path to the index directory based on the path in
 * the index context. This class also has methods that add fields to a document, either directly of via a file reader and writer.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public final class IndexManager {

	private static final Logger LOGGER = Logger.getLogger(IndexManager.class);

	/**
	 * Singularity.
	 */
	private IndexManager() {
		// Documented
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
	public static synchronized IndexWriter openIndexWriter(final IndexContext<?> indexContext, final long time, final String ip) {
		boolean delete = Boolean.FALSE;
		boolean success = Boolean.FALSE;
		File indexDirectory = null;
		IndexWriter indexWriter = null;
		try {
			String indexDirectoryPath = getIndexDirectory(indexContext, time, ip);
			indexDirectory = FileUtilities.getFile(indexDirectoryPath, Boolean.TRUE);
			indexDirectory.setReadable(true);
			indexDirectory.setWritable(true, false);
			LOGGER.info(Logging.getString("Index directory time : ", time, "date : ", new Date(time), "writing index to directory ",
					indexDirectoryPath));
			indexWriter = openIndexWriter(indexContext, indexDirectory, Boolean.TRUE);
			success = Boolean.TRUE;
		} catch (CorruptIndexException e) {
			LOGGER.error("We expected a new index and got a corrupt one.", e);
			LOGGER.warn("Didn't initialise the index writer. Will try to delete the index directory.");
			delete = Boolean.TRUE;
		} catch (LockObtainFailedException e) {
			LOGGER.error("Failed to obtain the lock on the directory. Check the file system permissions or failed indexing jobs, "
					+ "there will be a lock file in one of the index directories.", e);
		} catch (IOException e) {
			LOGGER.error("IO exception detected opening the writer", e);
		} catch (Exception e) {
			LOGGER.error("Unexpected exception detected while initializing the IndexWriter", e);
		} finally {
			if (!success) {
				closeIndexWriter(indexWriter);
				indexWriter = null;
			}
			if (delete && indexDirectory != null && indexDirectory.exists()) {
				FileUtilities.deleteFile(indexDirectory, 1);
			}
			IndexManager.class.notifyAll();
		}
		return indexWriter;
	}

	/**
	 * This method will open the index writer using the index context and a directory.
	 * 
	 * @param indexContext
	 *            the index context for the parameters for the index writer like compound file and buffer size
	 * @param indexDirectory
	 *            the directory to open the index in
	 * @param create
	 *            whether to create the index or open on an existing index
	 * @return the index writer open on the specified directory
	 * @throws Exception
	 */
	public static synchronized IndexWriter openIndexWriter(IndexContext<?> indexContext, File indexDirectory, boolean create)
			throws Exception {
		Directory directory = FSDirectory.open(indexDirectory);
		IndexWriter indexWriter = new IndexWriter(directory, IConstants.ANALYZER, create, MaxFieldLength.UNLIMITED);
		indexWriter.setUseCompoundFile(indexContext.isCompoundFile());
		indexWriter.setMaxBufferedDocs(indexContext.getBufferedDocs());
		indexWriter.setMaxFieldLength(indexContext.getMaxFieldLength());
		indexWriter.setMergeFactor(indexContext.getMergeFactor());
		indexWriter.setRAMBufferSizeMB(indexContext.getBufferSize());
		return indexWriter;
	}

	/**
	 * This method will close the index writer, provided it is not null. Also the index writer in the context will be removed.
	 * 
	 * @param indexContext the index context to close the writer for
	 */
	public static synchronized void closeIndexWriter(final IndexContext<?> indexContext) {
		try {
			if (indexContext != null && indexContext.getIndexWriter() != null) {
				IndexWriter indexWriter = indexContext.getIndexWriter();
				LOGGER.info("Optimizing and closing the index : " + indexContext.getIndexName() + ", snapshot : "
						+ indexContext.getLastSnapshot());
				closeIndexWriter(indexWriter);
				LOGGER.info("Index optimized and closed : " + indexWriter + ", " + indexContext.getIndexName() + ", snapshot : "
						+ indexContext.getLastSnapshot());
			}
		} finally {
			IndexManager.class.notifyAll();
		}
	}

	/**
	 * This method will close the index writer and optimize it too.
	 * 
	 * @param indexWriter
	 *            the index writer to close and optimize
	 */
	public static void closeIndexWriter(final IndexWriter indexWriter) {
		if (indexWriter == null) {
			LOGGER.warn("Tried to close a null writer : ");
			return;
		}
		Directory directory = null;
		try {
			// We'll sleep a few seconds to give the other threads a chance
			// to release themselves from work and more importantly the index files
			ThreadUtilities.sleep(3000);
			directory = indexWriter.getDirectory();
			indexWriter.prepareCommit();
			indexWriter.commit();
			indexWriter.maybeMerge();
			indexWriter.optimize(5, Boolean.TRUE);
		} catch (NullPointerException e) {
			LOGGER.error("Null pointer, in the index writer : " + indexWriter);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Stack trace : ", e);
			}
		} catch (CorruptIndexException e) {
			LOGGER.error("Corrput index : " + indexWriter, e);
		} catch (IOException e) {
			// TODO Open the index again and try the optimize a few times?
			LOGGER.error("IO optimising the index : " + indexWriter, e);
		} catch (Exception e) {
			LOGGER.error("General exception comitting the index : " + indexWriter, e);
		}
		try {
			indexWriter.close(Boolean.TRUE);
		} catch (Exception e) {
			LOGGER.error("Exception closing the index writer : " + indexWriter, e);
		}
		try {
			if (directory != null) {
				int retry = 0;
				int maxRetry = 10;
				// We have to wait for the merges and the close
				while (IndexWriter.isLocked(directory) && retry++ < maxRetry) {
					if (IndexWriter.isLocked(directory)) {
						LOGGER.warn("Index still locked : " + directory);
						ThreadUtilities.sleep(10000);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception releasing the lock on the index writer : " + indexWriter, e);
		}
	}

	/**
	 * This method will get the path to the index directory that will be created, based on the path in the context, the time and the ip of
	 * the machine.
	 * 
	 * @param indexContext
	 *            the context to use for the path to the indexes for the context
	 * @param time
	 *            the time for the upper directory name
	 * @param ip
	 *            the ip for the index directory name
	 * @return the full path to the
	 */
	public static String getIndexDirectory(final IndexContext<?> indexContext, final long time, final String ip) {
		StringBuilder builder = new StringBuilder();
		builder.append(IndexManager.getIndexDirectoryPath(indexContext));
		builder.append(IConstants.SEP);
		builder.append(time); // Time
		builder.append(IConstants.SEP);
		builder.append(ip); // Ip
		return builder.toString();
	}

	public static String getIndexDirectoryPath(final IndexContext<?> indexContext) {
		return getIndexDirectoryPath(indexContext, indexContext.getIndexDirectoryPath());
	}

	public static String getIndexDirectoryPathBackup(final IndexContext<?> indexContext) {
		return getIndexDirectoryPath(indexContext, indexContext.getIndexDirectoryPathBackup());
	}

	private static String getIndexDirectoryPath(IndexContext<?> indexContext, String indexDirectory) {
		StringBuilder builder = new StringBuilder();
		builder.append(new File(indexDirectory).getAbsolutePath()); // Path
		builder.append(File.separator);
		builder.append(indexContext.getIndexName()); // Index name
		return FileUtilities.cleanFilePath(builder.toString());
	}

	public static void addStringField(final String fieldName, final String fieldContent, final Document document, final Store store,
			final Index analyzed, final TermVector termVector) {
		if (fieldName == null || fieldContent == null) {
			LOGGER.warn("Field and content can't be null : " + fieldName + ", " + fieldContent);
			return;
		}
		Field field = document.getField(fieldName);
		if (field == null) {
			field = new Field(fieldName, fieldContent, store, analyzed, termVector);
			document.add(field);
		} else {
			String fieldValue = field.stringValue() != null ? field.stringValue() : "";
			StringBuilder builder = new StringBuilder(fieldValue).append(' ').append(fieldContent);
			field.setValue(builder.toString());
		}
	}

	public static void addNumericField(final String fieldName, final String fieldContent, final Document document, final Store store) {
		NumericField field = new NumericField(fieldName, 10, store, true);
		field.setDoubleValue(Double.parseDouble(fieldContent));
		document.add(field);
	}

	public static void addReaderField(final String fieldName, final Document document, final Store store, final TermVector termVector,
			final Reader reader) throws Exception {
		if (fieldName == null || reader == null) {
			LOGGER.warn("Field and reader can't be null : " + fieldName + ", " + reader);
			return;
		}
		Field field = document.getField(fieldName);
		if (field == null) {
			field = new Field(fieldName, reader, termVector);
			document.add(field);
		} else {
			Reader fieldReader = field.readerValue();

			if (fieldReader == null) {
				fieldReader = new StringReader(field.stringValue());
			}

			Reader finalReader = null;
			Writer writer = null;
			try {
				File tempFile = File.createTempFile(Long.toString(System.nanoTime()), IConstants.READER_FILE_SUFFIX);
				writer = new FileWriter(tempFile, false);
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
				finalReader = new FileReader(tempFile);
				// This is a string field, and could be stored so we check that
				if (store.isStored()) {
					// Remove the field and add it again
					document.removeField(fieldName);
					field = new Field(fieldName, finalReader, termVector);
					document.add(field);
				} else {
					field.setValue(finalReader);
				}
			} catch (Exception e) {
				LOGGER.error("Exception writing the field value with the file writer : ", e);
			} finally {
				FileUtilities.close(writer);
				FileUtilities.close(finalReader);
				FileUtilities.close(fieldReader);
			}
		}
	}

}