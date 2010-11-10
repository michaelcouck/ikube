package ikube.search;

import ikube.IConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;



/**
 * @see Search
 * @author Michael Couck
 * @since 02.09.08
 * @version 01.00
 */
public class SearchMulti extends Search {

	/** The search string that we are looking for. */
	private String[] searchStrings;
	/** The fields in index to add to the search. */
	private String[] searchFields;

	/**
	 * Constructor takes the searcher and the search string to look for. For each search one of these lightweight classes is created
	 * specifying the fragment parameter, the start and end of the results to return and the maximum results to return.
	 *
	 * @param configuration
	 *            the configuration for the search
	 * @param searchStrings
	 *            the strings that we are looking for in the index
	 * @param searchfields
	 *            the fields in the index to search
	 * @param fragment
	 *            whether to fragment the results like ...the best <b>fragment</b> of them all...
	 * @param firstResult
	 *            the start in the index results. This facilitates paging from clients
	 * @param maxResults
	 *            the end in the results to return. Also part of the paging functionality
	 */
	public SearchMulti(Searcher searcher) {
		super(searcher);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Map<String, String>> execute() {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		long duration = 0;
		long totalHits = 0;

		try {
			Query query = MultiFieldQueryParser.parse(IConstants.VERSION, searchStrings, searchFields, IConstants.ANALYZER);
			long start = System.currentTimeMillis();
			TopDocs topDocs = searcher.search(query, firstResult + maxResults);
			duration = System.currentTimeMillis() - start;
			totalHits = topDocs.totalHits;
			for (int i = 0; i < topDocs.totalHits; i++) {
				if (i < firstResult) {
					continue;
				}
				Map<String, String> result = new HashMap<String, String>();
				Document document = searcher.doc(topDocs.scoreDocs[i].doc);
				float score = topDocs.scoreDocs[i].score;
				String index = Integer.toString(topDocs.scoreDocs[i].doc);
				result.put(IConstants.INDEX, index);
				result.put(IConstants.SCORE, Float.toString(score));

				addFieldsToResults(document, result);

				if (fragment) {
					StringBuilder builder = new StringBuilder();
					for (String searchField : searchFields) {
						String fragment = getFragments(document, searchField, query);
						if (fragment != null) {
							builder.append(fragment);
						}
					}
					result.put(IConstants.FRAGMENT, builder.toString());
				}
				results.add(result);
			}
		} catch (Exception e) {
			logger.error("Exception searching for strings in searcher " + searcher, e);
		}

		// Add the search results size as a last result
		Map<String, String> statistics = new HashMap<String, String>();
		statistics.put(IConstants.TOTAL, Long.toString(totalHits));
		statistics.put(IConstants.DURATION, Long.toString(duration));
		results.add(statistics);

		return results;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void setSearchString(String... searchStrings) {
		this.searchStrings = searchStrings;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void setSearchField(String... searchFields) {
		this.searchFields = searchFields;
	}

}
