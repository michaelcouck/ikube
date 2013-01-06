package ikube.index;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.springframework.util.ReflectionUtils;

/**
 * This class opens and closes the Lucene index writer. There are also methods that get the path to the index directory based on the path in
 * the index context. This class also has methods that add fields to a document, either directly of via a file reader and writer.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public final class IndexManager {

	private static final Logger LOGGER = Logger.getLogger(IndexManager.class);

	public class MultiIndexWriter extends IndexWriter {
		public MultiIndexWriter(Directory d, IndexWriterConfig conf) throws CorruptIndexException, LockObtainFailedException, IOException {
			super(d, conf);
		}
	}

	/**
	 * Singularity.
	 */
	private IndexManager() {
		// Documented
	}

	/**
	 * This method will open index writers on each of the server index directories.
	 * 
	 * @param indexContext the index to open the writers for
	 * @return the array of index writers opened on all the index directories for the context
	 * @throws Exception
	 */
	public static synchronized IndexWriter[] openIndexWriterDelta(final IndexContext<?> indexContext) throws Exception {
		LOGGER.info("Opening delta writers on index context : " + indexContext);
		String ip = UriUtilities.getIp();
		String indexDirectoryPath = getIndexDirectoryPath(indexContext);
		// Find all the indexes in the latest index directory and open a writer on each one
		File latestIndexDirectory = getLatestIndexDirectory(indexDirectoryPath);
		IndexWriter[] indexWriters = null;
		if (latestIndexDirectory == null || latestIndexDirectory.listFiles() == null || latestIndexDirectory.listFiles().length == 0) {
			// This means that we tried to do a delta index but there was no index, i.e. we still have to index from the start
			IndexWriter indexWriter = openIndexWriter(indexContext, System.currentTimeMillis(), ip);
			indexWriters = new IndexWriter[] { indexWriter };
			LOGGER.info("Opened index writer new : " + indexWriter);
		} else {
			File[] latestServerIndexDirectories = latestIndexDirectory.listFiles();
			indexWriters = new IndexWriter[latestServerIndexDirectories.length];
			// Open an index writer on each one of the indexes in the set so we can delete the documents and update them
			for (int i = 0; i < latestServerIndexDirectories.length; i++) {
				final File latestServerIndexDirectory = latestServerIndexDirectories[i];
				IndexWriter indexWriter = openIndexWriter(indexContext, latestServerIndexDirectory, Boolean.FALSE);
				indexWriters[i] = indexWriter;
				LOGGER.info("Opened index writer on old index : " + latestServerIndexDirectory);
			}
		}
		return indexWriters;
	}

	/**
	 * This method opens a Lucene index writer, and if successful sets it in the index context where the handlers can access it and add
	 * documents to it during the index. The index writer is opened on a directory that will be the index path on the file system, the name
	 * of the index, then the
	 * 
	 * @param ip the ip address of this machine
	 * @param indexContext the index context to open the writer for
	 * @param time the time stamp for the index directory. This can come from the system time but it can also come from another server. When
	 *        an index is started the server will publish the time it started the index. In this way we can check the timestamp for the
	 *        index, and if it is set then we use the cluster timestamp. As a result we write the index in the same 'timestamp' directory
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
	 * @param indexContext the index context for the parameters for the index writer like compound file and buffer size
	 * @param indexDirectory the directory to open the index in
	 * @param create whether to create the index or open on an existing index
	 * @return the index writer open on the specified directory
	 * @throws Exception
	 */
	public static synchronized IndexWriter openIndexWriter(final IndexContext<?> indexContext, final File indexDirectory,
			final boolean create) throws Exception {
		Analyzer analyzer = indexContext.getAnalyzer() != null ? indexContext.getAnalyzer() : IConstants.ANALYZER;
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(IConstants.VERSION, analyzer);
		indexWriterConfig.setMaxBufferedDocs(indexContext.getBufferedDocs());
		MergePolicy mergePolicy = new LogByteSizeMergePolicy() {
			{
				this.maxMergeDocs = indexContext.getMergeFactor();
				this.maxMergeSize = (long) indexContext.getBufferSize();
				this.useCompoundFile = indexContext.isCompoundFile();
				this.mergeFactor = indexContext.getMergeFactor();
			}
		};
		indexWriterConfig.setMergePolicy(mergePolicy);

		Directory directory = FSDirectory.open(indexDirectory);
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);

		// Where to set these properties
		// indexWriter.setRAMBufferSizeMB(indexContext.getBufferSize());
		// indexWriter.setMaxFieldLength(indexContext.getMaxFieldLength());

		return indexWriter;
	}

	/**
	 * This method will close the index writer, provided it is not null. Also the index writer in the context will be removed.
	 * 
	 * @param indexContext the index context to close the writer for
	 */
	public static synchronized void closeIndexWriters(final IndexContext<?> indexContext) {
		try {
			if (indexContext != null && indexContext.getIndexWriters() != null) {
				for (final IndexWriter indexWriter : indexContext.getIndexWriters()) {
					LOGGER.info("Optimizing and closing the index : " + indexContext.getIndexName() + ", snapshot : "
							+ indexContext.getLastSnapshot());
					closeIndexWriter(indexWriter);
					LOGGER.info("Index optimized and closed : " + indexWriter + ", " + indexContext.getIndexName() + ", snapshot : "
							+ indexContext.getLastSnapshot());
				}
			}
		} finally {
			IndexManager.class.notifyAll();
		}
	}

	/**
	 * This method will close the index writer and optimize it too.
	 * 
	 * @param indexWriter the index writer to close and optimize
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
			indexWriter.commit();
			indexWriter.maybeMerge();
			indexWriter.forceMerge(8, Boolean.TRUE);
		} catch (NullPointerException e) {
			LOGGER.error("Null pointer, in the index writer : " + indexWriter);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Stack trace : ", e);
			}
		} catch (CorruptIndexException e) {
			LOGGER.error("Corrput index : " + indexWriter, e);
		} catch (IOException e) {
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
	 * @param indexContext the context to use for the path to the indexes for the context
	 * @param time the time for the upper directory name
	 * @param ip the ip for the index directory name
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

	/**
	 * This method gets the latest index directory. Index directories are defined by:<br>
	 * 
	 * 1) The path to the index on the file system<br>
	 * 2) The name of the index<br>
	 * 3) The time(as a long) that the index was created 4) The ip address of the server that created the index<br>
	 * 
	 * The result of this is something like ./indexes/ikube/123456789/127.0.0.1. This method will return the directory
	 * ./indexes/ikube/123456789. In other words the timestamp directory, not the individual server index directories.
	 * 
	 * @param baseIndexDirectoryPath the base path to the indexes, i.e. the ./indexes part
	 * @return the latest time stamped directory at this path, in other words the ./indexes/ikube/123456789 directory. Note that there is no
	 *         Lucene index at this path, the Lucene index is still in the server ip address directory in this time stamp directory, i.e. at
	 *         ./indexes/ikube/123456789/127.0.0.1
	 */
	public static synchronized File getLatestIndexDirectory(final String baseIndexDirectoryPath) {
		try {
			File baseIndexDirectory = FileUtilities.getFile(baseIndexDirectoryPath, Boolean.TRUE);
			LOGGER.debug("Base index directory : " + baseIndexDirectory);
			return getLatestIndexDirectory(baseIndexDirectory, null);
		} finally {
			IndexManager.class.notifyAll();
		}
	}

	protected static synchronized File getLatestIndexDirectory(final File file, final File latestSoFar) {
		if (file == null) {
			return latestSoFar;
		}
		File latest = latestSoFar;
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			for (File child : children) {
				if (IndexManager.isDigits(child.getName())) {
					if (latest == null) {
						latest = child;
					}
					long oneTime = Long.parseLong(child.getName());
					long twoTime = Long.parseLong(latest.getName());
					latest = oneTime > twoTime ? child : latest;
				} else {
					latest = getLatestIndexDirectory(child, latest);
				}
			}
		}
		return latest;
	}

	/**
	 * Verifies that all the characters in a string are digits, ie. the string is a number.
	 * 
	 * @param string the string to verify for digit data
	 * @return whether every character in a string is a digit
	 */
	public static boolean isDigits(final String string) {
		if (string == null || string.trim().equals("")) {
			return false;
		}
		char[] chars = string.toCharArray();
		for (char c : chars) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	public static long getIndexSize(final IndexContext<?> indexContext) {
		long indexSize = 0;
		try {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(indexContext.getIndexDirectoryPath());
			stringBuilder.append(IConstants.SEP);
			stringBuilder.append(indexContext.getIndexName());
			File latestIndexDirectory = IndexManager.getLatestIndexDirectory(stringBuilder.toString());
			if (latestIndexDirectory == null || !latestIndexDirectory.exists() || !latestIndexDirectory.isDirectory()) {
				// LOGGER.info("No latest index : " + indexContext.getIndexName());
				return indexSize;
			}
			File[] serverIndexDirectories = latestIndexDirectory.listFiles();
			if (serverIndexDirectories == null) {
				LOGGER.info("No server directories : " + indexContext.getIndexName());
				return indexSize;
			}
			for (File serverIndexDirectory : serverIndexDirectories) {
				if (serverIndexDirectory != null && serverIndexDirectory.exists() && serverIndexDirectory.isDirectory()) {
					indexSize += getDirectorySize(serverIndexDirectory);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception getting the size of the index : ", e);
		}
		return indexSize;
	}

	public static long getDirectorySize(final File directory) {
		long indexSize = 0;
		File[] indexFiles = directory.listFiles();
		if (indexFiles == null || indexFiles.length == 0) {
			return 0;
		}
		for (File indexFile : indexFiles) {
			indexSize += indexFile.length();
		}
		return indexSize;
	}

	public static long getNumDocs(final IndexContext<?> indexContext) {
		long numDocs = 0;
		MultiSearcher multiSearcher = indexContext.getMultiSearcher();
		if (multiSearcher != null) {
			for (Searchable searchable : multiSearcher.getSearchables()) {
				numDocs += ((IndexSearcher) searchable).getIndexReader().numDocs();
			}
		}
		return numDocs;
	}

	public static long getNumDocsIndexWriter(final IndexContext<?> indexContext) {
		long numDocs = 0;
		IndexWriter[] indexWriters = indexContext.getIndexWriters();
		if (indexWriters != null && indexWriters.length > 0) {
			for (final IndexWriter indexWriter : indexWriters) {
				try {
					final AtomicBoolean isClosed = new AtomicBoolean();
					ReflectionUtils.MethodFilter methodFilter = new ReflectionUtils.MethodFilter() {
						@Override
						public boolean matches(Method method) {
							return method.getName().equals("isClosed");
						}
					};
					ReflectionUtils.MethodCallback methodCallback = new ReflectionUtils.MethodCallback() {
						@Override
						public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
							try {
								method.setAccessible(Boolean.TRUE);
								Boolean result = (Boolean) method.invoke(indexWriter);
								isClosed.set(result);
							} catch (InvocationTargetException e) {
								isClosed.set(Boolean.TRUE);
								LOGGER.warn("Exception checking the closed status of the writer : ", e);
							}
						}
					};
					ReflectionUtils.doWithMethods(IndexWriter.class, methodCallback, methodFilter);
					// Checking if the directory is locked is like checking that the writer is sill open
					if (!isClosed.get()) {
						numDocs += indexWriter.numDocs();
					}
				} catch (AlreadyClosedException e) {
					LOGGER.warn("Index writer is closed : " + e.getMessage());
				} catch (Exception e) {
					LOGGER.error("Exception reading the number of documents from the writer", e);
				}
			}
		}
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
		if (latestIndexDirectory != null) {
			File[] serverIndexDirectories = latestIndexDirectory.listFiles();
			if (serverIndexDirectories != null) {
				Directory directory = null;
				IndexReader indexReader = null;
				for (File serverIndexDirectory : serverIndexDirectories) {
					try {
						directory = FSDirectory.open(serverIndexDirectory);
						if (!IndexWriter.isLocked(directory) && IndexReader.indexExists(directory)) {
							indexReader = IndexReader.open(directory);
							numDocs += indexReader.numDocs();
						}
					} catch (Exception e) {
						LOGGER.error("Exception opening the reader on the index : " + serverIndexDirectory, e);
					} finally {
						try {
							if (directory != null) {
								directory.close();
							}
							if (indexReader != null) {
								indexReader.close();
							}
						} catch (Exception e) {
							LOGGER.error("Exception closing the readon on the index : ", e);
						}
					}
				}
			}
		}
		return numDocs;
	}

	public static Date getLatestIndexDirectoryDate(final IndexContext<?> indexContext) {
		long timestamp = 0;
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
		if (latestIndexDirectory != null) {
			String name = latestIndexDirectory.getName();
			if (StringUtils.isNumeric(name)) {
				timestamp = Long.parseLong(name);
			}
		}
		return new Date(timestamp);
	}

	/**
	 * This method will get the exact path to the indexes for this index context, i.e. '/path/to/index/and/indexName'.
	 * 
	 * @param indexContext the index context to the the path to the indexes for
	 * @return the absolute, cleaned path to the indexes for this index context
	 */
	public static String getIndexDirectoryPath(final IndexContext<?> indexContext) {
		return getIndexDirectoryPath(indexContext, indexContext.getIndexDirectoryPath());
	}

	/**
	 * This method will get the exact path to the backup directory for the indexes indexes for this index context, i.e.
	 * '/path/to/index/and/backup/indexName'.
	 * 
	 * @param indexContext the index context to the the path to the backup directory for the indexes
	 * @return the absolute, cleaned path to the backup directory for the indexes for this index context
	 */
	public static String getIndexDirectoryPathBackup(final IndexContext<?> indexContext) {
		return getIndexDirectoryPath(indexContext, indexContext.getIndexDirectoryPathBackup());
	}

	private static String getIndexDirectoryPath(final IndexContext<?> indexContext, final String indexDirectory) {
		StringBuilder builder = new StringBuilder();
		builder.append(new File(indexDirectory).getAbsolutePath()); // Path
		builder.append(File.separator);
		builder.append(indexContext.getIndexName()); // Index name
		return FileUtilities.cleanFilePath(builder.toString());
	}

	public static void addStringField(final String fieldName, final String fieldContent, final Document document, final Store store,
			final Index analyzed, final TermVector termVector) {
		if (fieldName == null || fieldContent == null) {
			LOGGER.warn("Field and content can't be null : " + fieldName);
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