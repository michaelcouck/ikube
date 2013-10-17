package ikube.search;

import ikube.IConstants;
import ikube.cluster.IMonitorService;
import ikube.database.IDataBase;
import ikube.model.Coordinate;
import ikube.model.IndexContext;
import ikube.model.Search;
import ikube.toolkit.HashUtilities;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @see ISearcherService
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@SuppressWarnings("deprecation")
@Remote(ISearcherService.class)
public class SearcherService implements ISearcherService {

	static final Logger LOGGER = LoggerFactory.getLogger(SearcherService.class);

	private static final ArrayList<HashMap<String, String>> EMPTY_RESULTS = new ArrayList<HashMap<String, String>>();

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
	public ArrayList<HashMap<String, String>> searchSingle(final String indexName, final String searchString, final String searchField, final boolean fragment,
			final int firstResult, final int maxResults) {
		try {
			SearchSingle searchSingle = getSearch(SearchSingle.class, indexName);
			if (searchSingle == null) {
				LOGGER.warn("Searcher null for index : " + indexName);
				return EMPTY_RESULTS;
			}
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
			return handleException(indexName, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> searchMulti(final String indexName, final String[] searchStrings, final String[] searchFields,
			final boolean fragment, final int firstResult, final int maxResults) {
		try {
			SearchMulti searchMulti = getSearch(SearchMulti.class, indexName);
			if (searchMulti == null) {
				LOGGER.warn("Searcher null for index : " + indexName);
				return EMPTY_RESULTS;
			}
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
			return handleException(indexName, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> searchMultiSorted(final String indexName, final String[] searchStrings, final String[] searchFields,
			final String[] sortFields, final boolean fragment, final int firstResult, final int maxResults) {
		try {
			SearchMultiSorted searchMultiSorted = getSearch(SearchMultiSorted.class, indexName);
			if (searchMultiSorted == null) {
				LOGGER.warn("Searcher null for index : " + indexName);
				return EMPTY_RESULTS;
			}
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
		} catch (final Exception e) {
			return handleException(indexName, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> searchMultiAll(final String indexName, final String[] searchStrings, final boolean fragment,
			final int firstResult, final int maxResults) {
		try {
			SearchMultiAll searchMultiAll = getSearch(SearchMultiAll.class, indexName);
			if (searchMultiAll == null) {
				LOGGER.warn("Searcher null for index : " + indexName);
				return EMPTY_RESULTS;
			}
			searchMultiAll.setFirstResult(firstResult);
			searchMultiAll.setFragment(fragment);
			searchMultiAll.setMaxResults(maxResults);
			searchMultiAll.setSearchString(searchStrings);
			ArrayList<HashMap<String, String>> results = searchMultiAll.execute();
			String[] searchStringsCorrected = searchMultiAll.getCorrections();
			persistSearch(indexName, searchStrings, searchStringsCorrected, results);
			return results;
		} catch (final Exception e) {
			return handleException(indexName, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> searchMultiSpacial(final String indexName, final String[] searchStrings, final String[] searchFields,
			final boolean fragment, final int firstResult, final int maxResults, final int distance, final double latitude, final double longitude) {
		try {
			SearchSpatial searchSpatial = getSearch(SearchSpatial.class, indexName);
			if (searchSpatial == null) {
				LOGGER.warn("Searcher null for index : " + indexName);
				return EMPTY_RESULTS;
			}
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
		} catch (final Exception e) {
			return handleException(indexName, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> searchMultiSpacialAll(final String indexName, final String[] searchStrings, final boolean fragment,
			final int firstResult, final int maxResults, final int distance, final double latitude, final double longitude) {
		try {
			SearchSpatialAll searchSpatialAll = getSearch(SearchSpatialAll.class, indexName);
			if (searchSpatialAll == null) {
				LOGGER.warn("Searcher null for index : " + indexName);
				return EMPTY_RESULTS;
			}
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
		} catch (final Exception e) {
			return handleException(indexName, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> searchMultiAdvanced(final String indexName, final String[] searchStrings, final String[] searchFields,
			final boolean fragment, final int firstResult, final int maxResults) {
		try {
			SearchAdvanced searchAdvanced = getSearch(SearchAdvanced.class, indexName);
			if (searchAdvanced == null) {
				LOGGER.warn("Searcher null for index : " + indexName);
				return EMPTY_RESULTS;
			}
			searchAdvanced.setFirstResult(firstResult);
			searchAdvanced.setFragment(fragment);
			searchAdvanced.setMaxResults(maxResults);
			searchAdvanced.setSearchString(searchStrings);
			searchAdvanced.setSearchField(searchFields);
			ArrayList<HashMap<String, String>> results = searchAdvanced.execute();
			String[] searchStringsCorrected = searchAdvanced.getCorrections();
			persistSearch(indexName, searchStrings, searchStringsCorrected, results);
			return results;
		} catch (final Exception e) {
			return handleException(indexName, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> searchNumericAll(final String indexName, final String[] searchStrings, final boolean fragment,
			final int firstResult, final int maxResults) {
		try {
			SearchNumericAll searchNumericAll = getSearch(SearchNumericAll.class, indexName);
			if (searchNumericAll == null) {
				LOGGER.warn("Searcher null for index : " + indexName);
				return EMPTY_RESULTS;
			}
			searchNumericAll.setFirstResult(firstResult);
			searchNumericAll.setFragment(fragment);
			searchNumericAll.setMaxResults(maxResults);
			searchNumericAll.setSearchString(searchStrings[0]);
			ArrayList<HashMap<String, String>> results = searchNumericAll.execute();
			String[] searchStringsCorrected = searchNumericAll.getCorrections();
			persistSearch(indexName, searchStrings, searchStringsCorrected, results);
			return results;
		} catch (final Exception e) {
			return handleException(indexName, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> searchNumericRange(final String indexName, final String[] searchStrings, final boolean fragment,
			final int firstResult, final int maxResults) {
		try {
			SearchNumericRange searchNumericRange = getSearch(SearchNumericRange.class, indexName);
			if (searchNumericRange == null) {
				LOGGER.warn("Searcher null for index : " + indexName);
				return EMPTY_RESULTS;
			}
			searchNumericRange.setFirstResult(firstResult);
			searchNumericRange.setFragment(fragment);
			searchNumericRange.setMaxResults(maxResults);
			searchNumericRange.setSearchString(searchStrings);
			ArrayList<HashMap<String, String>> results = searchNumericRange.execute();
			String[] searchStringsCorrected = searchNumericRange.getCorrections();
			persistSearch(indexName, searchStrings, searchStringsCorrected, results);
			return results;
		} catch (final Exception e) {
			return handleException(indexName, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> searchComplex(final String indexName, final String[] searchStrings, final String[] searchFields,
			final String[] typeFields, final boolean fragment, final int firstResult, final int maxResults) {
		try {
			SearchComplex searchComplex = getSearch(SearchComplex.class, indexName);
			if (searchComplex == null) {
				LOGGER.warn("Searcher null for index : " + indexName);
				return EMPTY_RESULTS;
			}
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
		} catch (final Exception e) {
			return handleException(indexName, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> searchComplexSorted(final String indexName, final String[] searchStrings, final String[] searchFields,
			final String[] typeFields, final String[] sortFields, final boolean fragment, final int firstResult, final int maxResults) {
		try {
			SearchComplexSorted searchComplexSorted = getSearch(SearchComplexSorted.class, indexName);
			if (searchComplexSorted == null) {
				LOGGER.warn("Searcher null for index : " + indexName);
				return EMPTY_RESULTS;
			}
			searchComplexSorted.setFirstResult(firstResult);
			searchComplexSorted.setFragment(fragment);
			searchComplexSorted.setMaxResults(maxResults);
			searchComplexSorted.setSearchField(searchFields);
			searchComplexSorted.setSearchString(searchStrings);
			searchComplexSorted.setTypeFields(typeFields);
			searchComplexSorted.setSortField(sortFields);
			ArrayList<HashMap<String, String>> results = searchComplexSorted.execute();
			String[] searchStringsCorrected = searchComplexSorted.getCorrections();
			persistSearch(indexName, searchStrings, searchStringsCorrected, results);
			return results;
		} catch (final Exception e) {
			return handleException(indexName, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Search searchComplexSorted(final Search search) {
		try {
			SearchComplexSorted searchComplexSorted = getSearch(SearchComplexSorted.class, search.getIndexName());
			if (searchComplexSorted == null) {
				LOGGER.warn("Searcher null for index : " + search.getIndexName());
				return search;
			}
			String[] searchStrings = search.getSearchStrings().toArray(new String[search.getSearchStrings().size()]);
			String[] searchFields = search.getSearchFields().toArray(new String[search.getSearchFields().size()]);
			String[] typeFields = search.getTypeFields().toArray(new String[search.getTypeFields().size()]);
			String[] sortFields = search.getSortFields().toArray(new String[search.getSortFields().size()]);
			// TODO Change the field types based on the search strings for those fields, for example if the field
			// is something like 123-456 then this is a range query for the field

			searchComplexSorted.setFirstResult(search.getFirstResult());
			searchComplexSorted.setFragment(search.isFragment());
			searchComplexSorted.setMaxResults(search.getMaxResults());
			searchComplexSorted.setSearchString(searchStrings);
			searchComplexSorted.setSearchField(searchFields);
			searchComplexSorted.setTypeFields(typeFields);
			searchComplexSorted.setSortField(sortFields);
			ArrayList<HashMap<String, String>> results = searchComplexSorted.execute();
			String[] searchStringsCorrected = searchComplexSorted.getCorrections();
			persistSearch(search.getIndexName(), searchStrings, searchStringsCorrected, results);
			if (searchStringsCorrected != null) {
				search.setCorrectedSearchStrings(Arrays.asList(searchStringsCorrected));
			}
			search.setCorrections(searchStringsCorrected != null && searchStringsCorrected.length > 0);
			search.setSearchResults(results);
		} catch (final Exception e) {
			handleException(search.getIndexName(), e);
		}
		return search;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Search searchMultiSpacial(final Search search) {
		String indexName = search.getIndexName();
		String[] searchStrings = search.getSearchStrings().toArray(new String[search.getSearchStrings().size()]);
		String[] searchFields = search.getSearchFields().toArray(new String[search.getSearchFields().size()]);
		Coordinate coordinate = search.getCoordinate();
		ArrayList<HashMap<String, String>> results = searchMultiSpacial(indexName, searchStrings, searchFields, search.isFragment(), search.getFirstResult(),
				search.getMaxResults(), search.getDistance(), coordinate.getLatitude(), coordinate.getLongitude());
		search.setSearchResults(results);
		return search;
	}

	/**
	 * This method is particularly expensive, it will do a search on every index in the system.
	 */
	@Override
	public Search searchComplexSortedAll(final Search search) {
		try {
			long totalHits = 0;
			float highScore = 0;
			long duration = 0;

			String[] indexNames = monitorService.getIndexNames();
			String[] searchStrings = search.getSearchStrings().toArray(new String[search.getSearchStrings().size()]);
			ArrayList<HashMap<String, String>> searchResults = new ArrayList<HashMap<String, String>>();

			boolean fragment = search.isFragment();
			int firstResult = search.getFirstResult();
			int maxResults = search.getMaxResults();

			Exception exception = null;
			HashMap<String, String> statistics = new HashMap<String, String>();
			for (final String indexName : indexNames) {
				List<HashMap<String, String>> searchSubResults = searchMultiAll(indexName, searchStrings, fragment, firstResult, maxResults);
				if (searchSubResults != null && searchSubResults.size() > 1) {
					statistics = searchSubResults.remove(searchSubResults.size() - 1);
					totalHits += Long.parseLong(statistics.get(IConstants.TOTAL));
					highScore += Float.parseFloat(statistics.get(IConstants.SCORE));
					duration += Long.parseLong(statistics.get(IConstants.DURATION));
					searchResults.addAll(searchSubResults);
				}
			}
			statistics.put(IConstants.TOTAL, Long.toString(totalHits));
			statistics.put(IConstants.DURATION, Long.toString(duration));
			statistics.put(IConstants.SCORE, Float.toString(highScore));
			// Sort all the results according to the score
			Collections.sort(searchResults, new Comparator<HashMap<String, String>>() {
				@Override
				public int compare(final HashMap<String, String> o1, final HashMap<String, String> o2) {
					Double scoreOne = Double.parseDouble(o1.get(IConstants.SCORE));
					Double scoreTwo = Double.parseDouble(o2.get(IConstants.SCORE));
					return scoreOne.compareTo(scoreTwo);
				}
			});
			ArrayList<HashMap<String, String>> topResults = new ArrayList<HashMap<String, String>>();
			topResults.addAll(searchResults.subList(0, search.getMaxResults()));

			SearchSingle searchSingle = new SearchSingle(null);
			searchSingle.setSearchString(searchStrings);
			searchSingle.addStatistics(topResults, totalHits, highScore, duration, exception);

			search.setSearchResults(topResults);
		} catch (Exception e) {
			handleException(search.getIndexName(), e);
		}
		return search;
	}

	private ArrayList<HashMap<String, String>> handleException(final String indexName, final Exception e) {
		LOGGER.error("Exception doing search on : " + indexName, e);
		return EMPTY_RESULTS;
	}

	/**
	 * This method will return an instance of the search class, based on the class in the parameter list and the index context name. For each search there is an
	 * instance created for the searcher classes to avoid thread overlap. The instance is created using reflection :( but is there a more elegant way?
	 * 
	 * @param <T> the type of class that is expected as a category
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
		if (IConstants.AUTOCOMPLETE.equals(indexName) || results == null) {
			return;
		}
		Map<String, String> statistics = results.get(results.size() - 1);
		// Add the index name to the statistics here, not elegant, I know
		statistics.put(IConstants.INDEX_NAME, indexName);

		long hash = HashUtilities.hash(Arrays.deepToString(searchStrings));
		Search dbSearch = dataBase.findCriteria(Search.class, new String[] { "hash" }, new Object[] { hash });
		if (dbSearch != null) {
			dataBase.merge(dbSearch);
		} else {
			Search search = new Search();
			search.setHash(hash);
			search.setSearchStrings(Arrays.asList(searchStrings));
			search.setIndexName(indexName);
			search.setTotalResults(Integer.parseInt(statistics.get(IConstants.TOTAL)));
			search.setHighScore(Double.parseDouble(statistics.get(IConstants.SCORE)));
			search.setCorrections(Arrays.deepToString(searchStrings).equals(Arrays.deepToString(searchStringsCorrected)));
			search.setCorrectedSearchStrings(Arrays.asList(searchStringsCorrected));
			search.setSearchResults(results);
			dataBase.persist(search);
		}
	}

}