package ikube.search;

import ikube.model.Search;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is the 'service' layer for searching.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface ISearcherService {

	/** Basic */
	ArrayList<HashMap<String, String>> search(final String indexName, final String[] searchStrings, final String[] searchFields, final boolean fragment,
			final int firstResult, final int maxResults);

	/** Sorted */
	ArrayList<HashMap<String, String>> search(final String indexName, final String[] searchStrings, final String[] searchFields, final String[] sortFields,
			final boolean fragment, final int firstResult, final int maxResults);

	/** Sorted and typed */
	ArrayList<HashMap<String, String>> search(final String indexName, final String[] searchStrings, final String[] searchFields, final String[] typeFields,
			final String[] sortFields, final boolean fragment, final int firstResult, final int maxResults);

	/** Geospatial */
	ArrayList<HashMap<String, String>> search(final String indexName, final String[] searchStrings, final String[] searchFields, final String[] typeFields,
			final boolean fragment, final int firstResult, final int maxResults, final int distance, final double latitude, final double longitude);

	/** Search json */
	Search search(final Search search);
	
	/** Every field in every index */
	Search searchAll(final Search search);

}