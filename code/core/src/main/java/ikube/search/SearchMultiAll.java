package ikube.search;

import ikube.IConstants;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searchable;
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
public class SearchMultiAll extends SearchMulti {

	public SearchMultiAll(final Searcher searcher) {
		super(searcher);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Query getQuery() throws ParseException {
		Searchable[] searchables = ((MultiSearcher) searcher).getSearchables();
		Set<String> searchFieldNames = new TreeSet<String>();
		for (Searchable searchable : searchables) {
			Collection<String> fieldNames = ((IndexSearcher) searchable).getIndexReader().getFieldNames(FieldOption.ALL);
			searchFieldNames.addAll(fieldNames);
		}
		searchFields = searchFieldNames.toArray(new String[searchFieldNames.size()]);
		String[] newSearchStrings = new String[searchFields.length];
		System.arraycopy(searchStrings, 0, newSearchStrings, 0, searchStrings.length);
		Arrays.fill(newSearchStrings, searchStrings.length, newSearchStrings.length, searchStrings[0]);
		searchStrings = newSearchStrings;
		return MultiFieldQueryParser.parse(IConstants.VERSION, searchStrings, searchFields, IConstants.ANALYZER);
	}

}
