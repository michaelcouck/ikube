package ikube.service;

import ikube.search.Search;

import java.util.List;
import java.util.Map;

/**
 * @see ISearchDelegate
 * @author Michael Couck
 * @since 22.05.11
 * @version 01.00
 */
@Deprecated
public class SearchDelegate implements ISearchDelegate {
	
	public SearchDelegate() {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Map<String, String>> execute(String indexName, Search search) {
		return search.execute();
	}

}
