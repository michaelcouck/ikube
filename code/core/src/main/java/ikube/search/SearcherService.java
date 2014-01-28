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
import java.util.*;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Michael Couck
 * @version 02.00
 * @see ISearcherService
 * @since 21.11.10
 */
@Component
@SuppressWarnings("SpringJavaAutowiringInspection")
public class SearcherService implements ISearcherService {

    static final Logger LOGGER = LoggerFactory.getLogger(SearcherService.class);

    static final int MAX_MERGE_SIZE = 1000;
    static final int MAX_PERSIST_SIZE = 1000;
    private static final ArrayList<HashMap<String, String>> EMPTY_RESULTS = new ArrayList<>();

    /**
     * The database that we persist the searches to.
     */
    @Autowired
    private IDataBase dataBase;
    /**
     * The service to get system contexts and other bric-a-brac.
     */
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
            ikube.search.Search searchAction;
            if (search.getCoordinate() != null && search.getDistance() != 0) {
                searchAction = getSearch(SearchSpatial.class, search.getIndexName());
                ((SearchSpatial) searchAction).setDistance(search.getDistance());
                ((SearchSpatial) searchAction).setCoordinate(search.getCoordinate());
            } else if (search.getSortFields() == null || search.getSortFields().size() == 0) {
                searchAction = getSearch(SearchComplex.class, search.getIndexName());
            } else {
                searchAction = getSearch(SearchComplexSorted.class, search.getIndexName());
            }

            if (searchAction == null) {
                LOGGER.debug("Searcher null for index : " + search.getIndexName());
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
            String[] sortDirections;
            if (search.getSortDirections() == null) {
                sortDirections = new String[searchStrings.length];
            } else {
                sortDirections = search.getSortDirections().toArray(new String[search.getSortDirections().size()]);
            }
            String[] occurrenceFields;
            if (search.getOccurrenceFields() == null) {
                occurrenceFields = new String[searchStrings.length];
                Arrays.fill(occurrenceFields, BooleanClause.Occur.SHOULD.name());
            } else {
                occurrenceFields = search.getOccurrenceFields().toArray(new String[search.getOccurrenceFields().size()]);
            }

            searchAction.setFirstResult(search.getFirstResult());
            searchAction.setFragment(search.isFragment());
            searchAction.setMaxResults(search.getMaxResults());
            searchAction.setSearchStrings(searchStrings);
            searchAction.setSearchFields(searchFields);
            searchAction.setTypeFields(typeFields);
            searchAction.setSortFields(sortFields);
            searchAction.setSortDirections(sortDirections);
            searchAction.setOccurrenceFields(occurrenceFields);

            ArrayList<HashMap<String, String>> results = searchAction.execute();
            String[] searchStringsCorrected = searchAction.getCorrections(search.getSearchStrings().toArray(new String[search.getSearchStrings().size()]));
            if (searchStringsCorrected != null) {
                search.setCorrectedSearchStrings(Arrays.asList(searchStringsCorrected));
            }
            search.setCorrections(searchStringsCorrected != null && searchStringsCorrected.length > 0);
            search.setSearchResults(results);
            // persistSearch(search);
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
            List<Future<?>> futures = new ArrayList<>();
            List<Search> searches = new ArrayList<>();
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
            for (final Search clonedSearch : searches) {
                ThreadUtilities.destroy(Integer.toString(clonedSearch.hashCode()));
            }
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

        ArrayList<HashMap<String, String>> searchResults = new ArrayList<>();

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
        }

        // HashMap<String, String> statistics = new HashMap<>();
        // statistics.put(IConstants.TOTAL, Long.toString(totalHits));
        // statistics.put(IConstants.DURATION, Long.toString(duration));
        // statistics.put(IConstants.SCORE, Float.toString(highScore));
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
        ArrayList<HashMap<String, String>> topResults = new ArrayList<>();
        topResults.addAll(searchResults.subList(0, Math.min(search.getMaxResults(), searchResults.size())));

