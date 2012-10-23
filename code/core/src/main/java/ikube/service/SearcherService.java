package ikube.service;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.index.spatial.Coordinate;
import ikube.model.IndexContext;
import ikube.model.Search;
import ikube.search.SearchAdvanced;
import ikube.search.SearchMulti;
import ikube.search.SearchMultiAll;
import ikube.search.SearchMultiSorted;
import ikube.search.SearchSingle;
import ikube.search.SearchSpatial;
import ikube.search.SearchSpatialAll;
import ikube.search.spelling.SpellingChecker;
import ikube.toolkit.Logging;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @see ISearcherService
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Remote(ISearcherService.class)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
@WebService(name = ISearcherService.NAME, targetNamespace = ISearcherService.NAMESPACE, serviceName = ISearcherService.SERVICE)
public class SearcherService implements ISearcherService {

	private static final Logger LOGGER = Logger.getLogger(SearcherService.class);

	@Autowired
	private IDataBase dataBase;
	@Autowired
	private IMonitorService monitorService;
	@Autowired
	private SpellingChecker spellingChecker;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@WebMethod
	@WebResult(name = "result")
	public ArrayList<HashMap<String, String>> searchSingle(@WebParam(name = "indexName") final String indexName,
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
				ArrayList<HashMap<String, String>> results = searchSingle.execute();
				addSearchStatistics(indexName, new String[] { searchString }, results.size(), results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchString, searchField, fragment,
					firstResult, maxResults);
			LOGGER.error(message, e);
		}
		return getMessageResults(indexName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@WebMethod
	@WebResult(name = "result")
	public ArrayList<HashMap<String, String>> searchMulti(@WebParam(name = "indexName") final String indexName,
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
				ArrayList<HashMap<String, String>> results = searchMulti.execute();
				addSearchStatistics(indexName, searchStrings, results.size(), results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, searchFields, fragment,
					firstResult, maxResults);
			LOGGER.error(message, e);
		}
		return getMessageResults(indexName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@WebMethod
	@WebResult(name = "result")
	public ArrayList<HashMap<String, String>> searchMultiSorted(@WebParam(name = "indexName") final String indexName,
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
				ArrayList<HashMap<String, String>> results = searchMultiSorted.execute();
				addSearchStatistics(indexName, searchStrings, results.size(), results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, searchFields,
					Arrays.asList(sortFields), fragment, firstResult, maxResults);
			LOGGER.error(message, e);
		}
		return getMessageResults(indexName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@WebMethod
	@WebResult(name = "result")
	public ArrayList<HashMap<String, String>> searchMultiAll(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "fragment") final boolean fragment,
			@WebParam(name = "firstResult") final int firstResult, @WebParam(name = "maxResults") final int maxResults) {
		try {
			SearchMultiAll searchMultiAll = getSearch(SearchMultiAll.class, indexName);
			if (searchMultiAll != null) {
				searchMultiAll.setFirstResult(firstResult);
				searchMultiAll.setFragment(fragment);
				searchMultiAll.setMaxResults(maxResults);
				searchMultiAll.setSearchString(searchStrings);
				ArrayList<HashMap<String, String>> results = searchMultiAll.execute();
				addSearchStatistics(indexName, searchStrings, results.size(), results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, fragment, firstResult,
					maxResults);
			LOGGER.error(message, e);
		}
		return getMessageResults(indexName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@WebMethod
	@WebResult(name = "result")
	public ArrayList<HashMap<String, String>> searchMultiSpacial(@WebParam(name = "indexName") final String indexName,
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
				ArrayList<HashMap<String, String>> results = searchSpatial.execute();
				addSearchStatistics(indexName, searchStrings, results.size(), results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, fragment, firstResult,
					maxResults, latitude, longitude, distance);
			LOGGER.error(message, e);
		}
		return getMessageResults(indexName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@WebMethod
	@WebResult(name = "result")
	public ArrayList<HashMap<String, String>> searchMultiSpacialAll(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "fragment") final boolean fragment,
			@WebParam(name = "firstResult") final int firstResult, @WebParam(name = "maxResults") final int maxResults,
			@WebParam(name = "distance") final int distance, @WebParam(name = "latitude") final double latitude,
			@WebParam(name = "longitude") final double longitude) {
		try {
			SearchSpatialAll searchSpatialAll = getSearch(SearchSpatialAll.class, indexName);
			if (searchSpatialAll != null) {
				searchSpatialAll.setFirstResult(firstResult);
				searchSpatialAll.setFragment(fragment);
				searchSpatialAll.setMaxResults(maxResults);
				searchSpatialAll.setSearchString(searchStrings);
				searchSpatialAll.setCoordinate(new Coordinate(latitude, longitude));
				searchSpatialAll.setDistance(distance);
				ArrayList<HashMap<String, String>> results = searchSpatialAll.execute();
				addSearchStatistics(indexName, searchStrings, results.size(), results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, fragment, firstResult,
					maxResults, latitude, longitude, distance);
			LOGGER.error(message, e);
		}
		return getMessageResults(indexName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@WebMethod
	@WebResult(name = "result")
	public ArrayList<HashMap<String, String>> searchMultiAdvanced(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "searchFields") final String[] searchFields,
			@WebParam(name = "fragment") final boolean fragment, @WebParam(name = "firstResult") final int firstResult,
			@WebParam(name = "maxResults") final int maxResults) {
		try {
			SearchAdvanced searchAdvanced = getSearch(SearchAdvanced.class, indexName);
			if (searchAdvanced != null) {
				searchAdvanced.setFirstResult(firstResult);
				searchAdvanced.setFragment(fragment);
				searchAdvanced.setMaxResults(maxResults);
				searchAdvanced.setSearchString(searchStrings);
				searchAdvanced.setSearchField(searchFields);
				ArrayList<HashMap<String, String>> results = searchAdvanced.execute();
				addSearchStatistics(indexName, searchStrings, results.size(), results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, searchFields, fragment,
					firstResult, maxResults);
			LOGGER.error(message, e);
		}
		return getMessageResults(indexName);
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
		Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
		for (IndexContext<?> indexContext : indexContexts.values()) {
			if (indexContext.getIndexName().equals(indexName)) {
				if (indexContext.getMultiSearcher() != null) {
					Constructor<?> constructor = klass.getConstructor(Searcher.class);
					return (T) constructor.newInstance(indexContext.getMultiSearcher());
				}
			}
		}
		return null;
	}

	@SuppressWarnings({ "unused", "rawtypes" })
	private void printSearchables(final IndexContext indexContext) throws IOException {
		LOGGER.info("Multi searcher : " + indexContext.getMultiSearcher());
		Searchable[] searchables = indexContext.getMultiSearcher().getSearchables();
		for (Searchable searchable : searchables) {
			IndexSearcher indexSearcher = (IndexSearcher) searchable;
			IndexReader indexReader = indexSearcher.getIndexReader();
			FSDirectory directory = (FSDirectory) indexReader.directory();
			LOGGER.info("Max docs : " + indexReader.maxDoc());
			LOGGER.info("Num docs : " + indexReader.numDocs());
			LOGGER.info("Directories : " + Arrays.deepToString(directory.listAll()));
		}
	}

	/**
	 * This method returns the default message if there is no searcher defined for the index context, or the index is not generated or
	 * opened.
	 * 
	 * @param indexName the name of the index
	 * @return the message/map that will be sent to the client
	 */
	protected ArrayList<HashMap<String, String>> getMessageResults(final String indexName) {
		ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> notification = new HashMap<String, String>();
		notification.put(IConstants.CONTENTS, "No index defined for name : " + indexName);
		notification.put(IConstants.FRAGMENT, "Or exception thrown during search : " + indexName);
		results.add(notification);
		return results;
	}

	protected String getSearchKey(final String indexName, final String... parameters) {
		StringBuilder stringBuilder = new StringBuilder(indexName);
		for (String parameter : parameters) {
			stringBuilder.append("-");
			stringBuilder.append(parameter);
		}
		return stringBuilder.toString();
	}

	protected Search addSearchStatistics(final String indexName, final String[] searchStrings, final int results,
			final ArrayList<HashMap<String, String>> searchResults) {

		double highScore = 0;
		if (searchResults.size() > 1) {
			highScore = Double.parseDouble(searchResults.get(0).get(IConstants.SCORE));
		}

		searchResults.get(searchResults.size() - 1).put(IConstants.INDEX_NAME, indexName);
		StringBuilder stringBuilder = new StringBuilder();
		for (String searchString : searchStrings) {
			String correctedSearchString = spellingChecker.checkWords(searchString);
			stringBuilder.append(correctedSearchString);
			stringBuilder.append(" ");
		}
		String correctedSearchString = stringBuilder.toString();
		Search search = new Search();
		search.setCount(search.getCount() + 1);
		search.setSearchStrings(Arrays.deepToString(searchStrings));
		search.setIndexName(indexName);
		search.setResults(results);
		search.setHighScore(highScore);
		search.setCorrections(StringUtils.isNotEmpty(correctedSearchString));
		search.setCorrectedSearchStrings(correctedSearchString);
		search.setSearchResults(searchResults);
		dataBase.persist(search);
		// LOGGER.info("Search : " + ToStringBuilder.reflectionToString(search, ToStringStyle.MULTI_LINE_STYLE));
		// LOGGER.info("Search size : " + search.getResults());
		// LOGGER.info("Search results : " + search.getSearchResults());
		return search;
	}

}