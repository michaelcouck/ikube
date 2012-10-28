package ikube.search;

import ikube.IConstants;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.util.ReaderUtil;

/**
 * @see Search
 * @author Michael Couck
 * @since 02.10.11
 * @version 01.00
 */
public class SearchSpatialAll extends SearchSpatial {

	public SearchSpatialAll(final Searcher searcher) {
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
			Collection<String> fieldNames = ReaderUtil.getIndexedFields(((IndexSearcher) searchable).getIndexReader());
			searchFieldNames.addAll(fieldNames);
		}
		searchFields = searchFieldNames.toArray(new String[searchFieldNames.size()]);
		String[] newSearchStrings = new String[searchFields.length];
		int minLength = Math.min(searchStrings.length, newSearchStrings.length);
		System.arraycopy(searchStrings, 0, newSearchStrings, 0, minLength);
		String searchString = searchStrings != null && searchStrings.length > 0 ? searchStrings[0] : "";
		Arrays.fill(newSearchStrings, minLength, newSearchStrings.length, searchString);
		searchStrings = newSearchStrings;
		return MultiFieldQueryParser.parse(IConstants.VERSION, searchStrings, searchFields, IConstants.ANALYZER);
	}

}
