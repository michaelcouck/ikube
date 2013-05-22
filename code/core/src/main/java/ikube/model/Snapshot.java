package ikube.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * A snapshot is the metrics of a single index context in time, including some operating system details.
 * 
 * @author Michael Couck
 * @since 22.07.12
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries(value = { @NamedQuery(name = Snapshot.SELECT_SNAPSHOTS_ORDER_BY_TIMESTAMP_DESC, query = Snapshot.SELECT_SNAPSHOTS_ORDER_BY_TIMESTAMP_DESC) })
public class Snapshot extends Persistable {

	public static final String SELECT_SNAPSHOTS_ORDER_BY_TIMESTAMP_DESC = "select s from Snapshot as s where s.indexContext = :indexContext order by s.timestamp desc";

	/** Index context details. */
	@Column
	private long numDocs;
	@Column
	private long indexSize;
	@Column
	private Date latestIndexTimestamp;
	@Column
	private long docsPerMinute;
	@Column
	private long searchesPerMinute;
	@Column
	private long totalSearches;
	@Column
	private String indexContext;
	
	/** Server details for posterity. */
	@Column
	private double systemLoad;
	@Column
	private double availableProcessors;

	public long getNumDocs() {
		return numDocs;
	}

	public void setNumDocs(long numDocs) {
		this.numDocs = numDocs;
	}

	public long getIndexSize() {
		return indexSize;
	}

	public void setIndexSize(long indexSize) {
		this.indexSize = indexSize;
	}

	public Date getLatestIndexTimestamp() {
		return latestIndexTimestamp;
	}

	public void setLatestIndexTimestamp(Date latestIndexTimestamp) {
		this.latestIndexTimestamp = latestIndexTimestamp;
	}

	public long getDocsPerMinute() {
		return docsPerMinute;
	}

	public void setDocsPerMinute(long docsPerMinute) {
		this.docsPerMinute = docsPerMinute;
	}

	public long getSearchesPerMinute() {
		return searchesPerMinute;
	}

	public void setSearchesPerMinute(long searchesPerMinute) {
		this.searchesPerMinute = searchesPerMinute;
	}

	public long getTotalSearches() {
		return totalSearches;
	}

	public void setTotalSearches(long totalSearches) {
		this.totalSearches = totalSearches;
	}

	public double getSystemLoad() {
		return systemLoad;
	}

	public void setSystemLoad(double systemLoad) {
		this.systemLoad = systemLoad;
	}

	public double getAvailableProcessors() {
		return availableProcessors;
	}

	public void setAvailableProcessors(double availableProcessors) {
		this.availableProcessors = availableProcessors;
	}

	public String getIndexContext() {
		return indexContext;
	}

	public void setIndexContext(String indexContext) {
		this.indexContext = indexContext;
	}

	public static String getSelectSnapshotsOrderByTimestampDesc() {
		return SELECT_SNAPSHOTS_ORDER_BY_TIMESTAMP_DESC;
	}

}