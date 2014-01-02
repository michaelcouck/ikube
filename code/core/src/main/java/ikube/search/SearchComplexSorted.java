package ikube.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;

/**
 * @author Michael Couck
 * @version 01.00
 * @see Search
 * @since 02.07.2013
 */
public class SearchComplexSorted extends SearchComplex {

	public SearchComplexSorted(final IndexSearcher searcher) {
		this(searcher, ANALYZER);
	}

	public SearchComplexSorted(final IndexSearcher searcher, final Analyzer analyzer) {
		super(searcher, analyzer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TopDocs search(final Query query) throws IOException {
		Sort sort = getSort();
		// Filter filter = new QueryWrapperFilter(query);
		if (sort == null) {
			return searcher.search(query, /* filter,  */ firstResult + maxResults);
		}
		return searcher.search(query, /* filter,  */  firstResult + maxResults, sort);
	}

}