package ikube.service;

import ikube.IConstants;
import ikube.index.spatial.Coordinate;
import ikube.model.IndexContext;
import ikube.search.SearchMulti;
import ikube.search.SearchMultiAll;
import ikube.search.SearchMultiSorted;
import ikube.search.SearchSingle;
import ikube.search.SearchSpatial;
import ikube.search.SearchSpatialAll;
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
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.log4j.Logger;
import org.apache.lucene.search.Searcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * @see ISearcherWebService
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Remote(ISearcherWebService.class)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
@WebService(name = ISearcherWebService.NAME, targetNamespace = ISearcherWebService.NAMESPACE, serviceName = ISearcherWebService.SERVICE)
public class SearcherWebService implements ISearcherWebService {

	private static final Logger LOGGER = Logger.getLogger(SearcherWebService.class);

	@Value("${searcher.web.service.port}")
	private int port;
	@Value("${searcher.web.service.path}")
	private String path;
	@Autowired
	private SearchDelegate searchDelegate;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@WebMethod
	@WebResult(name = "result")
	public String searchSingle(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchString") final String searchString, @WebParam(name = "searchField") final String searchField,
			@WebParam(name = "fragment") final boolean fragment, @WebParam(name = "firstResult") final int firstResult,
			@WebParam(name = "maxResults") final int maxResults) {
		try {
			SearchSingle searchSingle = getSearch(SearchSingle.class, indexName);
			if (searchSingle != null) {
				searchSingle.setFirstResult(firstResult);
				searchSingle.setFragment(fragment);
				searchSingle.setMaxResults(maxResults);
				searchSingle.setSearchField(searchField);
				searchSingle.setSearchString(searchString);
				List<Map<String, String>> results = searchDelegate.execute(indexName, searchSingle);
				return SerializationUtilities.serialize(results);
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchString, searchField, fragment,
					firstResult, maxResults);
			LOGGER.error(message, e);
		}
		return SerializationUtilities.serialize(getMessageResults(indexName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@WebMethod
	@WebResult(name = "result")
	public String searchMulti(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "searchFields") final String[] searchFields,
			@WebParam(name = "fragment") final boolean fragment, @WebParam(name = "firstResult") final int firstResult,
			@WebParam(name = "maxResults") final int maxResults) {
		try {
			SearchMulti searchMulti = getSearch(SearchMulti.class, indexName);
			if (searchMulti != null) {
				searchMulti.setFirstResult(firstResult);
				searchMulti.setFragment(fragment);
				searchMulti.setMaxResults(maxResults);
				searchMulti.setSearchField(searchFields);
				searchMulti.setSearchString(searchStrings);
				List<Map<String, String>> results = searchDelegate.execute(indexName, searchMulti);
				return SerializationUtilities.serialize(results);
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, Arrays.asList(searchStrings),
					Arrays.asList(searchFields), fragment, firstResult, maxResults);
			LOGGER.error(message, e);
		}
		return SerializationUtilities.serialize(getMessageResults(indexName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@WebMethod
	@WebResult(name = "result")
	public String searchMultiSorted(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "searchFields") final String[] searchFields,
			@WebParam(name = "sortFields") final String[] sortFields, @WebParam(name = "fragment") final boolean fragment,
			@WebParam(name = "firstResult") final int firstResult, @WebParam(name = "maxResults") final int maxResults) {
		try {
			SearchMultiSorted searchMultiSorted = getSearch(SearchMultiSorted.class, indexName);
			if (searchMultiSorted != null) {
				searchMultiSorted.setFirstResult(firstResult);
				searchMultiSorted.setFragment(fragment);
				searchMultiSorted.setMaxResults(maxResults);
				searchMultiSorted.setSearchField(searchFields);
				searchMultiSorted.setSearchString(searchStrings);
				searchMultiSorted.setSortField(sortFields);
				List<Map<String, String>> results = searchDelegate.execute(indexName, searchMultiSorted);
				return SerializationUtilities.serialize(results);
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, Arrays.asList(searchStrings),
					Arrays.asList(searchFields), Arrays.asList(sortFields), fragment, firstResult, maxResults);
			LOGGER.error(message, e);
		}
		return SerializationUtilities.serialize(getMessageResults(indexName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@WebMethod
	@WebResult(name = "result")
	public String searchMultiAll(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "fragment") final boolean fragment,
			@WebParam(name = "firstResult") final int firstResult, @WebParam(name = "maxResults") final int maxResults) {
		try {
			SearchMultiAll searchMultiAll = getSearch(SearchMultiAll.class, indexName);
			if (searchMultiAll != null) {
				searchMultiAll.setFirstResult(firstResult);
				searchMultiAll.setFragment(fragment);
				searchMultiAll.setMaxResults(maxResults);
				searchMultiAll.setSearchString(searchStrings);
				List<Map<String, String>> results = searchDelegate.execute(indexName, searchMultiAll);
				return SerializationUtilities.serialize(results);
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, Arrays.asList(searchStrings), fragment,
					firstResult, maxResults);
			LOGGER.error(message, e);
		}
		return SerializationUtilities.serialize(getMessageResults(indexName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@WebMethod
	@WebResult(name = "result")
	public String searchSpacialMulti(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "searchFields") final String[] searchFields,
			@WebParam(name = "fragment") final boolean fragment, @WebParam(name = "firstResult") final int firstResult,
			@WebParam(name = "maxResults") final int maxResults, @WebParam(name = "distance") final int distance,
			@WebParam(name = "latitude") final double latitude, @WebParam(name = "longitude") final double longitude) {
		try {
			SearchSpatial searchSpatial = getSearch(SearchSpatial.class, indexName);
			if (searchSpatial != null) {
				searchSpatial.setFirstResult(firstResult);
				searchSpatial.setFragment(fragment);
				searchSpatial.setMaxResults(maxResults);
				searchSpatial.setSearchString(searchStrings);
				searchSpatial.setSearchField(searchFields);
				searchSpatial.setCoordinate(new Coordinate(latitude, longitude));
				searchSpatial.setDistance(distance);
				List<Map<String, String>> results = searchDelegate.execute(indexName, searchSpatial);
				return SerializationUtilities.serialize(results);
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, Arrays.asList(searchStrings), fragment,
					firstResult, maxResults, latitude, longitude, distance);
			LOGGER.error(message, e);
		}
		return SerializationUtilities.serialize(getMessageResults(indexName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@WebMethod
	@WebResult(name = "result")
	public String searchSpacialMultiAll(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "fragment") final boolean fragment,
			@WebParam(name = "firstResult") final int firstResult, @WebParam(name = "maxResults") final int maxResults,
			@WebParam(name = "distance") final int distance, @WebParam(name = "latitude") final double latitude,
			@WebParam(name = "longitude") final double longitude) {
		try {
			SearchSpatialAll searchSpatial = getSearch(SearchSpatialAll.class, indexName);
			if (searchSpatial != null) {
				searchSpatial.setFirstResult(firstResult);
				searchSpatial.setFragment(fragment);
				searchSpatial.setMaxResults(maxResults);
				searchSpatial.setSearchString(searchStrings);
				searchSpatial.setCoordinate(new Coordinate(latitude, longitude));
				searchSpatial.setDistance(distance);
				List<Map<String, String>> results = searchDelegate.execute(indexName, searchSpatial);
				return SerializationUtilities.serialize(results);
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, Arrays.asList(searchStrings), fragment,
					firstResult, maxResults, latitude, longitude, distance);
			LOGGER.error(message, e);
		}
		return SerializationUtilities.serialize(getMessageResults(indexName));
	}

	/**
	 * This method will return an instance of the search class, based on the class in the parameter list and the index context name. For
	 * each search there is an instance created for the searcher classes to avoid thread overlap. The instance is created using reflection
	 * :( but is there a more elegant way?
	 * 
	 * @param <T> the type of class that is expected as a result
	 * @param klass the class of the searcher
	 * @param indexName the name of the index
	 * @return the searcher with the searchable injected
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getSearch(final Class<?> klass, final String indexName) throws Exception {
		@SuppressWarnings("rawtypes")
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext<?> context : indexContexts.values()) {
			if (context.getIndexName().equals(indexName)) {
				if (context.getIndex().getMultiSearcher() != null) {
					Constructor<?> constructor = klass.getConstructor(Searcher.class);
					return (T) constructor.newInstance(context.getIndex().getMultiSearcher());
				}
			}
		}
		return null;
	}

	/**
	 * This method returns the default message if there is no searcher defined for the index context, or the index is not generated or
	 * opened.
	 * 
	 * @param indexName the name of the index
	 * @return the message/map that will be sent to the client
	 */
	protected List<Map<String, String>> getMessageResults(final String indexName) {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		Map<String, String> notification = new HashMap<String, String>();
		notification.put(IConstants.CONTENTS, "No index defined for name : " + indexName);
		notification.put(IConstants.FRAGMENT, "Or exception thrown during search : " + indexName);
		results.add(notification);
		return results;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public String getPath() {
		return path;
	}

}