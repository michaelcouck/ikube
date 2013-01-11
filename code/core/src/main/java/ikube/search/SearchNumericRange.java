package ikube.search;

import ikube.IConstants;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

/**
 * This search will search for a range of numbers. This assumes that the numbers were added to the index as a long and as a numeric field of
 * course.
 * 
 * @see Search
 * @author Michael Couck
 * @since 10.01.2012
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchNumericRange extends SearchSingle {

	public SearchNumericRange(final Searcher searcher) {
		this(searcher, IConstants.ANALYZER);
	}

	public SearchNumericRange(final Searcher searcher, final Analyzer analyzer) {
		super(searcher, analyzer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Query getQuery() throws ParseException {
		Double min = Double.parseDouble(searchStrings[0]);
		Double max = Double.parseDouble(searchStrings[1]);
		return NumericRangeQuery.newDoubleRange(searchFields[0], min, max, Boolean.TRUE, Boolean.TRUE);
	}

}