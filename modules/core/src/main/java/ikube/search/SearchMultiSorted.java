package ikube.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;

/**
 * @author Michael Couck
 * @since 02.09.08
 * @version 01.00
 */
public class SearchMultiSorted extends SearchMulti {

	public SearchMultiSorted(Searcher searcher) {
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
		Filter filter = new QueryWrapperFilter(query);
		Sort sort = new Sort();
		SortField[] fields = new SortField[sortFields.length];
		int sortFieldIndex = 0;
		for (String sortFieldName : sortFields) {
			SortField sortField = new SortField(sortFieldName, SortField.STRING);
			fields[sortFieldIndex] = sortField;
			sortFieldIndex++;
		}
		sort.setSort(fields);
		return searcher.search(query, filter, firstResult + maxResults, sort);
	}

}
