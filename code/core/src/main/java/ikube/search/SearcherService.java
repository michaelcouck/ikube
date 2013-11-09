package ikube.search;

import ikube.IConstants;
import ikube.cluster.IMonitorService;
import ikube.database.IDataBase;
import ikube.model.Coordinate;
import ikube.model.IndexContext;
import ikube.model.Search;
import ikube.search.Search.TypeField;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @see ISearcherService
 * @author Michael Couck
 * @since 21.11.10
 * @version 02.00
 */
@SuppressWarnings("deprecation")
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
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> search(final String indexName, final String[] searchStrings, final String[] searchFields, final boolean fragment,
			final int firstResult, final int maxResults) {
		try {
			Search search = new Search();
			search.setIndexName(indexName);

			String[] typeFields = new String[searchFields.length];
			Arrays.fill(typeFields, TypeField.STRING.fieldType());

			search.setSearchStrings(Arrays.asList(searchStrings));
			search.setSearchFields(Arrays.asList(searchFields));
			search.setTypeFields(Arrays.asList(typeFields));
			search.setSortFields(Collections.EMPTY_LIST);

			search.setFragment(fragment);
			search.setFirstResult(firstResult);
			search.setMaxResults(maxResults);
			search(search);
			return search.getSearchResults();
		} catch (final Exception e) {
			return handleException(indexName, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> search(final String indexName, final String[] searchStrings, final String[] searchFields,
			final String[] sortFields, final boolean fragment, final int firstResult, final int maxResults) {
		try {
			Search search = new Search();
			search.setIndexName(indexName);

			String[] typeFields = new String[searchFields.length];
			Arrays.fill(typeFields, TypeField.STRING.fieldType());

			search.setSearchStrings(Arrays.asList(searchStrings));
			search.setSearchFields(Arrays.asList(searchFields));
			search.setTypeFields(Arrays.asList(typeFields));
			search.setSortFields(Arrays.asList(sortFields));

			search.setFragment(fragment);
			search.setFirstResult(firstResult);
			search.setMaxResults(maxResults);
			search(search);
			return search.getSearchResults();
		} catch (final Exception e) {
			return handleException(indexName, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<HashMap<String, String>> search(final String indexName, final String[] searchStrings, final String[] searchFields,
			final String[] typeFields, final String[] sortFields, final boolean fragment, final int firstResult, final int maxResults) {
		try {
			Search search = new Search();
			search.setIndexName(indexName);

			search.setSearchStrings(Arrays.asList(searchStrings));
			search.setSearchFields(Arrays.asList(searchFields));
			search.setTypeFields(Arrays.asList(typeFields));
			search.setSortFields(Arrays.asList(sortFields));

			search.setFragment(fragment);
			search.setFirstResult(firstResult);
			search.setMaxResults(maxResults);
			search(search);
			return search.getSearchResults();
		} catch (final Exception e) {
			return handleException(indexName, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> search(final String indexName, final String[] searchStrings, final String[] searchFields,
			final String[] typeFields, final boolean fragment, final int firstResult, final int maxResults, final int distance, final double latitude,
			final double longitude) {
		try {
			Search search = new Search();
			search.setIndexName(indexName);
			search.setSearchStrings(Arrays.asList(searchStrings));
			search.setSearchFields(Arrays.asList(searchFields));
			search.setTypeFields(Arrays.asList(typeFields));
			search.setSortFields(Collections.EMPTY_LIST);

			search.setDistance(distance);
			search.setCoordinate(new Coordinate(latitude, longitude));

			search.setFragment(fragment);
			search.setFirstResult(firstResult);
			search.setMaxResults(maxResults);
			search(search);
			return search.getSearchResults();
		} catch (final Exception e) {
			return handleException(indexName, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Search search(final Search search) {
		try {
			SearchComplexSorted searchComplexSorted = getSearch(SearchComplexSorted.class, search.getIndexName());
			if (searchComplexSorted == null) {
				LOGGER.warn("Searcher null for index : " + search.getIndexName());
				return search;
			}
			String[] searchStrings = search.getSearchStrings().toArray(new String[search.getSearchStrings().size()]);
			String[] searchFields = search.getSearchFields().toArray(new String[search.getSearchFields().size()]);

			String[] typeFields;
			if (search.getTypeFields() == null || search.getTypeFields().size() < searchStrings.length) {
				typeFields = new String[searchStrings.length];
				Arrays.fill(typeFields, TypeField.STRING.fieldType());
			} else {
				typeFields = search.getTypeFields().toArray(new String[search.getTypeFields().size()]);
			}
			String[] sortFields;
			if (search.getSortFields() == null) {
				sortFields = new String[searchStrings.length];
			} else {
				sortFields = search.getSortFields().toArray(new String[search.getSortFields().size()]);
			}

			searchComplexSorted.setFirstResult(search.getFirstResult());
			searchComplexSorted.setFragment(search.isFragment());
			searchComplexSorted.setMaxResults(search.getMaxResults());
			searchComplexSorted.setSearchString(searchStrings);
			searchComplexSorted.setSearchField(searchFields);
			searchComplexSorted.setTypeFields(typeFields);
			searchComplexSorted.setSortField(sortFields);
			ArrayList<HashMap<String, String>> results = searchComplexSorted.execute();
			String[] searchStringsCorrected = searchComplexSorted.getCorrections(search.getSearchStrings()
					.toArray(new String[search.getSearchStrings().size()]));
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
	 * This method is particularly expensive, it will do a search on every index in the system.
	 */
	public Search searchAll(final Search search) {
		LOGGER.debug("Search all");
		try {
			String[] indexNames = monitorService.getIndexNames();
			List<Future<?>> futures = new ArrayList<Future<?>>();
			List<Search> searches = new ArrayList<Search>();
			for (final String indexName : indexNames) {
				final Search clonedSearch = cloneSearch(search, indexName);
				if (clonedSearch == null) {
					continue;
				}
				searches.add(clonedSearch);
				Runnable searchRunnable = new Runnable() {
					public void run() {
						// Search each index separately
						search(clonedSearch);
					}
				};
				String name = Integer.toString(clonedSearch.hashCode());
				Future<?> future = ThreadUtilities.submit(name, searchRunnable);
				futures.add(future);
			}
			ThreadUtilities.waitForFutures(futures, 60000);
			aggregateResults(search, searches);
		} catch (Exception e) {
			handleException(search.getIndexName(), e);
		}
		return search;
	}

	private void aggregateResults(final Search search, final List<Search> searches) {
		long totalHits = 0;
		float highScore = 0;
		long duration = 0;

		ArrayList<HashMap<String, String>> searchResults = new ArrayList<HashMap<String, String>>();

		for (final Search clonedSearch : searches) {
			// Consolidate the results, i.e. merge them
			List<HashMap<String, String>> searchSubResults = clonedSearch.getSearchResults();
			if (searchSubResults != null && searchSubResults.size() > 1) {
				HashMap<String, String> statistics = searchSubResults.remove(searchSubResults.size() - 1);
				totalHits += Long.parseLong(statistics.get(IConstants.TOTAL));
				highScore += Float.parseFloat(statistics.get(IConstants.SCORE));
				long subSearchDuration = Long.parseLong(statistics.get(IConstants.DURATION));
				duration = Math.max(duration, subSearchDuration);
				searchResults.addAll(searchSubResults);
			}
			ThreadUtilities.destroy(Integer.toString(clonedSearch.hashCode()));
		}

		HashMap<String, String> statistics = new HashMap<String, String>();
		statistics.put(IConstants.TOTAL, Long.toString(totalHits));
		statistics.put(IConstants.DURATION, Long.toString(duration));
		statistics.put(IConstants.SCORE, Float.toString(highScore));
		// Sort all the results according to the score
		Collections.sort(searchResults, new Comparator<HashMap<String, String>>() {
			@Override
			public int compare(final HashMap<String, String> o1, final HashMap<String, String> o2) {
				String scoreOneString = o1.get(IConstants.SCORE);
				String scoreTwoString = o2.get(IConstants.SCORE);
				Double scoreOne = Double.parseDouble(scoreOneString);
				Double scoreTwo = Double.parseDouble(scoreTwoString);
				return scoreOne.compareTo(scoreTwo);
			}
		});
		ArrayList<HashMap<String, String>> topResults = new ArrayList<HashMap<String, String>>();
		topResults.addAll(searchResults.subList(0, Math.min(search.getMaxResults(), searchResults.size())));

		String[] searchStrings = search.getSearchStrings().toArray(new String[search.getSearchStrings().size()]);
		SearchComplex searchSingle = new SearchComplex(null);
		searchSingle.setSearchString(searchStrings);
		searchSingle.addStatistics(searchStrings, topResults, totalHits, highScore, duration, null);

		search.setSearchResults(topResults);
	}

	@SuppressWarnings("unchecked")
	private Search cloneSearch(final Search search, final String indexName) {
		final Search clonedSearch = (Search) SerializationUtilities.clone(search);
		clonedSearch.setIndexName(indexName);

		String[] searchStrings = search.getSearchStrings().toArray(new String[search.getSearchStrings().size()]);
		String[] searchFields = monitorService.getIndexFieldNames(indexName);
		String[] typeFields = new String[0];
		if (searchFields == null || searchFields.length == 0) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Index : " + indexName + ", search fields : " + Arrays.deepToString(searchFields));
			}
			return null;
		}

		searchStrings = fillArray(searchFields, searchStrings);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Searching index : " + indexName + ", " + //
					Arrays.deepToString(searchStrings) + ", " + //
					Arrays.deepToString(searchFields) + ", " + //
					Arrays.deepToString(typeFields));
		}

		clonedSearch.setSearchStrings(Arrays.asList(searchStrings));
		clonedSearch.setSearchFields(Arrays.asList(searchFields));
		clonedSearch.setTypeFields(Arrays.asList(typeFields));
		clonedSearch.setSortFields(Collections.EMPTY_LIST);

		return clonedSearch;
	}

	private String[] fillArray(final String[] lengthOfThisArray, final String[] originalStrings) {
		String[] newStrings = new String[lengthOfThisArray.length];
		int minLength = Math.min(originalStrings.length, newStrings.length);
		System.arraycopy(originalStrings, 0, newStrings, 0, minLength);
		String fillerString = originalStrings != null && originalStrings.length > 0 ? originalStrings[0] : "";
		Arrays.fill(newStrings, minLength, newStrings.length, fillerString);
		return newStrings;
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