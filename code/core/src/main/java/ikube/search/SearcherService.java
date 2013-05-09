package ikube.search;

import ikube.IConstants;
import ikube.action.index.handler.enrich.geocode.Coordinate;
import ikube.cluster.IMonitorService;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.Search;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Searcher;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @see ISearcherService
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Remote(ISearcherService.class)
@SuppressWarnings("deprecation")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
@WebService(name = ISearcherService.NAME, targetNamespace = ISearcherService.NAMESPACE, serviceName = ISearcherService.SERVICE)
public class SearcherService implements ISearcherService {

	/** The database that we persist the searches to. */
	@Autowired
	private IDataBase dataBase;

	/** The service to get system contexts and other bric-a-brac. */
	@Autowired
	private IMonitorService monitorService;

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
			searchSingle.setFirstResult(firstResult);
			searchSingle.setFragment(fragment);
			searchSingle.setMaxResults(maxResults);
			searchSingle.setSearchField(searchField);
			searchSingle.setSearchString(searchString);
			ArrayList<HashMap<String, String>> results = searchSingle.execute();
			String[] searchStringsCorrected = searchSingle.getCorrections();
			persistSearch(indexName, new String[] { searchString }, searchStringsCorrected, results);
			return results;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
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
			searchMulti.setFirstResult(firstResult);
			searchMulti.setFragment(fragment);
			searchMulti.setMaxResults(maxResults);
			searchMulti.setSearchField(searchFields);
			searchMulti.setSearchString(searchStrings);
			ArrayList<HashMap<String, String>> results = searchMulti.execute();
			String[] searchStringsCorrected = searchMulti.getCorrections();
			persistSearch(indexName, searchStrings, searchStringsCorrected, results);
			return results;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
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
			searchMultiSorted.setFirstResult(firstResult);
			searchMultiSorted.setFragment(fragment);
			searchMultiSorted.setMaxResults(maxResults);
			searchMultiSorted.setSearchField(searchFields);
			searchMultiSorted.setSearchString(searchStrings);
			searchMultiSorted.setSortField(sortFields);
			ArrayList<HashMap<String, String>> results = searchMultiSorted.execute();
			String[] searchStringsCorrected = searchMultiSorted.getCorrections();
			persistSearch(indexName, searchStrings, searchStringsCorrected, results);
			return results;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
			searchMultiAll.setFirstResult(firstResult);
			searchMultiAll.setFragment(fragment);
			searchMultiAll.setMaxResults(maxResults);
			searchMultiAll.setSearchString(searchStrings);
			ArrayList<HashMap<String, String>> results = searchMultiAll.execute();
			String[] searchStringsCorrected = searchMultiAll.getCorrections();
			persistSearch(indexName, searchStrings, searchStringsCorrected, results);
			return results;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
			searchSpatial.setFirstResult(firstResult);
			searchSpatial.setFragment(fragment);
			searchSpatial.setMaxResults(maxResults);
			searchSpatial.setSearchString(searchStrings);
			searchSpatial.setSearchField(searchFields);
			searchSpatial.setCoordinate(new Coordinate(latitude, longitude));
			searchSpatial.setDistance(distance);
			ArrayList<HashMap<String, String>> results = searchSpatial.execute();
			String[] searchStringsCorrected = searchSpatial.getCorrections();
			persistSearch(indexName, searchStrings, searchStringsCorrected, results);
			return results;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
			searchSpatialAll.setFirstResult(firstResult);
			searchSpatialAll.setFragment(fragment);
			searchSpatialAll.setMaxResults(maxResults);
			searchSpatialAll.setSearchString(searchStrings);
			searchSpatialAll.setCoordinate(new Coordinate(latitude, longitude));
			searchSpatialAll.setDistance(distance);
			ArrayList<HashMap<String, String>> results = searchSpatialAll.execute();
			String[] searchStringsCorrected = searchSpatialAll.getCorrections();
			persistSearch(indexName, searchStrings, searchStringsCorrected, results);
			return results;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
			searchAdvanced.setFirstResult(firstResult);
			searchAdvanced.setFragment(fragment);
			searchAdvanced.setMaxResults(maxResults);
			searchAdvanced.setSearchString(searchStrings);
			searchAdvanced.setSearchField(searchFields);
			ArrayList<HashMap<String, String>> results = searchAdvanced.execute();
			String[] searchStringsCorrected = searchAdvanced.getCorrections();
			persistSearch(indexName, searchStrings, searchStringsCorrected, results);
			return results;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> searchNumericAll(String indexName, String[] searchStrings, boolean fragment, int firstResult,
			int maxResults) {
		try {
			SearchNumericAll searchNumericAll = getSearch(SearchNumericAll.class, indexName);
			searchNumericAll.setFirstResult(firstResult);
			searchNumericAll.setFragment(fragment);
			searchNumericAll.setMaxResults(maxResults);
			searchNumericAll.setSearchString(searchStrings[0]);
			ArrayList<HashMap<String, String>> results = searchNumericAll.execute();
			String[] searchStringsCorrected = searchNumericAll.getCorrections();
			persistSearch(indexName, searchStrings, searchStringsCorrected, results);
			return results;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> searchNumericRange(String indexName, String[] searchStrings, boolean fragment,
			int firstResult, int maxResults) {
		try {
			SearchNumericRange searchNumericRange = getSearch(SearchNumericRange.class, indexName);
			searchNumericRange.setFirstResult(firstResult);
			searchNumericRange.setFragment(fragment);
			searchNumericRange.setMaxResults(maxResults);
			searchNumericRange.setSearchString(searchStrings);
			ArrayList<HashMap<String, String>> results = searchNumericRange.execute();
			String[] searchStringsCorrected = searchNumericRange.getCorrections();
			persistSearch(indexName, searchStrings, searchStringsCorrected, results);
			return results;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@WebMethod
	@WebResult(name = "result")
	public ArrayList<HashMap<String, String>> searchComplex(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "searchFields") final String[] searchFields,
			@WebParam(name = "typeFields") final String[] typeFields, @WebParam(name = "fragment") final boolean fragment,
			@WebParam(name = "firstResult") final int firstResult, @WebParam(name = "maxResults") final int maxResults) {
		try {
			SearchComplex searchComplex = getSearch(SearchComplex.class, indexName);
			searchComplex.setFirstResult(firstResult);
			searchComplex.setFragment(fragment);
			searchComplex.setMaxResults(maxResults);
			searchComplex.setSearchField(searchFields);
			searchComplex.setSearchString(searchStrings);
			searchComplex.setTypeFields(typeFields);
			ArrayList<HashMap<String, String>> results = searchComplex.execute();
			String[] searchStringsCorrected = searchComplex.getCorrections();
			persistSearch(indexName, searchStrings, searchStringsCorrected, results);
			return results;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
		T search = null;
		@SuppressWarnings("rawtypes")
		Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
		for (final IndexContext<?> indexContext : indexContexts.values()) {
			if (indexContext.getIndexName().equals(indexName)) {
				if (indexContext.getMultiSearcher() != null) {
					if (indexContext.getAnalyzer() != null) {
						Constructor<?> constructor = klass.getConstructor(Searcher.class, Analyzer.class);
						search = (T) constructor.newInstance(indexContext.getMultiSearcher(), indexContext.getAnalyzer());
					} else {
						Constructor<?> constructor = klass.getConstructor(Searcher.class);
						search = (T) constructor.newInstance(indexContext.getMultiSearcher());
					}
				}
				break;
			}
		}
		return search;
	}

	protected void persistSearch(final String indexName, final String[] searchStrings, final String[] searchStringsCorrected,
			final ArrayList<HashMap<String, String>> results) {
		// Don't persist the auto complete searches
		if (IConstants.AUTOCOMPLETE.equals(indexName)) {
			return;
		}
		Map<String, String> statistics = results.get(0);
		// Add the index name to the statistics here, not elegant, I know
		statistics.put(IConstants.INDEX_NAME, indexName);
		for (int i = 0; i < searchStrings.length; i++) {
			String searchString = searchStrings[i];
			String correctSearchString = null;
			if (searchStringsCorrected != null && searchStringsCorrected.length > i) {
				correctSearchString = searchStringsCorrected[i];
			}
			Search dbSearch = dataBase.findCriteria(Search.class, new String[] { "indexName", "searchStrings" }, new Object[] { indexName,
					searchString });
			if (dbSearch == null) {
				Search search = new Search();
				search.setCount(1);
				search.setSearchStrings(searchString);
				search.setIndexName(indexName);
				search.setTotalResults(Integer.parseInt(statistics.get(IConstants.TOTAL)));
				search.setHighScore(Double.parseDouble(statistics.get(IConstants.SCORE)));
				search.setCorrections(searchString.equals(correctSearchString));
				search.setCorrectedSearchStrings(correctSearchString);
				search.setSearchResults(results);
				dataBase.persist(search);
			} else {
				dataBase.merge(dbSearch);
			}
		}
	}

}