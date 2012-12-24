package ikube.search;

import ikube.IConstants;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

/**
 * @see SearchAdvanced
 * @author Michael Couck
 * @since 22.08.08
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchAdvancedAll extends SearchAdvanced {

	public SearchAdvancedAll(final Searcher searcher) {
		this(searcher, IConstants.ANALYZER);
	}

	public SearchAdvancedAll(final Searcher searcher, final Analyzer analyzer) {
		super(searcher, analyzer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Query getQuery() throws ParseException {
		searchFields = getFields(searcher);
		return super.getQuery();
	}

}