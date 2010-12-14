package ikube.index;

import ikube.IConstants;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

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
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexManager {

	private static Logger LOGGER = Logger.getLogger(IndexManager.class);

	public static synchronized IndexWriter openIndexWriter(String ip, IndexContext indexContext, long time) {
		try {
			StringBuilder builder = new StringBuilder();

			builder.append(indexContext.getIndexDirectoryPath()); // Path
			builder.append(File.separator);
			builder.append(indexContext.getName()); // Index name
			builder.append(File.separator);
			builder.append(time); // Time
			builder.append(File.separator);
			builder.append(ip); // Ip

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

	public static void addStringField(String fieldName, String fieldContent, Document document, Store store, Index analyzed,
			TermVector termVector) {
		Field field = document.getField(fieldName);
		if (field == null) {
			field = new Field(fieldName, fieldContent, store, analyzed, termVector);
			document.add(field);
		} else {
			String fieldValue = field.stringValue();
			StringBuilder builder = new StringBuilder(fieldValue).append(" ").append(fieldContent);
			field.setValue(builder.toString());
		}
	}

	public static void addReaderField(String fieldName, Document document, Store store, TermVector termVector, Reader reader)
			throws Exception {
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
			int read = -1;
			char[] chars = new char[1024];
			while ((read = fieldReader.read(chars)) > -1) {
				writer.write(chars, 0, read);
			}
			while ((read = reader.read(chars)) > -1) {
				writer.write(chars, 0, read);
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