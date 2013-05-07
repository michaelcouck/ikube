package ikube.search;

import ikube.IConstants;
import ikube.action.index.handler.enrich.geocode.Coordinate;
import ikube.cluster.IMonitorService;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.Search;
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

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
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
@SuppressWarnings("deprecation")
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
	public ArrayList<HashMap<String, String>> searchSingle(@WebParam(name = "indexName")
	final String indexName, @WebParam(name = "searchString")
	final String searchString, @WebParam(name = "searchField")
	final String searchField, @WebParam(name = "fragment")
	final boolean fragment, @WebParam(name = "firstResult")
	final int firstResult, @WebParam(name = "maxResults")
	final int maxResults) {
		try {
			SearchSingle searchSingle = getSearch(SearchSingle.class, indexName);
			if (searchSingle != null) {
				searchSingle.setFirstResult(firstResult);
				searchSingle.setFragment(fragment);
				searchSingle.setMaxResults(maxResults);
				searchSingle.setSearchField(searchField);
				searchSingle.setSearchString(searchString);
				ArrayList<HashMap<String, String>> results = searchSingle.execute();
				String[] searchStringsCorrected = searchSingle.getCorrections();
				persistSearch(indexName, new String[] { searchString }, searchStringsCorrected, results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchString, searchField, fragment, firstResult, maxResults);
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
	public ArrayList<HashMap<String, String>> searchMulti(@WebParam(name = "indexName")
	final String indexName, @WebParam(name = "searchStrings")
	final String[] searchStrings, @WebParam(name = "searchFields")
	final String[] searchFields, @WebParam(name = "fragment")
	final boolean fragment, @WebParam(name = "firstResult")
	final int firstResult, @WebParam(name = "maxResults")
	final int maxResults) {
		try {
			SearchMulti searchMulti = getSearch(SearchMulti.class, indexName);
			if (searchMulti != null) {
				searchMulti.setFirstResult(firstResult);
				searchMulti.setFragment(fragment);
				searchMulti.setMaxResults(maxResults);
				searchMulti.setSearchField(searchFields);
				searchMulti.setSearchString(searchStrings);
				ArrayList<HashMap<String, String>> results = searchMulti.execute();
				String[] searchStringsCorrected = searchMulti.getCorrections();
				persistSearch(indexName, searchStrings, searchStringsCorrected, results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, searchFields, fragment, firstResult, maxResults);
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
	public ArrayList<HashMap<String, String>> searchMultiSorted(@WebParam(name = "indexName")
	final String indexName, @WebParam(name = "searchStrings")
	final String[] searchStrings, @WebParam(name = "searchFields")
	final String[] searchFields, @WebParam(name = "sortFields")
	final String[] sortFields, @WebParam(name = "fragment")
	final boolean fragment, @WebParam(name = "firstResult")
	final int firstResult, @WebParam(name = "maxResults")
	final int maxResults) {
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
				String[] searchStringsCorrected = searchMultiSorted.getCorrections();
				persistSearch(indexName, searchStrings, searchStringsCorrected, results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, searchFields, Arrays.asList(sortFields),
					fragment, firstResult, maxResults);
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
	public ArrayList<HashMap<String, String>> searchMultiAll(@WebParam(name = "indexName")
	final String indexName, @WebParam(name = "searchStrings")
	final String[] searchStrings, @WebParam(name = "fragment")
	final boolean fragment, @WebParam(name = "firstResult")
	final int firstResult, @WebParam(name = "maxResults")
	final int maxResults) {
		try {
			SearchMultiAll searchMultiAll = getSearch(SearchMultiAll.class, indexName);
			if (searchMultiAll != null) {
				searchMultiAll.setFirstResult(firstResult);
				searchMultiAll.setFragment(fragment);
				searchMultiAll.setMaxResults(maxResults);
				searchMultiAll.setSearchString(searchStrings);
				ArrayList<HashMap<String, String>> results = searchMultiAll.execute();
				String[] searchStringsCorrected = searchMultiAll.getCorrections();
				persistSearch(indexName, searchStrings, searchStringsCorrected, results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, fragment, firstResult, maxResults);
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
	public ArrayList<HashMap<String, String>> searchMultiSpacial(@WebParam(name = "indexName")
	final String indexName, @WebParam(name = "searchStrings")
	final String[] searchStrings, @WebParam(name = "searchFields")
	final String[] searchFields, @WebParam(name = "fragment")
	final boolean fragment, @WebParam(name = "firstResult")
	final int firstResult, @WebParam(name = "maxResults")
	final int maxResults, @WebParam(name = "distance")
	final int distance, @WebParam(name = "latitude")
	final double latitude, @WebParam(name = "longitude")
	final double longitude) {
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
				String[] searchStringsCorrected = searchSpatial.getCorrections();
				persistSearch(indexName, searchStrings, searchStringsCorrected, results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, fragment, firstResult, maxResults, latitude,
					longitude, distance);
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
	public ArrayList<HashMap<String, String>> searchMultiSpacialAll(@WebParam(name = "indexName")
	final String indexName, @WebParam(name = "searchStrings")
	final String[] searchStrings, @WebParam(name = "fragment")
	final boolean fragment, @WebParam(name = "firstResult")
	final int firstResult, @WebParam(name = "maxResults")
	final int maxResults, @WebParam(name = "distance")
	final int distance, @WebParam(name = "latitude")
	final double latitude, @WebParam(name = "longitude")
	final double longitude) {
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
				String[] searchStringsCorrected = searchSpatialAll.getCorrections();
				persistSearch(indexName, searchStrings, searchStringsCorrected, results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, fragment, firstResult, maxResults, latitude,
					longitude, distance);
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
	public ArrayList<HashMap<String, String>> searchMultiAdvanced(@WebParam(name = "indexName")
	final String indexName, @WebParam(name = "searchStrings")
	final String[] searchStrings, @WebParam(name = "searchFields")
	final String[] searchFields, @WebParam(name = "fragment")
	final boolean fragment, @WebParam(name = "firstResult")
	final int firstResult, @WebParam(name = "maxResults")
	final int maxResults) {
		try {
			SearchAdvanced searchAdvanced = getSearch(SearchAdvanced.class, indexName);
			if (searchAdvanced != null) {
				searchAdvanced.setFirstResult(firstResult);
				searchAdvanced.setFragment(fragment);
				searchAdvanced.setMaxResults(maxResults);
				searchAdvanced.setSearchString(searchStrings);
				searchAdvanced.setSearchField(searchFields);
				ArrayList<HashMap<String, String>> results = searchAdvanced.execute();
				String[] searchStringsCorrected = searchAdvanced.getCorrections();
				persistSearch(indexName, searchStrings, searchStringsCorrected, results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, searchFields, fragment, firstResult, maxResults);
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
	public ArrayList<HashMap<String, String>> searchMultiAdvancedAll(@WebParam(name = "indexName")
	final String indexName, @WebParam(name = "searchStrings")
	final String[] searchStrings, @WebParam(name = "fragment")
	final boolean fragment, @WebParam(name = "firstResult")
	final int firstResult, @WebParam(name = "maxResults")
	final int maxResults) {
		try {
			SearchAdvancedAll searchAdvancedAll = getSearch(SearchAdvancedAll.class, indexName);
			if (searchAdvancedAll != null) {
				searchAdvancedAll.setFirstResult(firstResult);
				searchAdvancedAll.setFragment(fragment);
				searchAdvancedAll.setMaxResults(maxResults);
				searchAdvancedAll.setSearchString(searchStrings);
				ArrayList<HashMap<String, String>> results = searchAdvancedAll.execute();
				String[] searchStringsCorrected = searchAdvancedAll.getCorrections();
				persistSearch(indexName, searchStrings, searchStringsCorrected, results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, fragment, firstResult, maxResults);
			LOGGER.error(message, e);
		}
		return getMessageResults(indexName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> searchNumericAll(String indexName, String[] searchStrings, boolean fragment, int firstResult, int maxResults) {
		try {
			SearchNumericAll searchNumericAll = getSearch(SearchNumericAll.class, indexName);
			if (searchNumericAll != null) {
				searchNumericAll.setFirstResult(firstResult);
				searchNumericAll.setFragment(fragment);
				searchNumericAll.setMaxResults(maxResults);
				searchNumericAll.setSearchString(searchStrings[0]);
				ArrayList<HashMap<String, String>> results = searchNumericAll.execute();
				String[] searchStringsCorrected = searchNumericAll.getCorrections();
				persistSearch(indexName, searchStrings, searchStringsCorrected, results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, fragment, firstResult, maxResults);
			LOGGER.error(message, e);
		}
		return getMessageResults(indexName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> searchNumericRange(String indexName, String[] searchStrings, boolean fragment, int firstResult, int maxResults) {
		try {
			SearchNumericRange searchNumericAll = getSearch(SearchNumericRange.class, indexName);
			if (searchNumericAll != null) {
				searchNumericAll.setFirstResult(firstResult);
				searchNumericAll.setFragment(fragment);
				searchNumericAll.setMaxResults(maxResults);
				searchNumericAll.setSearchString(searchStrings);
				ArrayList<HashMap<String, String>> results = searchNumericAll.execute();
				String[] searchStringsCorrected = searchNumericAll.getCorrections();
				persistSearch(indexName, searchStrings, searchStringsCorrected, results);
				return results;
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, fragment, firstResult, maxResults);
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
	public ArrayList<HashMap<String, String>> searchComplex(@WebParam(name = "indexName")
	final String indexName, @WebParam(name = "searchStrings")
	final String[] searchStrings, @WebParam(name = "searchFields")
	final String[] searchFields, @WebParam(name = "typeFields")
	final String[] typeFields, @WebParam(name = "fragment")
	final boolean fragment, @WebParam(name = "firstResult")
	final int firstResult, @WebParam(name = "maxResults")
	final int maxResults) {
		try {
			SearchComplex searchComplex = getSearch(SearchComplex.class, indexName);
			if (searchComplex != null) {
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
			}
		} catch (Exception e) {
			String message = Logging.getString("Exception doing search on index : ", indexName, searchStrings, searchFields, fragment, firstResult, maxResults);
			LOGGER.error(message, e);
		}
		return getMessageResults(indexName);
	}

	/**
	 * This method will return an instance of the search class, based on the class in the parameter list and the index context name. For each search there is an
	 * instance created for the searcher classes to avoid thread overlap. The instance is created using reflection :( but is there a more elegant way?
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
			try {
				if (indexContext.getIndexName().equals(indexName)) {
					if (indexContext.getMultiSearcher() != null) {
						if (indexContext.getAnalyzer() != null) {
							Constructor<?> constructor = klass.getConstructor(Searcher.class, Analyzer.class);
							return (T) constructor.newInstance(indexContext.getMultiSearcher(), indexContext.getAnalyzer());
						}
						Constructor<?> constructor = klass.getConstructor(Searcher.class);
						return (T) constructor.newInstance(indexContext.getMultiSearcher());
					}
				}
			} catch (NullPointerException e) {
				LOGGER.error(null, e);
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
	 * This method returns the default message if there is no searcher defined for the index context, or the index is not generated or opened.
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
			String correctSearchString = searchStringsCorrected[i];
			Search dbSearch = dataBase.findCriteria(Search.class, new String[] { "indexName", "searchStrings" }, new Object[] { indexName, searchString });
			if (dbSearch == null) {
				Search search = new Search();
				search.setCount(1);
				search.setSearchStrings(searchString);
				search.setIndexName(indexName);
				search.setResults(Integer.parseInt(statistics.get(IConstants.RESULTS)));
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