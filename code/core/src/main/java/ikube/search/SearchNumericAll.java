package ikube.search;

import ikube.IConstants;

import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.NumericUtils;

/**
 * This search is for all numeric searches that are different from normal searches. This search will search for the search string(s) in all
 * the fields.
 * 
 * @see Search
 * @author Michael Couck
 * @since 10.01.2012
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchNumericAll extends SearchMultiAll {

	public SearchNumericAll(final Searcher searcher) {
		this(searcher, IConstants.ANALYZER);
	}

	public SearchNumericAll(final Searcher searcher, final Analyzer analyzer) {
		super(searcher, analyzer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Query getQuery() throws ParseException {
		BooleanQuery booleanQuery = new BooleanQuery();

		searchFields = getFields(searcher);
		String[] newSearchStrings = new String[searchFields.length];
		int minLength = Math.min(searchStrings.length, newSearchStrings.length);
		System.arraycopy(searchStrings, 0, newSearchStrings, 0, minLength);
		String searchString = searchStrings != null && searchStrings.length > 0 ? searchStrings[0] : "";
		Arrays.fill(newSearchStrings, minLength, newSearchStrings.length, searchString);
		searchStrings = newSearchStrings;

		for (int i = 0; i < searchFields.length; i++) {
			String searchField = searchFields[i];
			String newSearchString = newSearchStrings[i];
			Long numeric = Long.parseLong(newSearchString);
			TermQuery numberQuery = new TermQuery(new Term(searchField, NumericUtils.doubleToPrefixCoded(numeric)));
			booleanQuery.add(numberQuery, BooleanClause.Occur.SHOULD);
		}

		return booleanQuery;
	}

}