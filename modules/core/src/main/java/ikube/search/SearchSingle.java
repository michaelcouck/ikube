package ikube.search;

import ikube.IConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;

/**
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
	public List<Map<String, String>> execute() {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		if (searcher == null) {
			logger.warn("No searcher on any index, is an index created?");
		}
		long duration = 0;
		long totalHits = 0;

		try {
			int maxHits = firstResult + maxResults;
			Query query = getQueryParser(searchFields[0]).parse(searchStrings[0]);
			long start = System.currentTimeMillis();
			TopDocs topDocs = searcher.search(query, maxHits);
			duration = System.currentTimeMillis() - start;
			totalHits = topDocs.totalHits;
			for (int i = 0; i < topDocs.totalHits && i < maxHits; i++) {
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
					String fragment = getFragments(document, searchFields[0], query);
					result.put(IConstants.FRAGMENT, fragment);
				}
				results.add(result);
			}
		} catch (Exception e) {
			logger.error("Exception searching for string " + searchStrings[0] + " in searcher " + searcher, e);
		}

		// Add the search results size as a last result
		Map<String, String> statistics = new HashMap<String, String>();
		statistics.put(IConstants.TOTAL, Long.toString(totalHits));
		statistics.put(IConstants.DURATION, Long.toString(duration));
		results.add(statistics);

		return results;
	}

}