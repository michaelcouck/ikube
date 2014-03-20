package ikube.model;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This is the context for a single index. It has the properties that define the index
 * like what it is going to index, i.e. the databases, intranets etc., and properties relating to
 * the Lucene index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
@Entity
@SuppressWarnings({"serial"})
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({@NamedQuery(name = IndexContext.FIND_BY_NAME, query = IndexContext.FIND_BY_NAME)})
public class IndexContext extends Indexable implements Comparable<IndexContext> {

    public static final String FIND_BY_NAME = "select i from IndexContext as i where i.name = :name";

    /**
     * Whether the searcher is open or not.
     */
    @Transient
    @Attribute(field = false, description = "Whether this index is opened, for transport in the grid as the writers and searchers are not serializable")
    private boolean open;
    /**
     * Can be null if there are no indexes running.
     */
    @Transient
    @Attribute(field = false, description = "The currently opened index writers, which can be null of course")
    private transient volatile IndexWriter[] indexWriters;
    /**
     * Can be null if there is no index created.
     */
    @Transient
    @Attribute(field = false, description = "The index searcher for the index, opened with the correct parameters and analyzer")
    private transient volatile IndexSearcher multiSearcher;
    /**
     * This analyzer will be used to index the data, and indeed to do the searching.
     */
    @Transient
    @Attribute(field = false, description = "The analyzer that was used for indexing this document set and is consequently used for searching")
    private transient volatile Analyzer analyzer;
    /**
     * A collection of currently processed resources, for duplication avoidance.
     */
    @Transient
    @Attribute(field = false, description = "An arbitrary collection of hashes for currently indexed documents, for disarding duplicates and delta indexing")
    private transient volatile Set<Long> hashes;

    /**
     * The maximum age of the index defined in minutes.
     */
    @Column
    @Min(value = 1)
    @Max(value = Integer.MAX_VALUE)
    @Attribute(field = false, description = "This is the maximum age that the index can become before it is re-indexed")
    private long maxAge;
    /**
     * The delay between documents being indexed, slows the indexing down.
     */
    @Column
    @Min(value = 0)
    @Max(value = 60000)
    @Attribute(field = false, description = "This is the throttle in mili seconds that will slow down the indexing")
    private long throttle = 0;

    /**
     * Lucene properties.
     */
    @Column
    @Min(value = 10)
    @Max(value = 1000000)
    @Attribute(field = false, description = "The number of documents to keep in the segments before they are merged to the main file during indexing")
    private int mergeFactor;
    @Column
    @Min(value = 10)
    @Max(value = 1000000)
    @Attribute(field = false, description = "The number of documents to keep in memory before writing to the file")
    private int bufferedDocs;
    @Column
    @Min(value = 1)
    @Max(value = 1000)
    @Attribute(field = false, description = "The size of the memory Lucene can occupy before the documents are written to the file")
    private double bufferSize;
    @Column
    @Min(value = 10)
    @Max(value = 1000000)
    @Attribute(field = false, description = "The maximum length of a field in the Lucene index")
    private int maxFieldLength;
    @Column
    @Attribute(field = false, description = "Whether this index should be in a compound file format")
    private boolean compoundFile;

    /**
     * Jdbc properties.
     */
    @Column
    @Min(value = 1)
    @Max(value = 1000000)
    @Attribute(field = false, description = "The batch size of the category set for database indexing")
    private int batchSize;
    /**
     * Internet properties.
     */
    @Column
    @Min(value = 1)
    @Max(value = 1000000)
    @Attribute(field = false, description = "The batch size of urls for the crawler")
    private int internetBatchSize;
    /**
     * The maximum length of a document that can be read.
     */
    @Column
    @Min(value = 1)
    @Max(value = 1000000000)
    @Attribute(field = false, description = "The maximum read length for a document")
    private long maxReadLength;
    /**
     * The path to the index directory, either relative or absolute.
     */
    @Column
    @NotNull
    @Size(min = 2, max = 256)
    @Attribute(field = false, description = "The absolute or relative path to the directory where the index will be written")
    private String indexDirectoryPath;
    /**
     * The path to the backup index directory, either relative or absolute.
     */
    @Column
    @NotNull
    @Size(min = 2, max = 256)
    @Attribute(field = false, description = "The absolute or relative path to the directory where the index will be backed up")
    private String indexDirectoryPathBackup;
    @Column
    @Attribute(field = false, description = "This flag indicates whether the index is being generated currently")
    private boolean indexing;
    @Column
    @Attribute(field = false, description = "This flag indicates whether the index should be delta indexed, i.e. no new index just the changes n the resources")
    private boolean delta;

