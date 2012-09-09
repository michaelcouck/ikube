package ikube.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Michael Couck
 * @since 22.07.12
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Snapshot extends Persistable {

	private long numDocs;
	private long indexSize;
	private long timestamp;
	private Date latestIndexTimestamp;
	private long docsPerMinute;
	private long searchesPerMinute;
	private long totalSearches;

	@ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST }, fetch = FetchType.EAGER)
	private IndexContext<?> indexContext;

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getIndexSize() {
		return indexSize;
	}

	public void setIndexSize(long indexSize) {
		this.indexSize = indexSize;
	}

	public long getNumDocs() {
		return numDocs;
	}

	public void setNumDocs(long numDocs) {
		this.numDocs = numDocs;
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

	public IndexContext<?> getIndexContext() {
		return indexContext;
	}

	public void setIndexContext(IndexContext<?> indexContext) {
		this.indexContext = indexContext;
	}

	public long getTotalSearches() {
		return totalSearches;
	}

	public void setTotalSearches(long totalSearches) {
		this.totalSearches = totalSearches;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}