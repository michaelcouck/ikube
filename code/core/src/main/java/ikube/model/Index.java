package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.MultiSearcher;

/**
 * @author Michael Couck
 * @since 28.12.10
 * @version 01.00
 */
@Entity()
public class Index extends Persistable {

	/** Can be null if there are no indexes running. */
	@Transient
	private transient IndexWriter indexWriter;
	/** Can be null if there is no index created. */
	@Transient
	private transient MultiSearcher multiSearcher;

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

	public String toString() {
		final StringBuilder builder = new StringBuilder('[');
		builder.append(getId()).append(", ");
		builder.append(']');
		return builder.toString();
	}

}
