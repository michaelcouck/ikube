package ikube.search;

import org.apache.lucene.search.Searcher;

/**
 * TODO Implement this class:
 * 
 * This search is for all the fields in all the indexes.
 * 
 * @see Search
 * @author Michael Couck
 * @since 22.08.08
 * @version 01.00
 */
public class SearchAll extends SearchMultiAll {

	public SearchAll(final Searcher searcher) {
		super(searcher);
	}

}