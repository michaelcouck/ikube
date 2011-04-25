package ikube.service;

import ikube.IConstants;
import ikube.index.spatial.Coordinate;
import ikube.model.IndexContext;
import ikube.search.SearchMulti;
import ikube.search.SearchMultiAll;
import ikube.search.SearchMultiSorted;
import ikube.search.SearchSingle;
import ikube.search.SearchSpatial;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;
import ikube.toolkit.SerializationUtilities;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.log4j.Logger;
import org.apache.lucene.search.Searcher;

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

	private Map<String, IndexContext> indexContexts;

	public SearcherWebService() {
		indexContexts = new HashMap<String, IndexContext>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchSingle(final String indexName, final String searchString, final String searchField, final boolean fragment,
			final int start, final int end) {
		try {
			SearchSingle searchSingle = getSearch(SearchSingle.class, indexName);
			if (searchSingle != null) {
				searchSingle.setFirstResult(start);
				searchSingle.setFragment(fragment);
				searchSingle.setMaxResults(end);
				searchSingle.setSearchField(searchField);
				searchSingle.setSearchString(searchString);
				List<Map<String, String>> results = searchSingle.execute();
				return SerializationUtilities.serialize(results);
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchString, searchField, fragment, start,
					end);
			LOGGER.error(message, e);
		}
		return SerializationUtilities.serialize(getMessageResults(indexName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchMulti(final String indexName, final String[] searchStrings, final String[] searchFields, final boolean fragment,
			final int start, final int end) {
		try {
			SearchMulti searchMulti = getSearch(SearchMulti.class, indexName);
			if (searchMulti != null) {
				searchMulti.setFirstResult(start);
				searchMulti.setFragment(fragment);
				searchMulti.setMaxResults(end);
				searchMulti.setSearchField(searchFields);
				searchMulti.setSearchString(searchStrings);
				List<Map<String, String>> results = searchMulti.execute();
				return SerializationUtilities.serialize(results);
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, Arrays.asList(searchStrings),
					Arrays.asList(searchFields), fragment, start, end);
			LOGGER.error(message, e);
		}
		return SerializationUtilities.serialize(getMessageResults(indexName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchMultiSorted(final String indexName, final String[] searchStrings, final String[] searchFields,
			final String[] sortFields, final boolean fragment, final int start, final int end) {
		try {
			SearchMultiSorted searchMultiSorted = getSearch(SearchMultiSorted.class, indexName);
			if (searchMultiSorted != null) {
				searchMultiSorted.setFirstResult(start);
				searchMultiSorted.setFragment(fragment);
				searchMultiSorted.setMaxResults(end);
				searchMultiSorted.setSearchField(searchFields);
				searchMultiSorted.setSearchString(searchStrings);
				searchMultiSorted.setSortField(sortFields);
				List<Map<String, String>> results = searchMultiSorted.execute();
				return SerializationUtilities.serialize(results);
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, Arrays.asList(searchStrings),
					Arrays.asList(searchFields), Arrays.asList(sortFields), fragment, start, end);
			LOGGER.error(message, e);
		}
		return SerializationUtilities.serialize(getMessageResults(indexName));
	}

	@Override
	public String searchMultiAll(String indexName, String[] searchStrings, boolean fragment, int start, int end) {
		try {
			SearchMultiAll searchMultiAll = getSearch(SearchMultiAll.class, indexName);
			if (searchMultiAll != null) {
				searchMultiAll.setFirstResult(start);
				searchMultiAll.setFragment(fragment);
				searchMultiAll.setMaxResults(end);
				searchMultiAll.setSearchString(searchStrings);
				// searchMultiSorted.setSearchField(searchFields);
				// searchMultiSorted.setSortField(sortFields);
				List<Map<String, String>> results = searchMultiAll.execute();
				return SerializationUtilities.serialize(results);
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, Arrays.asList(searchStrings), fragment,
					start, end);
			LOGGER.error(message, e);
		}
		return SerializationUtilities.serialize(getMessageResults(indexName));
	}

	@Override
	public String searchSpacialMulti(String indexName, String[] searchStrings, String[] searchFields, boolean fragment, int start, int end,
			int distance, double latitude, double longitude) {
		try {
			SearchSpatial searchSpatial = getSearch(SearchSpatial.class, indexName);
			if (searchSpatial != null) {
				searchSpatial.setFirstResult(start);
				searchSpatial.setFragment(fragment);
				searchSpatial.setMaxResults(end);
				searchSpatial.setSearchString(searchStrings);
				searchSpatial.setSearchField(searchFields);
				searchSpatial.setCoordinate(new Coordinate(latitude, longitude));
				searchSpatial.setDistance(distance);
				// searchMultiSorted.setSortField(sortFields);
				List<Map<String, String>> results = searchSpatial.execute();
				return SerializationUtilities.serialize(results);
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, Arrays.asList(searchStrings), fragment,
					start, end, latitude, longitude, distance);
			LOGGER.error(message, e);
		}
		return SerializationUtilities.serialize(getMessageResults(indexName));
	}

	@SuppressWarnings("unchecked")
	protected <T> T getSearch(Class<?> klass, String indexName) throws Exception {
		IndexContext indexContext = this.indexContexts.get(indexName);
		if (indexContext == null) {
			Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
			for (IndexContext context : indexContexts.values()) {
				if (context.getIndexName().equals(indexName)) {
					this.indexContexts.put(indexName, context);
					indexContext = context;
					break;
				}
			}
		}
		if (indexContext != null) {
			if (indexContext.getIndex().getMultiSearcher() != null) {
				Constructor<?> constructor = klass.getConstructor(Searcher.class);
				return (T) constructor.newInstance(indexContext.getIndex().getMultiSearcher());
			}
		}
		return null;
	}

	protected List<Map<String, String>> getMessageResults(String indexName) {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		Map<String, String> notification = new HashMap<String, String>();
		notification.put(IConstants.CONTENTS, "No index defined for name : " + indexName);
		notification.put(IConstants.FRAGMENT, "Or exception thrown during search : " + indexName);
		results.add(notification);
		return results;
	}
}