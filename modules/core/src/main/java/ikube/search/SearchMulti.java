package ikube.search;

import ikube.IConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;

/**
 * @author Michael Couck
 * @since 02.09.08
 * @version 01.00
 */
public class SearchMulti extends Search {

	public SearchMulti(Searcher searcher) {
		super(searcher);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Map<String, String>> execute() {
		long duration = 0;
		long totalHits = 0;
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();

		try {
			Query query = getQuery();
			long start = System.currentTimeMillis();
			TopDocs topDocs = search(query);
			duration = System.currentTimeMillis() - start;
			totalHits = topDocs.totalHits;
			results.addAll(getResults(topDocs, query));
		} catch (Exception e) {
			logger.error("Exception searching in searcher : " + searcher, e);
		}

		// Add the search results size as a last result
		addStatistics(results, totalHits, duration);

		return results;
	}

	protected TopDocs search(Query query) throws IOException {
		return searcher.search(query, firstResult + maxResults);
	}

	protected void addStatistics(List<Map<String, String>> results, long totalHits, long duration) {
		// Add the search results size as a last result
		Map<String, String> statistics = new HashMap<String, String>();
		statistics.put(IConstants.TOTAL, Long.toString(totalHits));
		statistics.put(IConstants.DURATION, Long.toString(duration));
		results.add(statistics);
	}

	protected Query getQuery() throws ParseException {
		return MultiFieldQueryParser.parse(IConstants.VERSION, searchStrings, searchFields, IConstants.ANALYZER);
	}

	protected List<Map<String, String>> getResults(TopDocs topDocs, Query query) {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();

		long totalHits = topDocs.totalHits;
		long scoreHits = topDocs.scoreDocs.length;
		for (int i = 0; i < totalHits && i < scoreHits; i++) {
			if (i < firstResult) {
				continue;
			}
			try {
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
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return results;
	}

}
