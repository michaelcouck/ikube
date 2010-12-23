package ikube.search;

import java.io.IOException;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;

/**
 * Searches a single field in an index.
 * 
 * @see Search
 * @author Michael Couck
 * @since 22.08.08
 * @version 01.00
 */
public class SearchSingle extends Search {

	public SearchSingle(Searcher searcher) {
		super(searcher);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TopDocs search(Query query) throws IOException {
		return searcher.search(query, firstResult + maxResults);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Query getQuery() throws ParseException {
		return getQueryParser(searchFields[0]).parse(searchStrings[0]);
	}

}