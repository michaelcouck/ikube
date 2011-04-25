package ikube.search;

import ikube.IConstants;

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
public class SearchMulti extends SearchSingle {

	public SearchMulti(final Searcher searcher) {
		super(searcher);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Query getQuery() throws ParseException {
		return MultiFieldQueryParser.parse(IConstants.VERSION, searchStrings, searchFields, IConstants.ANALYZER);
	}

}
