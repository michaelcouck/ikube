package ikube.web.service;

import com.sun.jersey.api.spring.Autowire;
import com.tomgibara.cluster.gvm.dbl.DblClusters;
import com.tomgibara.cluster.gvm.dbl.DblListKeyer;
import com.tomgibara.cluster.gvm.dbl.DblResult;
import ikube.IConstants;
import ikube.analytics.IAnalyticsService;
import ikube.model.Search;
import ikube.model.SearchTwitter;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.Timer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Strangely enough, if this class name is changed to 'Twitter', Spring and Jersey
 * do not inject the services. Hmmm... what's in a name huh? Could be because there is a mapping
 * for a tile called twitter? OR a Jsp called twitter and without the suffix, well you know.
 *
 * @author Michael couck
 * @version 01.00
 * @since 17-12-2013
 */
@Provider
@Autowire
@Component
@Path(Tweets.TWITTER)
@Scope(Resource.REQUEST)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings("SpringJavaAutowiringInspection")
@Api(description = "The Twitter rest resource, provides twitter results, like a heat map and results of tweets go-locations.")
public class Tweets extends Resource {

    /**
     * Web service paths.
     */
    public static final String TWITTER = "/twitter";
    public static final String HAPPY = "/happy";
    public static final String ANALYZE = "/analyze";

    static final String CREATED_AT = "created-at";
    static final long MINUTE_MILLIS = 1000 * 60;
    static final long HOUR_MILLIS = MINUTE_MILLIS * 60;

    @Autowired
    protected IAnalyticsService analyticsService;

