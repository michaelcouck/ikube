package ikube.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.MultiSearcher;

/**
 * This is the context for a single index. It has the properties that define the index like what it is going to index, i.e. the databases, intranets etc., and
 * properties relating to the Lucene index. This object acts a like the command in the 'Command Pattern' as in this context is passed to handlers that will
 * perform certain logic based on the properties of this context.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@SuppressWarnings({ "serial", "deprecation" })
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({ @NamedQuery(name = IndexContext.FIND_BY_NAME, query = IndexContext.FIND_BY_NAME) })
public class IndexContext<T> extends Indexable<T> implements Comparable<IndexContext<?>> {

	public static final String FIND_BY_NAME = "select i from IndexContext as i where i.name = :name";

	@Transient
	private boolean open;

	/** Can be null if there are no indexes running. */
	@Transient
	private transient volatile IndexWriter[] indexWriters;
	/** Can be null if there is no index created. */
	@Transient
	private transient volatile MultiSearcher multiSearcher;
	/** This analyzer will be used to index the data, and indeed to do the searching. */
	@Transient
	private transient volatile Analyzer analyzer;
	@Transient
	private transient volatile List<Long> hashes;

	/** The maximum age of the index defined in minutes. */
	@Column
	@Min(value = 1)
	@Max(value = Integer.MAX_VALUE)
	@Attribute(field = false, description = "This is the maximum age that the index can become before it is re-indexed")
	private long maxAge;
	/** The delay between documents being indexed, slows the indexing down. */
	@Column
	@Min(value = 0)
	@Max(value = 60000)
	@Attribute(field = false, description = "This is the throttle in mili seconds that will slow down the indexing")
	private long throttle = 0;

	/** Lucene properties. */
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

	/** Jdbc properties. */
	@Column
	@Min(value = 1)
	@Max(value = 1000000)
	@Attribute(field = false, description = "The batch size of the result set for database indexing")
	private int batchSize;
	/** Internet properties. */
	@Column
	@Min(value = 1)
	@Max(value = 1000000)
	@Attribute(field = false, description = "The batch size of urls for the crawler")
	private int internetBatchSize;

	/** The maximum length of a document that can be read. */
	@Column
	@Min(value = 1)
	@Max(value = 1000000000)
	@Attribute(field = false, description = "The maximum read length for a document")
	private long maxReadLength;
	/** The path to the index directory, either relative or absolute. */
	@Column
	@NotNull
	@Size(min = 2, max = 256)
	@Attribute(field = false, description = "The absolute or relative path to the directory where the index will be written")
	private String indexDirectoryPath;
	/** The path to the backup index directory, either relative or absolute. */
	@Column
	@NotNull
	@Size(min = 2, max = 256)
	@Attribute(field = false, description = "The absolute or relative path to the directory where the index will be backed up")
	private String indexDirectoryPathBackup;
	@Column
	@Attribute(field = false, description = "The is dynamically set by the logic to validate that there is disk space left on the drive where the index is")
	private long availableDiskSpace;
	@Column
	@Attribute(field = false, description = "This flag indicates whether the index is being generated currently")
	private boolean indexing;
	@Column
	@Attribute(field = false, description = "This flag indicates whether the index should be delta indexed, i.e. no new index just the changes n the resources")
	private boolean delta;

	@Transient
	private long numDocsForSearchers;
	@Transient
	private Snapshot snapshot;
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
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

	public MultiSearcher getMultiSearcher() {
		return multiSearcher;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(final boolean open) {
		this.open = open;
	}

	public void setMultiSearcher(final MultiSearcher multiSearcher) {
		setOpen(multiSearcher != null);
		this.multiSearcher = multiSearcher;
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(final Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	public List<Long> getHashes() {
		return hashes;
	}

	public void setHashes(final List<Long> hashes) {
		this.hashes = hashes;
	}

	public List<Snapshot> getSnapshots() {
		if (snapshots == null) {
			snapshots = new ArrayList<Snapshot>();
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

	public long getAvailableDiskSpace() {
		return availableDiskSpace;
	}

	public void setAvailableDiskSpace(long availableDiskSpace) {
		this.availableDiskSpace = availableDiskSpace;
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
	public int compareTo(final IndexContext<?> other) {
		return getIndexName().compareTo(other.getIndexName());
	}

}