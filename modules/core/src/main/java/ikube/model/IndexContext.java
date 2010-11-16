package ikube.model;

import ikube.index.visitor.IndexableVisitor;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.MultiSearcher;

@Entity()
public class IndexContext extends Persistable {

	/** Set in the Spring configuration. */
	private String indexName;
	private long maxAge;
	private long queueTimeout;

	/** Lucene properties. */
	private boolean compoundFile;
	private int bufferedDocs;
	private int maxFieldLength;
	private int mergeFactor;
	private double bufferSize;

	/** Jdbc properties. */
	private long batchSize;

	private long maxReadLength;
	/** Not mandatory, default implementation determined. */
	private String indexDirectoryPath;

	/** These are passed to other servers in the cluster. So they are not transient but they should not be stored. */
	private String latestIndexDirectoryName;
	private String indexFileName;

	private List<Indexable<?>> indexables;
	private transient List<IndexableVisitor<Indexable<?>>> indexableVisitors;

	/** Dynamically set at runtime. */
	private String serverName;
	/** Can be null if there are no indexes running. */
	private transient IndexWriter indexWriter;
	/** Can be null if there is no index created. */
	private transient MultiSearcher multiSearcher;

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

	public long getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(final long batchSize) {
		this.batchSize = batchSize;
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

	@Transient
	public String getLatestIndexDirectoryName() {
		return latestIndexDirectoryName;
	}

	public void setLatestIndexDirectoryName(String latestIndexDirectoryName) {
		this.latestIndexDirectoryName = latestIndexDirectoryName;
	}

	@Transient
	public String getIndexFileName() {
		return indexFileName;
	}

	public void setIndexFileName(String indexFileName) {
		this.indexFileName = indexFileName;
	}

	public List<Indexable<?>> getIndexables() {
		return indexables;
	}

	public void setIndexables(final List<Indexable<?>> indexables) {
		this.indexables = indexables;
	}

	@Transient
	public List<IndexableVisitor<Indexable<?>>> getIndexableVisitors() {
		return indexableVisitors;
	}

	public void setIndexableVisitors(final List<IndexableVisitor<Indexable<?>>> indexableVisitors) {
		this.indexableVisitors = indexableVisitors;
	}

	@Transient
	public String getServerName() {
		return serverName;
	}

	public void setServerName(final String serverName) {
		this.serverName = serverName;
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
