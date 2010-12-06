package ikube.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.MultiSearcher;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public class IndexContext extends Persistable implements Comparable<IndexContext> {

	private String name;
	private long maxAge;
	private String indexName;
	private long queueTimeout;
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

	private long maxReadLength;
	/** Not mandatory, default implementation determined. */
	private String indexDirectoryPath;

	private List<Indexable<?>> indexables;

	/** Can be null if there are no indexes running. */
	private transient IndexWriter indexWriter;
	/** Can be null if there is no index created. */
	private transient MultiSearcher multiSearcher;

	public String getName() {
		return name;
	}

	public void setName(final String serverName) {
		this.name = serverName;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(final String indexName) {
		this.indexName = indexName;
	}

	public long getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(final long maxAge) {
		this.maxAge = maxAge;
	}

	public long getQueueTimeout() {
		return queueTimeout;
	}

	public void setQueueTimeout(final long queueTimeout) {
		this.queueTimeout = queueTimeout;
	}

	public long getThrottle() {
		return throttle;
	}

	public void setThrottle(long throttle) {
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

	public void setInternetBatchSize(int internetBatchSize) {
		this.internetBatchSize = internetBatchSize;
	}

	public long getMaxReadLength() {
		return maxReadLength;
	}

	public void setMaxReadLength(final long maxOneShotReadLength) {
		this.maxReadLength = maxOneShotReadLength;
	}

	public String getIndexDirectoryPath() {
		return indexDirectoryPath;
	}

	public void setIndexDirectoryPath(final String indexDirectoryPath) {
		this.indexDirectoryPath = indexDirectoryPath;
	}

	public List<Indexable<?>> getIndexables() {
		return indexables;
	}

	public void setIndexables(final List<Indexable<?>> indexables) {
		this.indexables = indexables;
	}

	@Override
	public int compareTo(IndexContext o) {
		return getName().compareTo(o.getName());
	}

	@Transient
	public IndexWriter getIndexWriter() {
		return indexWriter;
	}

	public void setIndexWriter(final IndexWriter indexWriter) {
		this.indexWriter = indexWriter;
	}

	@Transient
	public MultiSearcher getMultiSearcher() {
		return multiSearcher;
	}

	public void setMultiSearcher(final MultiSearcher multiSearcher) {
		this.multiSearcher = multiSearcher;
	}

}
