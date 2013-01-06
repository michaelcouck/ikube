package ikube.search;

import ikube.IConstants;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

/**
 * This class searches multiple fields in a Lucene index.
 * 
 * @see Search
 * @author Michael Couck
 * @since 02.09.08
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchMulti extends SearchSingle {

	public SearchMulti(final Searcher searcher) {
		this(searcher, IConstants.ANALYZER);
	}

	public SearchMulti(final Searcher searcher, final Analyzer analyzer) {
		super(searcher, analyzer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Query getQuery() throws ParseException {
		return MultiFieldQueryParser.parse(IConstants.VERSION, searchStrings, searchFields, analyzer);
	}

}
