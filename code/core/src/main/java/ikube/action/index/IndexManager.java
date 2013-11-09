package ikube.action.index;

import ikube.IConstants;
import ikube.action.index.analyzer.StemmingAnalyzer;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NIOFSDirectory;

/**
 * This class opens and closes the Lucene index writer. There are also methods that get the path to the index directory based on the path in the index context.
 * This class also has methods that add fields to a document, either directly of via a file reader and writer.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public final class IndexManager {

	private static final Logger LOGGER = Logger.getLogger(IndexManager.class);

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
	 * This method opens a Lucene index writer, and if successful sets it in the index context where the handlers can access it and add documents to it during
	 * the index. The index writer is opened on a directory that will be the index path on the file system, the name of the index, then the
	 * 
	 * @param ip the ip address of this machine
	 * @param indexContext the index context to open the writer for
	 * @param time the time stamp for the index directory. This can come from the system time but it can also come from another server. When an index is started
	 *        the server will publish the time it started the index. In this way we can check the timestamp for the index, and if it is set then we use the
	 *        cluster timestamp. As a category we write the index in the same 'timestamp' directory
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
			LOGGER.info("Index directory time : " + time + ", date : " + new Date(time) + ", writing index to directory " + indexDirectoryPath);
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
	public static synchronized IndexWriter openIndexWriter(final IndexContext<?> indexContext, final File indexDirectory, final boolean create)
			throws Exception {
		Directory directory = NIOFSDirectory.open(indexDirectory);
		// Directory directory = FSDirectory.open(indexDirectory);
		return openIndexWriter(indexContext, directory, create);
	}

	/**
	 * This method opens the index writer, with the specified directory, allowing the opportunity to open the writer in memory for example.
	 * 
	 * @param indexContext the index context to open the writer for
	 * @param directory the directory to open the writer on, could be in memory
	 * @param create whether to create the index from scratch, i.e. deleting the original contents
	 * @return the index writer on the directory
	 * @throws Exception
	 */
	public static synchronized IndexWriter openIndexWriter(final IndexContext<?> indexContext, final Directory directory, final boolean create)
			throws Exception {
		@SuppressWarnings("resource")
		Analyzer analyzer = indexContext.getAnalyzer() != null ? indexContext.getAnalyzer() : new StemmingAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(IConstants.VERSION, analyzer);
		indexWriterConfig.setOpenMode(create ? OpenMode.CREATE : OpenMode.APPEND);
		indexWriterConfig.setRAMBufferSizeMB(indexContext.getBufferSize());
		indexWriterConfig.setMaxBufferedDocs(indexContext.getBufferedDocs());
		LogByteSizeMergePolicy mergePolicy = new LogByteSizeMergePolicy() {
			{
				this.maxMergeDocs = indexContext.getMergeFactor();
				this.maxMergeSize = (long) indexContext.getBufferSize();
				this.useCompoundFile = indexContext.isCompoundFile();
				this.mergeFactor = indexContext.getMergeFactor();
			}
		};
		indexWriterConfig.setMergePolicy(mergePolicy);
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		return indexWriter;
	}

	/**
	 * This method will close the index writer, provided it is not null. Also the index writer in the context will be removed.
	 * 
	 * @param indexContext the index context to close the writer for
	 */
	public static synchronized void closeIndexWriters(final IndexContext<?> indexContext) {
		try {
			if (indexContext.getIndexWriters() != null) {
				for (final IndexWriter indexWriter : indexContext.getIndexWriters()) {
					LOGGER.info("Optimizing and closing the index : " + indexContext.getIndexName() + ", " + indexWriter);
					closeIndexWriter(indexWriter);
					LOGGER.info("Index optimized and closed : " + indexContext.getIndexName() + ", " + indexWriter);
				}
				indexContext.setIndexWriters(new IndexWriter[0]);
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
			indexWriter.forceMerge(10, Boolean.TRUE);
			indexWriter.deleteUnusedFiles();
			indexWriter.optimize(10);
		} catch (NullPointerException e) {
			LOGGER.error("Null pointer, in the index writer : " + indexWriter);
			LOGGER.debug(null, e);
		} catch (CorruptIndexException e) {
			LOGGER.error("Corrput index : " + indexWriter, e);
		} catch (IOException e) {
			LOGGER.error("IO optimising the index : " + indexWriter, e);
		} catch (Exception e) {
			LOGGER.error("General exception comitting the index : " + indexWriter, e);
		}
		try {
			indexWriter.close();
		} catch (Exception e) {
			LOGGER.error("Exception closing the index writer : " + indexWriter, e);
		}
		try {
			if (directory != null) {
				int retry = 0;
				int maxRetry = 10;
				// We have to wait for the merges and the close
				while (IndexWriter.isLocked(directory) && retry++ < maxRetry) {
					IndexWriter.unlock(directory);
					if (IndexWriter.isLocked(directory)) {
						LOGGER.warn("Index still locked : " + directory);
						ThreadUtilities.sleep(1000);
					}
				}
				directory.close();
			}
		} catch (Exception e) {
			LOGGER.error("Exception releasing the lock on the index writer : " + indexWriter, e);
		}
	}

	/**
	 * This method will get the path to the index directory that will be created, based on the path in the context, the time and the ip of the machine.
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
	 * The category of this is something like ./indexes/ikube/123456789/127.0.0.1. This method will return the directory ./indexes/ikube/123456789. In other
	 * words the timestamp directory, not the individual server index directories.
	 * 
	 * @param baseIndexDirectoryPath the base path to the indexes, i.e. the ./indexes part
	 * @return the latest time stamped directory at this path, in other words the ./indexes/ikube/123456789 directory. Note that there is no Lucene index at
	 *         this path, the Lucene index is still in the server ip address directory in this time stamp directory, i.e. at ./indexes/ikube/123456789/127.0.0.1
	 */
	public static synchronized File getLatestIndexDirectory(final String baseIndexDirectoryPath) {
		try {
			File baseIndexDirectory = FileUtilities.getFile(baseIndexDirectoryPath, Boolean.TRUE);
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
				return indexSize;
			}
			List<File> files = new ArrayList<File>(Arrays.asList(latestIndexDirectory.listFiles()));
			do {
				for (final File file : files.toArray(new File[files.size()])) {
					if (file.exists() && file.canRead()) {
						if (file.isDirectory()) {
							files.addAll(Arrays.asList(file.listFiles()));
						} else {
							indexSize += file.length();
						}
					}
					files.remove(file);
				}
			} while (files.size() > 0);
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

	/**
	 * This method will first look at the index writers to get the number of documents currently indexed in the current action, otherwise the total number of
	 * documents in the index searcher for the index context.
	 * 
	 * @param indexContext the index context to the get the total number of documents for, either in the index writers or in the searcher
	 * @return the total current number of documents in the index context
	 */
	public static long getNumDocsForIndexWriters(final IndexContext<?> indexContext) {
		long numDocs = 0;
		IndexWriter[] indexWriters = indexContext.getIndexWriters();
		if (indexWriters != null && indexWriters.length > 0) {
			for (final IndexWriter indexWriter : indexWriters) {
				try {
					numDocs += indexWriter.numDocs();
				} catch (AlreadyClosedException e) {
					LOGGER.warn("Index writer is closed : " + e.getMessage());
				} catch (Exception e) {
					LOGGER.error("Exception reading the number of documents from the writer", e);
				}
			}
		}
		return numDocs;
	}

	public static long getNumDocsForIndexSearchers(final IndexContext<?> indexContext) {
		long numDocs = 0;
		if (indexContext.getMultiSearcher() != null) {
			for (final Searchable searchable : indexContext.getMultiSearcher().getSearchables()) {
				numDocs += ((IndexSearcher) searchable).getIndexReader().numDocs();
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
	 * This method will get the exact path to the backup directory for the indexes indexes for this index context, i.e. '/path/to/index/and/backup/indexName'.
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

	public static Document addStringField(final String fieldName, final String fieldContent, final Indexable<?> indexable, final Document document) {
		
		Store store = indexable.isStored() ? Store.YES : Store.NO;
		Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
		TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;
		
		if (fieldName != null && fieldContent != null) {
			Field field = document.getField(fieldName);
			if (field == null) {
				field = new Field(fieldName, fieldContent, store, analyzed, termVector);
				document.add(field);
			} else {
				String fieldValue = field.stringValue() != null ? field.stringValue() : "";
				StringBuilder builder = new StringBuilder(fieldValue).append(' ').append(fieldContent);
				field.setValue(builder.toString());
			}
			// Add this for the autocomplete
			// field.setOmitNorms(omitNorms);
		}
		return document;
	}

	public static Document addNumericField(final String fieldName, final String fieldContent, final Document document, final Store store) {
		document.add(new NumericField(fieldName, store, true).setDoubleValue(Double.parseDouble(fieldContent)));
		return document;
	}

	public static Document addReaderField(final String fieldName, final Document document, final Store store, final TermVector termVector, final Reader reader)
			throws Exception {
		if (fieldName != null && reader != null) {
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
		return document;
	}

	/**
	 * Singularity.
	 */
	private IndexManager() {
		// Documented
	}

}