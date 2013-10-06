package ikube.search;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

/**
 * @see Search
 * @author Michael Couck
 * @since 02.07.2013
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchComplexSorted extends SearchComplex {

	public SearchComplexSorted(final Searcher searcher) {
		this(searcher, ANALYZER);
	}

	public SearchComplexSorted(final Searcher searcher, final Analyzer analyzer) {
		super(searcher, analyzer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TopDocs search(final Query query) throws IOException {
		Sort sort = getSort(query);
		if (sort == null) {
			return super.search(query);
		}
		Filter filter = new QueryWrapperFilter(query);
		return searcher.search(query, filter, firstResult + maxResults, sort);
	}

}