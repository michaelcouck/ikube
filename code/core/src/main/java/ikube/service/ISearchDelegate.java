package ikube.service;

import java.util.List;
import java.util.Map;

import ikube.index.handler.IDocumentDelegate;
import ikube.search.Search;

/**
 * This interface is for Spring to intercept. Please refer to the JavaDoc in the {@link IDocumentDelegate} class for an explanation.
 * 
 * @author Michael Couck
 * @since 22.05.11
 * @version 01.00
 */
public interface ISearchDelegate {

	/**
	 * This method executes the search and typically gets intercepted by the monitoring intercepter
	 * 
	 * @param search
	 *            the search object to execute
	 * @param indexName
	 *            the name of the index to search
	 * @return the result from the search
	 */
	public List<Map<String, String>> execute(String indexName, Search search);

}
