package ikube.action.index;

import ikube.IConstants;
import ikube.action.index.analyzer.StemmingAnalyzer;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.*;
import java.util.*;

/**
 * This class opens and closes the Lucene index writer. There are also methods that get the path to the index directory
 * based on the path in the index context. This class also has methods that add fields to a document, either directly of via a
 * file reader and writer.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
@SuppressWarnings("StringBufferReplaceableByString")
public final class IndexManager {

    private static final Logger LOGGER = Logger.getLogger(IndexManager.class);

    /**
     * There can be only one...
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
    @SuppressWarnings("ConstantConditions")
    public static synchronized IndexWriter[] openIndexWriterDelta(final IndexContext indexContext) throws Exception {
        LOGGER.info("Opening delta writers on index context : " + indexContext.getName());
        String ip = UriUtilities.getIp();
        String indexDirectoryPath = getIndexDirectoryPath(indexContext);
        // Find all the indexes in the latest index directory and open a writer on each one
        File latestIndexDirectory = getLatestIndexDirectory(indexDirectoryPath);
        IndexWriter[] indexWriters;
        if (latestIndexDirectory == null || latestIndexDirectory.listFiles() == null || latestIndexDirectory.listFiles() == null ||
                latestIndexDirectory.listFiles().length == 0) {
            // This means that we tried to do a delta index but there was no index, i.e. we still have to index from the start
            IndexWriter indexWriter = openIndexWriter(indexContext, System.currentTimeMillis(), ip);
            indexWriters = new IndexWriter[]{indexWriter};
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
     * This method opens a Lucene index writer, and if successful sets it in the index context where the handlers can access
     * it and add documents to it during the index. The index writer is opened on a directory that will be the index path on the
     * file system, the name of the index, then the
     *
     * @param ip           the ip address of this machine
     * @param indexContext the index context to open the writer for
     * @param time         the time stamp for the index directory. This can come from the system time but it can also come from
     *                     another server. When an index is started the server will publish the time it started the index. In this
     *                     way we can check the timestamp for the index, and if it is set then we use the cluster timestamp. As a
     *                     category we write the index in the same 'timestamp' directory
     * @return the index writer opened for this index context or null if there was any exception opening the index
     */
    @SuppressWarnings("ConstantConditions")
    public static synchronized IndexWriter openIndexWriter(final IndexContext indexContext, final long time, final String ip) {
        boolean delete = Boolean.FALSE;
        File indexDirectory = null;
        IndexWriter indexWriter = null;
        try {
            String indexDirectoryPath = getIndexDirectory(indexContext, time, ip);
            // indexDirectory = FileUtilities.getFile(indexDirectoryPath, Boolean.TRUE);
            indexDirectory = FileUtilities.getOrCreateDirectory(indexDirectoryPath);
            boolean readable = indexDirectory.setReadable(true);
            boolean writable = indexDirectory.setWritable(true, false);
            if (!readable || !writable) {
                LOGGER.warn("Directory not readable or writable : read : " + readable + ", write : " + writable);
            }
            LOGGER.info("Index directory time : " + time + ", date : " + new Date(time) +
                    ", writing index to directory " + indexDirectoryPath);
            indexWriter = openIndexWriter(indexContext, indexDirectory, Boolean.TRUE);
        } catch (final CorruptIndexException e) {
            LOGGER.error("We expected a new index and got a corrupt one.", e);
            LOGGER.warn("Didn't initialise the index writer. Will try to delete the index directory.");
            delete = Boolean.TRUE;
        } catch (final LockObtainFailedException e) {
            LOGGER.error("Failed to obtain the lock on the directory. Check the file system permissions or failed indexing jobs, "
                    + "there will be a lock file in one of the index directories.", e);
        } catch (final IOException e) {
            LOGGER.error("IO exception detected opening the writer", e);
        } catch (final Exception e) {
            LOGGER.error("Unexpected exception detected while initializing the IndexWriter", e);
        } finally {
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
     * @param indexContext   the index context for the parameters for the index writer like compound file and buffer size
     * @param indexDirectory the directory to open the index in
     * @param create         whether to create the index or open on an existing index
     * @return the index writer open on the specified directory
     * @throws Exception
     */
    public static synchronized IndexWriter openIndexWriter(final IndexContext indexContext, final File indexDirectory, final boolean create)
            throws Exception {
        Directory directory = NIOFSDirectory.open(indexDirectory);
        return openIndexWriter(indexContext, directory, create);
    }

    /**
     * This method opens the index writer, with the specified directory, allowing the opportunity to open the writer in memory for example.
     *
     * @param indexContext the index context to open the writer for
     * @param directory    the directory to open the writer on, could be in memory
     * @param create       whether to create the index from scratch, i.e. deleting the original contents
     * @return the index writer on the directory
     * @throws Exception
     */
    public static synchronized IndexWriter openIndexWriter(final IndexContext indexContext, final Directory directory, final boolean create) throws Exception {
        @SuppressWarnings("resource")
        Analyzer analyzer = indexContext.getAnalyzer() != null ? indexContext.getAnalyzer() : new StemmingAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(IConstants.LUCENE_VERSION, analyzer);
        indexWriterConfig.setOpenMode(create ? OpenMode.CREATE : OpenMode.APPEND);
        indexWriterConfig.setRAMBufferSizeMB(indexContext.getBufferSize());
        indexWriterConfig.setMaxBufferedDocs(indexContext.getBufferedDocs());
        LogByteSizeMergePolicy mergePolicy = new LogByteSizeMergePolicy() {
            {
                this.maxMergeDocs = indexContext.getBufferedDocs();
                this.maxMergeSize = (long) indexContext.getBufferSize();
                // this.useCompoundFile = indexContext.isCompoundFile();
                this.mergeFactor = indexContext.getMergeFactor();
            }
        };
        indexWriterConfig.setMergePolicy(mergePolicy);
        return new IndexWriter(directory, indexWriterConfig);
    }

    /**
     * This method will close the index writer, provided it is not null. Also the index writer in the context will be removed.
     *
     * @param indexContext the index context to close the writer for
     */
    public static synchronized void closeIndexWriters(final IndexContext indexContext) {
        try {
            if (indexContext.getIndexWriters() != null) {
                for (final IndexWriter indexWriter : indexContext.getIndexWriters()) {
                    LOGGER.info("Optimizing and closing the index : " + indexContext.getIndexName() + ", " + indexWriter);
                    closeIndexWriter(indexWriter);
                    LOGGER.info("Index optimized and closed : " + indexContext.getIndexName() + ", " + indexWriter);
                }
            }
        } finally {
            indexContext.setIndexWriters();
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
            // specially over the network...
            ThreadUtilities.sleep(3000);
            directory = indexWriter.getDirectory();
            indexWriter.prepareCommit();
            indexWriter.commit();
            indexWriter.maybeMerge();
            indexWriter.forceMerge(10, Boolean.TRUE);
            indexWriter.waitForMerges();
            indexWriter.deleteUnusedFiles();
        } catch (final NullPointerException e) {
            LOGGER.error("Null pointer, in the index writer : " + indexWriter);
            LOGGER.debug(null, e);
        } catch (final CorruptIndexException e) {
            LOGGER.error("Corrupt index : " + indexWriter, e);
        } catch (final IOException e) {
            LOGGER.error("IO optimising the index : " + indexWriter, e);
        } catch (final Exception e) {
            LOGGER.error("General exception committing the index : " + indexWriter, e);
        } finally {
            try {
                LOGGER.info("Unlocking the index directory : " + indexWriter.getDirectory());
                IndexWriter.unlock(indexWriter.getDirectory());
            } catch (final IOException e) {
                LOGGER.error("Exception trying to unlock the index writer directory : " + indexWriter, e);
            }
        }
        try {
            indexWriter.close();
        } catch (final Exception e) {
            LOGGER.error("Exception closing the index writer : " + indexWriter, e);
        }
        try {
            LOGGER.info("Checking that all the index writers are closed and unlocked : " + directory);
            if (directory != null) {
                int retry = 10;
                // We have to wait for the merges and the close
                LOGGER.info("Unlocking : " + IndexWriter.isLocked(directory) + ", retry : " + retry);
                while (IndexWriter.isLocked(directory) && --retry > 0) {
                    IndexWriter.unlock(directory);
                    if (IndexWriter.isLocked(directory)) {
                        LOGGER.warn("Index still locked : " + directory);
                        ThreadUtilities.sleep(1000);
                    }
                }
                directory.close();
            }
        } catch (final Exception e) {
            LOGGER.error("Exception releasing the lock on the index writer : " + indexWriter, e);
        }
    }

    /**
     * This method will get the path to the index directory that will be created, based on the path in the context, the time and the ip of the machine.
     *
     * @param indexContext the context to use for the path to the indexes for the context
     * @param time         the time for the upper directory name
     * @param ip           the ip for the index directory name
     * @return the full path to the
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    public static String getIndexDirectory(final IndexContext indexContext, final long time, final String ip) {
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
     * <p/>
     * 1) The path to the index on the file system<br>
     * 2) The name of the index<br>
     * 3) The time(as a long) that the index was created 4) The ip address of the server that created the index<br>
     * <p/>
     * The category of this is something like ./indexes/ikube/123456789/127.0.0.1. This method will return the directory ./indexes/ikube/123456789. In other
     * words the timestamp directory, not the individual server index directories.
     *
     * @param baseIndexDirectoryPath the base path to the indexes, i.e. the ./indexes part
     * @return the latest time stamped directory at this path, in other words the ./indexes/ikube/123456789 directory. Note that there is no Lucene index at
     * this path, the Lucene index is still in the server ip address directory in this time stamp directory, i.e. at ./indexes/ikube/123456789/127.0.0.1
     */
    public static synchronized File getLatestIndexDirectory(final String baseIndexDirectoryPath) {
        try {
            File baseIndexDirectory = FileUtilities.getFile(baseIndexDirectoryPath, Boolean.TRUE);
            return getLatestIndexDirectory(baseIndexDirectory, null);
        } finally {
            IndexManager.class.notifyAll();
        }
    }

    public static synchronized File getLatestIndexDirectory(final File file, final File latestSoFar) {
        if (file == null) {
            return latestSoFar;
        }
        File latest = latestSoFar;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (final File child : children) {
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

    @SuppressWarnings("StringBufferReplaceableByString")
    public static long getIndexSize(final IndexContext indexContext) {
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
            File[] latestIndexDirectories = latestIndexDirectory.listFiles();
            if (latestIndexDirectories != null) {
                List<File> files = new ArrayList<>(Arrays.asList(latestIndexDirectories));
                do {
                    for (final File file : files.toArray(new File[files.size()])) {
                        if (file.exists() && file.canRead()) {
                            if (file.isDirectory()) {
                                File[] subFiles = file.listFiles();
                                if (subFiles != null) {
                                    files.addAll(Arrays.asList(subFiles));
                                }
                            } else {
                                indexSize += file.length();
                            }
                        }
                        files.remove(file);
                    }
                } while (files.size() > 0);
            }
        } catch (final Exception e) {
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
     * This method will first look at the index writers to get the number of documents currently indexed in the current action,
     * otherwise the total number of documents in the index searcher for the index context.
     *
     * @param indexContext the index context to the get the total number of documents for, either in the index writers or
     *                     in the searcher
     * @return the total current number of documents in the index context
     */
    public static long getNumDocsForIndexWriters(final IndexContext indexContext) {
        long numDocs = 0;
        IndexWriter[] indexWriters = indexContext.getIndexWriters();
        if (indexWriters != null && indexWriters.length > 0) {
            for (final IndexWriter indexWriter : indexWriters) {
                try {
                    numDocs += indexWriter.numDocs();
                } catch (final AlreadyClosedException e) {
                    LOGGER.warn("Index writer is closed : " + e.getMessage());
                } catch (final Exception e) {
                    LOGGER.error("Exception reading the number of documents from the writer", e);
                }
            }
        }
        return numDocs;
    }

    public static long getNumDocsForIndexSearchers(final IndexContext indexContext) {
        long numDocs = 0;
        if (indexContext.getMultiSearcher() != null) {
            numDocs = indexContext.getMultiSearcher().getIndexReader().numDocs();
        }
        return numDocs;
    }

    public static Date getLatestIndexDirectoryDate(final IndexContext indexContext) {
        long timestamp = 0;
        String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
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
    public static String getIndexDirectoryPath(final IndexContext indexContext) {
        return getIndexDirectoryPath(indexContext, indexContext.getIndexDirectoryPath());
    }

    /**
     * This method will get the exact path to the backup directory for the indexes indexes for this index context,
     * i.e. '/path/to/index/and/backup/indexName'.
     *
     * @param indexContext the index context to the the path to the backup directory for the indexes
     * @return the absolute, cleaned path to the backup directory for the indexes for this index context
     */
    public static String getIndexDirectoryPathBackup(final IndexContext indexContext) {
        return getIndexDirectoryPath(indexContext, indexContext.getIndexDirectoryPathBackup());
    }

    private static String getIndexDirectoryPath(final IndexContext indexContext, final String indexDirectory) {
        StringBuilder builder = new StringBuilder();
        builder.append(new File(indexDirectory).getAbsolutePath()); // Path
        builder.append(File.separator);
        builder.append(indexContext.getIndexName()); // Index name
        return FileUtilities.cleanFilePath(builder.toString());
    }

    public static Document addStringField(final String fieldName, final String fieldContent, final Indexable indexable, final Document document) {
        if (fieldName != null && fieldContent != null) {
            Field field;
            FieldType fieldType = new FieldType();
            fieldType.setIndexed(indexable.isAnalyzed());
            fieldType.setStored(indexable.isStored());
            // NOTE: Must be tokenized to search correctly, not tokenized? no results!!!
            fieldType.setTokenized(indexable.isTokenized());
            // For normalization of the length, i.e. longer strings are scored higher
            // normally, but consider that a book with the word 3 times but 10 000 words, will
            // get a lower score than a sentence of two exact words. Omitting the norms will force the book
            // to get a higher score because it has the word three times, although the 'natural' relevance
            // would be the sentence with two words because of the length and matches
            fieldType.setOmitNorms(indexable.isOmitNorms());
            // NOTE: If the term vectors are enabled the field cannot be searched, i.e. no results!!!
            fieldType.setStoreTermVectors(indexable.isVectored());

            Field oldField = (Field) document.getField(fieldName);
            if (oldField == null) {
                field = new Field(fieldName, fieldContent, fieldType);
            } else {
                document.removeField(fieldName);

                StringBuilder builder = new StringBuilder();
                builder.append(oldField.stringValue());
                builder.append(" ");
                builder.append(fieldContent);

                field = new Field(fieldName, builder.toString(), fieldType);
            }
            if (indexable.getBoost() > 0) {
                field.setBoost(indexable.getBoost());
            }
            document.add(field);
        }
        return document;
    }

    public static Document addNumericField(final String fieldName, final String fieldContent, final Document document, final boolean store, final float boost) {
        FieldType floatFieldType = new FieldType();
        floatFieldType.setStored(store);
        floatFieldType.setIndexed(Boolean.TRUE);
        floatFieldType.setNumericType(NumericType.FLOAT);
        // To sort on these fields they must not be tokenized for some reason
        floatFieldType.setTokenized(Boolean.FALSE);
        floatFieldType.setOmitNorms(Boolean.FALSE);

        Field floatField = new FloatField(fieldName, Float.parseFloat(fieldContent), floatFieldType);
        if (boost > 0) {
            floatField.setBoost(boost);
        }
        document.add(floatField);
        return document;
    }

    public static Document addReaderField(final String fieldName, final Document document, final Reader reader, final boolean vectored, final float boost) {
        if (fieldName != null && reader != null) {
            FieldType fieldType = new FieldType();
            Field field = (Field) document.getField(fieldName);
            if (field == null) {
                fieldType.setStoreTermVectors(vectored);
                field = new Field(fieldName, reader, fieldType);
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
                    document.removeField(fieldName);
                    field = new Field(fieldName, finalReader, fieldType);
                    if (boost > 0) {
                        field.setBoost(boost);
                    }
                    document.add(field);
                } catch (final Exception e) {
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

    public static Collection<String> getFieldNames(final IndexSearcher indexSearcher) {
        Set<String> fields = new TreeSet<>();
        MultiReader multiReader = (MultiReader) indexSearcher.getIndexReader();
        List<AtomicReaderContext> atomicReaderContexts = multiReader.leaves();
        for (final AtomicReaderContext atomicReaderContext : atomicReaderContexts) {
            try {
                FieldInfos fieldInfos = atomicReaderContext.reader().getFieldInfos();
                for (final FieldInfo fieldInfo : fieldInfos) {
                    fields.add(fieldInfo.name);
                }
            } catch (final NullPointerException e) {
                LOGGER.warn("Null pointer : ");
                LOGGER.debug(null, e);
            }
        }
        return fields;
    }

}