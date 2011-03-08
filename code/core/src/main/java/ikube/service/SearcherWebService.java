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

	private static final Logger LOGGER = Logger.getLogger(SearcherWebService.class);
	private transient final Map<String, SearchSingle> singleSearchers;
	private transient final Map<String, SearchMulti> multiSearchers;
	private transient final Map<String, SearchMultiSorted> multiSortedSearchers;

	public SearcherWebService() {
		this.singleSearchers = new HashMap<String, SearchSingle>();
		this.multiSearchers = new HashMap<String, SearchMulti>();
		this.multiSortedSearchers = new HashMap<String, SearchMultiSorted>();
		ListenerManager.addListener(new IListener() {
			@Override
			public void handleNotification(final Event event) {
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

	protected void setMultiSearcher(final IndexContext indexContext) {
		MultiSearcher multiSearcher = indexContext.getIndex().getMultiSearcher();
		SearchSingle searchSingle = new SearchSingle(multiSearcher);
		SearchMulti searchMulti = new SearchMulti(multiSearcher);
		SearchMultiSorted searchMultiSorted = new SearchMultiSorted(multiSearcher);
		singleSearchers.put(indexContext.getIndexName(), searchSingle);
		multiSearchers.put(indexContext.getIndexName(), searchMulti);
		multiSortedSearchers.put(indexContext.getIndexName(), searchMultiSorted);
		LOGGER.info("Single searchers : " + singleSearchers);
		LOGGER.info("Multi searchers : " + multiSearchers);
		LOGGER.info("Multi sorted searchers : " + multiSortedSearchers);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchSingle(final String indexName, final String searchString, final String searchField, final boolean fragment,
			final int start, final int end) {
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
			LOGGER.error(message, e);
		}
		return "Exception thrown during search.";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchMulti(final String indexName, final String[] searchStrings, final String[] searchFields, final boolean fragment,
			final int start, final int end) {
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
			LOGGER.error(message, e);
		}
		return "Exception thrown during search.";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchMultiSorted(final String indexName, final String[] searchStrings, final String[] searchFields,
			final String[] sortFields, final boolean fragment, final int start, final int end) {
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
			LOGGER.error(message, e);
		}
		return "Exception thrown during search.";
	}

}