    @POST
    @Path(Tweets.HAPPY)
    @Api(description = "This resource will generate a heat map of the positive tweets on the planet, and " +
            "collect the last +-10 000 tweets for display on the world map.",
            produces = SearchTwitter.class)
    public Response happy(final SearchTwitter twitterSearch) {
        // Google Maps API heat map data format is {lat, lng, weight} eg. {42, 1.8, 3}
        final long endTime = System.currentTimeMillis();
        final long minutesOfHistory = twitterSearch.getMinutesOfHistory();
        final long startTime = endTime - (minutesOfHistory * MINUTE_MILLIS);
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                search(twitterSearch, startTime, endTime, twitterSearch.getClassification());
                ArrayList<HashMap<String, String>> searchResults = twitterSearch.getSearchResults();
                // Remove the statistics for the heat map calculation
                HashMap<String, String> statistics = searchResults.remove(searchResults.size() - 1);
                Object[][] heatMapData = heatMapData(searchResults, twitterSearch.getClusters());
                searchResults.add(statistics);
                twitterSearch.setHeatMapData(heatMapData);
                // Reduce the search results to something reasonable,
                // so we always have some tweets, but don't send 10 meg to the front
                int maxResults = 10000;
                if (searchResults.size() > maxResults) {
                    List<HashMap<String, String>> subList = searchResults.subList(0, maxResults);
                    searchResults = new ArrayList<>(subList);
                    twitterSearch.setSearchResults(searchResults);
                }
                logger.info("Heat map data size : " + heatMapData.length + ", " + twitterSearch.getSearchResults().size());
            }
        });
        logger.info("Duration for heat map data : " + duration);
        return buildResponse(twitterSearch);
    }

    @POST
    @Path(Tweets.ANALYZE)
    @Api(description = "This resource will do searches, positive and negative, in increments of an hour, and return " +
            "the data in an array that can be displayed on a time based graph, showing the sentiment trend over time.",
            produces = SearchTwitter.class)
    public Response twitter(final SearchTwitter twitterSearch) {
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                // First do the primary search for the term, language, etc...
                searcherService.search(twitterSearch);
                // Get the time line for both positive and negative
                setTimeLineSentiment(twitterSearch);
            }
        });

        if (twitterSearch.getSearchResults() != null && twitterSearch.getSearchResults().size() > 0) {
            HashMap<String, String> statistics = twitterSearch.getSearchResults().get(twitterSearch.getSearchResults().size() - 1);
            statistics.put(IConstants.DURATION, Double.toString(duration));
        }
        return buildResponse(twitterSearch);
    }

    /**
     * This method takes all the geospatial points and clusters them into a maximum of clusters so they can be put on a map.
     *
     * @param results the results to cluster the points for
     * @return the clustered geo points in the form [[lat, lng, weight], []...]
     */
    Object[][] heatMapData(final ArrayList<HashMap<String, String>> results, final int capacity) {
        DblClusters<List<double[]>> clusters = new DblClusters<>(2, capacity);
        clusters.setKeyer(new DblListKeyer<double[]>());
        double[] coordinate = new double[2];
        for (final HashMap<String, String> result : results) {
            String latString = result.get(IConstants.LATITUDE);
            String longString = result.get(IConstants.LONGITUDE);
            if (StringUtils.isEmpty(latString) || StringUtils.isEmpty(longString)) {
                continue;
            }
            double latitude = Double.parseDouble(latString);
            double longitude = Double.parseDouble(longString);
            coordinate[0] = latitude;
            coordinate[1] = longitude;
            List<double[]> key = new ArrayList<>();
            key.add(coordinate);
            clusters.add(1.0, coordinate, key);
        }
        List<DblResult<List<double[]>>> clustered = clusters.results();
        logger.debug("Clusters : " + clustered.size());
        Object[][] heatMapData = new Object[clustered.size()][3];
        for (int i = 0; i < clustered.size(); i++) {
            final DblResult<List<double[]>> cluster = clustered.get(i);
            int count = cluster.getCount();
            coordinate = cluster.getCoords();
            Object[] heatMapDatum = {coordinate[0], coordinate[1], count};
            heatMapData[i] = heatMapDatum;
        }
        return heatMapData;
    }

    @SuppressWarnings("unchecked")
    Object[][] setTimeLineSentiment(final SearchTwitter search) {
        // Now we have to search for positive and negative for each hour
        // going back as far as the user specified, aggregate the results in an
        // array for the chart
        long currentTime = System.currentTimeMillis();
        int period = (int) Math.abs(search.getStartHour());
        long startTime = currentTime - period * HOUR_MILLIS;
        long endTime = startTime + HOUR_MILLIS;

        // Periods plus one for the headers
        final Object[][] timeLineSentiment = new Object[3][period + 1];

        List<Future<Object>> futures = new ArrayList<>();
        do {
            Future<Object> future = (Future<Object>) search(search, period, startTime, endTime, timeLineSentiment);
            futures.add(future);
            // Plus an hour
            period--;

            startTime += HOUR_MILLIS;
            endTime += HOUR_MILLIS;
        } while (startTime < currentTime);
        ThreadUtilities.waitForFutures(futures, 300);

        ArrayList<HashMap<String, String>> searchResults = search.getSearchResults();
        HashMap<String, String> statistics = searchResults.get(searchResults.size() - 1);
        addCount(timeLineSentiment[0], IConstants.POSITIVE, statistics);
        addCount(timeLineSentiment[1], IConstants.NEGATIVE, statistics);

        // Invert the matrix
        Object[][] invertedTimeLineSentiment = invertMatrix(timeLineSentiment);
        // Set the headers
        invertedTimeLineSentiment[0][0] = "Hour";
        invertedTimeLineSentiment[0][1] = "Negative";
        invertedTimeLineSentiment[0][2] = "Positive";

        search.setTimeLineSentiment(invertedTimeLineSentiment);

        return invertedTimeLineSentiment;
    }

    Future<?> search(final Search search, final int period, final long startTime, final long endTime, final Object[][] timeLineSentiment) {
        return ThreadUtilities.submit(this.getClass().getSimpleName(), new Runnable() {
            public void run() {
                int positiveCount = search(search, startTime, endTime, IConstants.POSITIVE);
                int negativeCount = search(search, startTime, endTime, IConstants.NEGATIVE);
                timeLineSentiment[0][period] = positiveCount;
                timeLineSentiment[1][period] = negativeCount;
                timeLineSentiment[2][period] = -period;
                // logger.error("Time line : " + Arrays.deepToString(timeLineSentiment));
            }
        });
    }

    void addCount(final Object[] count, final String property, final HashMap<String, String> statistics) {
        int total = 0;
        for (int i = 1; i < count.length; i++) {
            if (count[i] != null) {
                total += (Integer) count[i];
            }
        }
        statistics.put(property, Integer.toString(total));
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    int search(final Search search, final long startTime, final long endTime, final String classification) {
        Search searchClone = SerializationUtilities.clone(Search.class, search);
        String timeRange = new StringBuilder(Long.toString(startTime)).append("-").append(endTime).toString();

        List<String> searchStrings = new ArrayList<>(searchClone.getSearchStrings());
        List<String> searchFields = new ArrayList<>(searchClone.getSearchFields());
        List<String> searchOccurrenceFields = new ArrayList<>(searchClone.getOccurrenceFields());
        List<String> searchTypeFields = new ArrayList<>(searchClone.getTypeFields());

        searchStrings.add(timeRange);
        searchFields.add(CREATED_AT);
        searchOccurrenceFields.add(IConstants.MUST);
        searchTypeFields.add(IConstants.RANGE);

        searchStrings.add(classification);
        searchFields.add(IConstants.CLASSIFICATION);
        searchOccurrenceFields.add(IConstants.MUST);
        searchTypeFields.add(IConstants.STRING);

        searchClone.setSearchStrings(searchStrings);
        searchClone.setSearchFields(searchFields);
        searchClone.setOccurrenceFields(searchOccurrenceFields);
        searchClone.setTypeFields(searchTypeFields);

        // This is not necessary
        // searchClone.setSortFields(Arrays.asList(CREATED_AT));
        // searchClone.setSortDirections(Arrays.asList(Boolean.TRUE.toString()));

        // searchClone.setSearchResults(null);
        searchClone = searcherService.search(searchClone);
        ArrayList<HashMap<String, String>> searchCloneResults = new ArrayList<>(searchClone.getSearchResults());
        HashMap<String, String> statistics = searchCloneResults.get(searchCloneResults.size() - 1);
        String total = statistics.get(IConstants.TOTAL);
        search.setSearchResults(searchCloneResults);
        // System.out.println("Total : " + total);
        // System.out.println(startTime + "-" + endTime + ", " + new Date(startTime) + "-" + new Date(endTime));
        return Integer.valueOf(total);
    }

}