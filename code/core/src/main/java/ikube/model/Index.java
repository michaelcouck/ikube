package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.store.Directory;

/**
 * @author Michael Couck
 * @since 28.12.10
 * @version 01.00
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Index extends Persistable {

	/** Can be null if there are no indexes running. */
	@Transient
	private transient IndexWriter	indexWriter;
	/** This is the latest directory from the indexing process. */
	@Transient
	private transient Directory		directory;
	/** Can be null if there is no index created. */
	@Transient
	private transient MultiSearcher	multiSearcher;

	public IndexWriter getIndexWriter() {
		return indexWriter;
	}

	public void setIndexWriter(final IndexWriter indexWriter) {
		this.indexWriter = indexWriter;
	}

	public Directory getDirectory() {
		return directory;
	}

	public void setDirectory(final Directory directory) {
		this.directory = directory;
	}

	public MultiSearcher getMultiSearcher() {
		return multiSearcher;
	}

	public void setMultiSearcher(final MultiSearcher multiSearcher) {
		this.multiSearcher = multiSearcher;
	}

}