    @Transient
    @Attribute(field = false, description = "The number of documents in the currently opened searcher")
    private long numDocsForSearchers;
    @Transient
    @SuppressWarnings("UnusedDeclaration")
    @Attribute(field = false, description = "The latest snapshot for the index with static details for it, like size on disk etc.")
    private Snapshot snapshot;
    @Transient
    @Attribute(field = false, description = "The snapshot for this index, in reverse chronological order")
    private List<Snapshot> snapshots;

    public String getIndexName() {
        return super.getName();
    }

    public void setIndexName(final String indexName) {
        super.setName(indexName);
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(final long maxAge) {
        this.maxAge = maxAge;
    }

    public long getThrottle() {
        return throttle;
    }

    public void setThrottle(final long throttle) {
        this.throttle = throttle;
    }

    public boolean isCompoundFile() {
        return compoundFile;
    }

    public void setCompoundFile(final boolean compoundFile) {
        this.compoundFile = compoundFile;
    }

    public int getMaxFieldLength() {
        return maxFieldLength;
    }

    public void setMaxFieldLength(final int maxFieldLength) {
        this.maxFieldLength = maxFieldLength;
    }

    public int getBufferedDocs() {
        return bufferedDocs;
    }

    public void setBufferedDocs(final int bufferedDocs) {
        this.bufferedDocs = bufferedDocs;
    }

    public int getMergeFactor() {
        return mergeFactor;
    }

    public void setMergeFactor(final int mergeFactor) {
        this.mergeFactor = mergeFactor;
    }

    public double getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(final double bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    public int getInternetBatchSize() {
        return internetBatchSize;
    }

    public void setInternetBatchSize(final int internetBatchSize) {
        this.internetBatchSize = internetBatchSize;
    }

    public long getMaxReadLength() {
        return maxReadLength;
    }

    public void setMaxReadLength(final long maxReadLength) {
        this.maxReadLength = maxReadLength;
    }

    public String getIndexDirectoryPath() {
        return indexDirectoryPath;
    }

    public void setIndexDirectoryPath(final String indexDirectoryPath) {
        this.indexDirectoryPath = indexDirectoryPath;
    }

    public String getIndexDirectoryPathBackup() {
        return indexDirectoryPathBackup;
    }

    public void setIndexDirectoryPathBackup(final String indexDirectoryPathBackup) {
        this.indexDirectoryPathBackup = indexDirectoryPathBackup;
    }

    public IndexWriter[] getIndexWriters() {
        return indexWriters;
    }

    public void setIndexWriters(final IndexWriter... indexWriters) {
        this.indexWriters = indexWriters;
        setIndexing(indexWriters != null && indexWriters.length > 0);
    }

    public IndexSearcher getMultiSearcher() {
        return multiSearcher;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(final boolean open) {
        this.open = open;
    }

    public void setMultiSearcher(final IndexSearcher multiSearcher) {
        setOpen(multiSearcher != null);
        this.multiSearcher = multiSearcher;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(final Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public Set<Long> getHashes() {
        return hashes;
    }

    public void setHashes(Set<Long> hashes) {
        this.hashes = hashes;
    }

    public List<Snapshot> getSnapshots() {
        if (snapshots == null) {
            snapshots = new ArrayList<>();
        }
        return snapshots;
    }

    public void setSnapshots(final List<Snapshot> snapshots) {
        this.snapshots = snapshots;
        getSnapshot();
    }

    public Snapshot getSnapshot() {
        if (getSnapshots().size() == 0) {
            return null;
        }
        return snapshot = getSnapshots().get(getSnapshots().size() - 1);
    }

    public long getNumDocsForSearchers() {
        return numDocsForSearchers;
    }

    public void setNumDocsForSearchers(long numDocsForSearchers) {
        this.numDocsForSearchers = numDocsForSearchers;
    }

    public boolean isIndexing() {
        setIndexing(getIndexWriters() != null);
        return indexing;
    }

    public void setIndexing(final boolean indexing) {
        this.indexing = indexing;
    }

    public boolean isDelta() {
        return delta;
    }

    public void setDelta(final boolean delta) {
        this.delta = delta;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public int compareTo(final IndexContext other) {
        return getIndexName().compareTo(other.getIndexName());
    }

}