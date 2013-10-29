package ikube.search;

import ikube.IConstants;

import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

/**
 * This class searches all the fields in the index with the specified search string. This is a convenience class that will dynamically get
 * all the fields in the index and add them to the fields to search rather than explicitly setting the fields to search. Typically the usage
 * of this class will be having one search string and the index name. In this case the search string will be duplicated for each search
 * field found in the index.
 * 
 * @see Search
 * @author Michael Couck
 * @since 02.09.08
 * @version 01.00
 */
@Deprecated
public class SearchMultiAll extends SearchMulti {

	public SearchMultiAll(final Searcher searcher) {
		this(searcher, ANALYZER);
	}

	public SearchMultiAll(final Searcher searcher, final Analyzer analyzer) {
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
