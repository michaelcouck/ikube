package ikube.search;

import ikube.IConstants;

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
 * @see Search
 * @author Michael Couck
 * @since 10.01.2012
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchComplex extends SearchMultiAll {

	public SearchComplex(final Searcher searcher) {
		this(searcher, IConstants.ANALYZER);
	}

	public SearchComplex(final Searcher searcher, final Analyzer analyzer) {
		super(searcher, analyzer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Query getQuery() throws ParseException {
		BooleanQuery booleanQuery = new BooleanQuery();
		for (int i = 0; i < searchStrings.length; i++) {
			final String typeField = typeFields[i];
			final String searchField = searchFields[i];
			final String searchString = searchStrings[i];
			Query query = null;
			if (TypeField.STRING.equals(typeField)) {
				query = getQueryParser(searchField).parse(searchString);
			} else if (TypeField.NUMERIC.equals(typeField)) {
				Long numeric = Long.parseLong(searchField);
				query = new TermQuery(new Term(searchField, NumericUtils.longToPrefixCoded(numeric)));
			}
			booleanQuery.add(query, BooleanClause.Occur.SHOULD);
		}
		return booleanQuery;
	}

}