        String[] searchStrings = search.getSearchStrings().toArray(new String[search.getSearchStrings().size()]);
        SearchComplex searchSingle = new SearchComplex(null);
        searchSingle.setSearchStrings(searchStrings);
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
        LOGGER.error("Exception doing search on : " + indexName + ", " + e.getLocalizedMessage());
        LOGGER.debug(null, e);
        return EMPTY_RESULTS;
    }

    /**
     * This method will return an instance of the search class, based on the class in the parameter list and the index context name. For each search there is an
     * instance created for the searcher classes to avoid thread overlap. The instance is created using reflection :( but is there a more elegant way?
     *
     * @param <T>       the type of class that is expected as a category
     * @param klass     the class of the searcher
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
                        Constructor<?> constructor = klass.getConstructor(IndexSearcher.class, Analyzer.class);
                        search = (T) constructor.newInstance(indexContext.getMultiSearcher(), indexContext.getAnalyzer());
                    } else {
                        Constructor<?> constructor = klass.getConstructor(IndexSearcher.class);
                        search = (T) constructor.newInstance(indexContext.getMultiSearcher());
                    }
                }
                break;
            }
        }
        return search;
    }

    private long merged = System.currentTimeMillis();
    private Map<Long, Search> mergeBatch = new HashMap<>();
    private List<Search> persistBatch = new ArrayList<>();

    /**
     * TODO This needs to be re-implemented for optimization and performance. Two possibilities, either create
     * an index on the hash column in the search table or keep everything in memory some how.
     */
    protected synchronized void persistSearch(final Search search) {
        final String indexName = search.getIndexName();
        final String[] searchStrings = search.getSearchStrings().toArray(new String[search.getSearchStrings().size()]);
        final ArrayList<HashMap<String, String>> results = search.getSearchResults();
        // Don't persist the auto complete searches
        if (IConstants.AUTOCOMPLETE.equals(indexName) || results == null) {
            return;
        }
        if (searchStrings.length == 0) {
            return;
        }
        Map<String, String> statistics = results.get(results.size() - 1);
        // Add the index name to the statistics here, not elegant, I know
        statistics.put(IConstants.INDEX_NAME, indexName);

        List<String> cleanedSearchStrings = new ArrayList<>(Arrays.asList(searchStrings));
        String string;
        Iterator<String> iterator = cleanedSearchStrings.iterator();
        do {
            string = iterator.next();
            if (StringUtils.isEmpty(string)) {
                iterator.remove();
            }
        } while (iterator.hasNext());
        if (cleanedSearchStrings.size() == 0) {
            return;
        }
        long hash = HashUtilities.hash(cleanedSearchStrings.toString());
        try {
            Search dbSearch = dataBase.findCriteria(Search.class, new String[]{"hash"}, new Object[]{hash});
            if (dbSearch != null) {
                Search mergeSearch = mergeBatch.get(hash);
                if (mergeSearch != null) {
                    mergeSearch.setCount(mergeSearch.getCount() + 1);
                } else {
                    dbSearch.setCount(dbSearch.getCount() + 1);
                    mergeBatch.put(hash, dbSearch);
                }
                if (mergeBatch.size() > MAX_MERGE_SIZE || System.currentTimeMillis() - merged > 1000 * 60 * 5) {
                    LOGGER.info("Merging searches : " + mergeBatch.size());
                    merged = System.currentTimeMillis();
                    dataBase.mergeBatch(new ArrayList<>(mergeBatch.values()));
                    mergeBatch.clear();
                }
                // dataBase.merge(dbSearch);
                // dataBase.executeUpdate(Search.UPDATE_SEARCH_COUNT_SEARCHES, new String[]{"count", "indexName"}, new Object[]{count, indexName});
            } else {
                search.setCount(1);
                search.setHash(hash);
                search.setTotalResults(Integer.parseInt(statistics.get(IConstants.TOTAL)));
                search.setHighScore(Double.parseDouble(statistics.get(IConstants.SCORE)));
                persistBatch.add(search);
                if (persistBatch.size() > MAX_PERSIST_SIZE) {
                    LOGGER.info("Persisting searches : " + persistBatch.size());
                    dataBase.persistBatch(persistBatch);
                    persistBatch.clear();
                }
                // dataBase.persist(search);
            }
        } catch (final Exception e) {
            LOGGER.debug(null, e);
            LOGGER.info("Exception setting search in database : ", e.getMessage());
        }
    }

}