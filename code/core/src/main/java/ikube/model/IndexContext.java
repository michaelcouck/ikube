package ikube.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;

/**
 * This is the context for a single index. It has the properties that define the index like what it is going to index, i.e. the databases,
 * intranets etc., and properties relating to the Lucene index. This object acts a like the command in the 'Command Pattern' as in this
 * context is passed to handlers that will perform certain logic based on the properties of this context.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class IndexContext<T> extends Indexable<T> implements Comparable<IndexContext<?>> {

	private static final transient Logger LOGGER = Logger.getLogger(IndexContext.class);

	/** Can be null if there are no indexes running. */
	@Transient
	private transient IndexWriter indexWriter;
	/** Can be null if there is no index created. */
	@Transient
	private transient MultiSearcher multiSearcher;

	/** The maximum age of the index defined in minutes. */
	private long maxAge;
	/** The delay between documents being indexed, slows the indexing down. */
	private long throttle;

	/** Lucene properties. */
	private int mergeFactor;
	private int bufferedDocs;
	private double bufferSize;
	private int maxFieldLength;
	private boolean compoundFile;

	/** Jdbc properties. */
	private int batchSize;
	/** Internet properties. */
	private int internetBatchSize;

	/** The maximum length of a document that can be read. */
	private long maxReadLength;
	/** The path to the index directory, either relative or absolute. */
	private String indexDirectoryPath;
	/** The path to the backup index directory, either relative or absolute. */
	private String indexDirectoryPathBackup;

	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "indexContext", fetch = FetchType.EAGER)
	private List<Snapshot> snapshots = new ArrayList<Snapshot>();

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

	public List<Indexable<?>> getIndexables() {
		return super.getChildren();
	}

	public void setIndexables(final List<Indexable<?>> indexables) {
		super.setChildren(indexables);
	}

	public IndexWriter getIndexWriter() {
		return indexWriter;
	}

	public void setIndexWriter(final IndexWriter indexWriter) {
		this.indexWriter = indexWriter;
	}

	public MultiSearcher getMultiSearcher() {
		return multiSearcher;
	}

	public void setMultiSearcher(final MultiSearcher multiSearcher) {
		// We'll close the current searcher if it is not already closed
		if (this.multiSearcher != null && !this.multiSearcher.equals(multiSearcher)) {
			try {
				LOGGER.info("Searcher not closed, will close now : " + this.multiSearcher);
				Searchable[] searchables = this.multiSearcher.getSearchables();
				if (searchables != null) {
					for (Searchable searchable : searchables) {
						try {
							searchable.close();
						} catch (Exception e) {
							LOGGER.error("Exception closing the searchable : ", e);
						}
					}
				}
			} catch (Exception e) {
				LOGGER.error("Exception closing the searcher : " + this.multiSearcher, e);
			}
		}
		this.multiSearcher = multiSearcher;
	}

	public List<Snapshot> getSnapshots() {
		return snapshots;
	}

	public void setSnapshots(List<Snapshot> snapshots) {
		this.snapshots = snapshots;
	}
	
	public Snapshot getLastSnapshot() {
		return snapshots.size() > 0 ? snapshots.get(snapshots.size() - 1) : null;
	}

	@Override
	public int compareTo(final IndexContext<?> other) {
		return getIndexName().compareTo(other.getIndexName());
	}

}