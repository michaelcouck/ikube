package ikube.search;

import ikube.IConstants;

import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

/**
 * @see Search
 * @author Michael Couck
 * @since 02.10.11
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchSpatialAll extends SearchSpatial {

	public SearchSpatialAll(final Searcher searcher) {
		this(searcher, ANALYZER);
	}

	public SearchSpatialAll(final Searcher searcher, final Analyzer analyzer) {
		super(searcher, analyzer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Query getQuery() throws ParseException {
		searchFields = getFields(searcher);
		String[] newSearchStrings = new String[searchFields.length];
		int minLength = Math.min(searchStrings.length, newSearchStrings.length);
		System.arraycopy(searchStrings, 0, newSearchStrings, 0, minLength);
		String searchString = searchStrings != null && searchStrings.length > 0 ? searchStrings[0] : "";
		Arrays.fill(newSearchStrings, minLength, newSearchStrings.length, searchString);
		searchStrings = newSearchStrings;
		return MultiFieldQueryParser.parse(IConstants.VERSION, searchStrings, searchFields, analyzer);
	}

}
