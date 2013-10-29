package ikube.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Searcher;

/**
 * This search is for all the fields in all the indexes.
 * 
 * @see Search
 * @author Michael Couck
 * @since 22.08.08
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchAll extends SearchMultiAll {

	public SearchAll(final Searcher searcher) {
		this(searcher, ANALYZER);
	}
	
	public SearchAll(final Searcher searcher, final Analyzer analyzer) {
		super(searcher, analyzer);
	}

}