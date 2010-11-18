package ikube.model;

import ikube.index.visitor.IndexableVisitor;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.MultiSearcher;

@Entity()
public class IndexContext extends Persistable implements Comparable<IndexContext> {

	private String name;
	private long maxAge;
	private String indexName;
	private long queueTimeout;

	/** Lucene properties. */
	private int mergeFactor;
	private int bufferedDocs;
	private double bufferSize;
	private int maxFieldLength;
	private boolean compoundFile;

	/** Jdbc properties. */
	private long batchSize;

	private long maxReadLength;
	/** Not mandatory, default implementation determined. */
	private String indexDirectoryPath;

	private List<Indexable<?>> indexables;
	/** These can't be sent down the wire. */
	private transient List<IndexableVisitor<Indexable<?>>> indexableVisitors;

	/** The time the action was started. */
	private long start;
	/** The name of the action that is being executed on this configuration. */
	private String action;
	/** The next id number to use for select. */
	private long idNumber;
	/** Whether this server is working. */
	private boolean working;

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
	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	@Transient
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Transient
	public long getIdNumber() {
		return idNumber;
	}

	public void setIdNumber(long idNumber) {
		this.idNumber = idNumber;
	}

	@Transient
	public boolean isWorking() {
		return working;
	}

	public void setWorking(boolean working) {
		this.working = working;
	}

	@Transient
	public List<IndexableVisitor<Indexable<?>>> getIndexableVisitors() {
		return indexableVisitors;
	}

	public void setIndexableVisitors(final List<IndexableVisitor<Indexable<?>>> indexableVisitors) {
		this.indexableVisitors = indexableVisitors;
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
