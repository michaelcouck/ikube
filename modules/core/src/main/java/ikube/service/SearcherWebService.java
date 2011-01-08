package ikube.service;

import ikube.IConstants;
import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.search.SearchMulti;
import ikube.search.SearchMultiSorted;
import ikube.search.SearchSingle;
import ikube.toolkit.SerializationUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.log4j.Logger;
import org.apache.lucene.search.MultiSearcher;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Remote(ISearcherWebService.class)
@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService(name = ISearcherWebService.NAME, targetNamespace = ISearcherWebService.NAMESPACE, serviceName = ISearcherWebService.SERVICE)
public class SearcherWebService implements ISearcherWebService {

	private Logger logger = Logger.getLogger(this.getClass());
	private Map<String, SearchSingle> singleSearchers;
	private Map<String, SearchMulti> multiSearchers;
	private Map<String, SearchMultiSorted> multiSortedSearchers;

	public SearcherWebService() {
		this.singleSearchers = new HashMap<String, SearchSingle>();
		this.multiSearchers = new HashMap<String, SearchMulti>();
		this.multiSortedSearchers = new HashMap<String, SearchMultiSorted>();
		ListenerManager.addListener(new IListener() {
			@Override
			public void handleNotification(Event event) {
				if (event.getType().equals(Event.SEARCHER_OPENED)) {
					Object object = event.getObject();
					if (IndexContext.class.isAssignableFrom(object.getClass())) {
						IndexContext indexContext = (IndexContext) object;
						setMultiSearcher(indexContext);
					}
				}
			}
		});
	}

	protected void setMultiSearcher(IndexContext indexContext) {
		MultiSearcher multiSearcher = indexContext.getIndex().getMultiSearcher();
		SearchSingle searchSingle = new SearchSingle(multiSearcher);
		SearchMulti searchMulti = new SearchMulti(multiSearcher);
		SearchMultiSorted searchMultiSorted = new SearchMultiSorted(multiSearcher);
		singleSearchers.put(indexContext.getIndexName(), searchSingle);
		multiSearchers.put(indexContext.getIndexName(), searchMulti);
		multiSortedSearchers.put(indexContext.getIndexName(), searchMultiSorted);
		logger.info("Single searchers : " + singleSearchers);
		logger.info("Multi searchers : " + multiSearchers);
		logger.info("Multi sorted searchers : " + multiSortedSearchers);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchSingle(String indexName, String searchString, String searchField, boolean fragment, int start, int end) {
		try {
			SearchSingle searchSingle = this.singleSearchers.get(indexName);
			if (searchSingle == null) {
				List<Map<String, String>> results = new ArrayList<Map<String, String>>();
				Map<String, String> notification = new HashMap<String, String>();
				notification.put(IConstants.CONTENTS, "No index defined for name : " + indexName);
				results.add(notification);
				return SerializationUtilities.serialize(results);
			}
			searchSingle.setFirstResult(start);
			searchSingle.setFragment(fragment);
			searchSingle.setMaxResults(end);
			searchSingle.setSearchField(searchField);
			searchSingle.setSearchString(searchString);
			List<Map<String, String>> results = searchSingle.execute();
			return SerializationUtilities.serialize(results);
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchString, searchField, fragment, start,
					end);
			logger.error(message, e);
		}
		return "Exception thrown during search.";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchMulti(String indexName, String[] searchStrings, String[] searchFields, boolean fragment, int start, int end) {
		try {
			SearchMulti searchMulti = this.multiSearchers.get(indexName);
			if (searchMulti == null) {
				List<Map<String, String>> results = new ArrayList<Map<String, String>>();
				Map<String, String> notification = new HashMap<String, String>();
				notification.put(IConstants.CONTENTS, "No index defined for name : " + indexName);
				results.add(notification);
				return SerializationUtilities.serialize(results);
			}
			searchMulti.setFirstResult(start);
			searchMulti.setFragment(fragment);
			searchMulti.setMaxResults(end);
			searchMulti.setSearchField(searchFields);
			searchMulti.setSearchString(searchStrings);
			List<Map<String, String>> results = searchMulti.execute();
			return SerializationUtilities.serialize(results);
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, Arrays.asList(searchStrings),
					Arrays.asList(searchFields), fragment, start, end);
			logger.error(message, e);
		}
		return "Exception thrown during search.";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchMultiSorted(String indexName, String[] searchStrings, String[] searchFields, String[] sortFields, boolean fragment,
			int start, int end) {
		try {
			SearchMultiSorted searchMultiSorted = this.multiSortedSearchers.get(indexName);
			if (searchMultiSorted == null) {
				List<Map<String, String>> results = new ArrayList<Map<String, String>>();
				Map<String, String> notification = new HashMap<String, String>();
				notification.put(IConstants.CONTENTS, "No index defined for name : " + indexName);
				results.add(notification);
				return SerializationUtilities.serialize(results);
			}
			searchMultiSorted.setFirstResult(start);
			searchMultiSorted.setFragment(fragment);
			searchMultiSorted.setMaxResults(end);
			searchMultiSorted.setSearchField(searchFields);
			searchMultiSorted.setSearchString(searchStrings);
			searchMultiSorted.setSortField(sortFields);
			List<Map<String, String>> results = searchMultiSorted.execute();
			return SerializationUtilities.serialize(results);
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, Arrays.asList(searchStrings),
					Arrays.asList(searchFields), Arrays.asList(sortFields), fragment, start, end);
			logger.error(message, e);
		}
		return "Exception thrown during search.";
	}

}