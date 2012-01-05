package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;

/**
 * @author Michael Couck
 * @since 28.12.10
 * @version 01.00
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Index extends Persistable {

	@Transient
	private static final transient Logger LOGGER = Logger.getLogger(Index.class);

	/** Can be null if there are no indexes running. */
	@Transient
	private transient IndexWriter indexWriter;
	/** Can be null if there is no index created. */
	@Transient
	private transient MultiSearcher multiSearcher;

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
		if (this.multiSearcher != null) {
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

}