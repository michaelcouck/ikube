package ikube.web.service;

import com.sun.jersey.api.spring.Autowire;
import com.tomgibara.cluster.gvm.dbl.DblClusters;
import com.tomgibara.cluster.gvm.dbl.DblListKeyer;
import com.tomgibara.cluster.gvm.dbl.DblResult;
import ikube.IConstants;
import ikube.analytics.IAnalyticsService;
import ikube.model.Search;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.Timer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Strangely enough, if this class name is changed to 'Twitter', Spring and Jersey
 * do not inject the services. Hmmm... what's in a name huh?
 *
 * @author Michael couck
 * @version 01.00
 * @since 17-12-2013
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@Provider
@Autowire
@Component
@Path(Anal.TWITTER)
@Scope(Resource.REQUEST)
@Produces(MediaType.APPLICATION_JSON)
public class Anal extends Resource {

    /**
     * Custom search transfer object for the Twitter application.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static class TwitterSearch extends Search {

        int clusters;
        long startHour;
        long minutesOfHistory;
        String classification;
        Object[][] heatMapData;
        Object[][] timeLineSentiment;

        public Object[][] getTimeLineSentiment() {
            return timeLineSentiment;
        }

        public void setTimeLineSentiment(Object[][] timeLineSentiment) {
            this.timeLineSentiment = timeLineSentiment;
        }

        public Object[][] getHeatMapData() {
            return heatMapData;
        }

        public void setHeatMapData(Object[][] heatMapData) {
            this.heatMapData = heatMapData;
        }

        public String getClassification() {
            return classification;
        }

        public void setClassification(String classification) {
            this.classification = classification;
        }

        public long getMinutesOfHistory() {
            return minutesOfHistory;
        }

        public void setMinutesOfHistory(long minutesOfHistory) {
            this.minutesOfHistory = minutesOfHistory;
        }

        public long getStartHour() {
            return startHour;
        }

        public void setStartHour(long startHour) {
            this.startHour = startHour;
        }

        public int getClusters() {
            return clusters;
        }

        public void setClusters(int clusters) {
            this.clusters = clusters;
        }
    }

    /**
     * Web service paths.
     */
    public static final String TWITTER = "/twitter";
    public static final String HAPPY = "/happy";
    public static final String ANALYZE = "/analyze";

    static final String OCCURRENCE = "must";
    static final String CREATED_AT = "created-at";
    static final long MINUTE_MILLIS = 60 * 1000;
    static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;

    @Autowired
    protected IAnalyticsService analyticsService;

    @POST
    @Path(Anal.HAPPY)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response happy(@Context final HttpServletRequest request) {
        // Google Maps API heat map data format is {lat, lng, weight} eg. {42, 1.8, 3}
        final TwitterSearch twitterSearch = unmarshall(TwitterSearch.class, request);
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
        return buildJsonResponse(twitterSearch);
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
        logger.info("Clusters : " + clustered.size());
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

    @POST
    @Path(Anal.ANALYZE)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response twitter(@Context final HttpServletRequest request) {
        final TwitterSearch search = unmarshall(TwitterSearch.class, request);
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                // First do the primary search for the term, language, etc...
                searcherService.search(search);
                // Get the time line for both positive and negative
                setTimeLineSentiment(search);
            }
        });

        if (search.getSearchResults() != null && search.getSearchResults().size() > 0) {
            HashMap<String, String> statistics = search.getSearchResults().get(search.getSearchResults().size() - 1);
            statistics.put(IConstants.DURATION, Double.toString(duration));
        }
        return buildJsonResponse(search);
    }

    @SuppressWarnings("unchecked")
    Object[][] setTimeLineSentiment(final TwitterSearch search) {
        // Now we have to search for positive and negative for each hour
        // going back as far as the user specified, aggregate the results in an
        // array for the chart
        int hour = 0;
        int period = (int) Math.abs(search.getStartHour());
        long startTime = System.currentTimeMillis() - (((long) (period)) * HOUR_MILLIS);
        long endTime = System.currentTimeMillis();
        // logger.info("Start : " + startTime + ", end : " + endTime);
        // Periods plus one for the headers
        final Object[][] timeLineSentiment = new Object[3][period + 1];

        List<Future<Object>> futures = new ArrayList<>();
        do {
            Future<Object> future = (Future<Object>) search(search, period, endTime - HOUR_MILLIS, endTime, hour, timeLineSentiment);
            futures.add(future);
            // Plus an hour
            hour--;
            period--;
            endTime -= HOUR_MILLIS;
        } while (startTime < endTime);
        ThreadUtilities.waitForFutures(futures, 300);

        // new HashMap<>();
        ArrayList<HashMap<String, String>> searchResults = search.getSearchResults();
        HashMap<String, String> statistics = searchResults.get(searchResults.size() - 1);
        addCount(timeLineSentiment[0], IConstants.POSITIVE, statistics);
        addCount(timeLineSentiment[1], IConstants.NEGATIVE, statistics);
        // search.getSearchResults().add(statistics);

        // Invert the matrix
        Object[][] invertedTimeLineSentiment = invertMatrix(timeLineSentiment);
        // Set the headers
        invertedTimeLineSentiment[0][0] = "Hour";
        invertedTimeLineSentiment[0][1] = "Negative";
        invertedTimeLineSentiment[0][2] = "Positive";

        search.setTimeLineSentiment(invertedTimeLineSentiment);

        return invertedTimeLineSentiment;
    }

    void addCount(final Object[] count, final String property, final HashMap<String, String> statistics) {
        int total = 0;
        for (int i = 1; i < count.length; i++) {
            total += (Integer) count[i];
        }
        statistics.put(property, Integer.toString(total));
    }

    Future<?> search(final Search search, final int periods, final long periodTime, final long endTime, final int hour, final Object[][] timeLineSentiment) {
        return ThreadUtilities.submit(this.getClass().getSimpleName(), new Runnable() {
            public void run() {
                int positiveCount = search(search, periodTime, endTime, IConstants.POSITIVE);
                int negativeCount = search(search, periodTime, endTime, IConstants.NEGATIVE);
                /*if (logger.isDebugEnabled()) {
                    logger.debug("Positive/negative : " + hour + "-" + positiveCount + "-" + negativeCount);
                }*/
                timeLineSentiment[0][periods] = positiveCount;
                timeLineSentiment[1][periods] = negativeCount;
                timeLineSentiment[2][periods] = hour;
            }
        });
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    int search(final Search search, final long startTime, final long endTime, final String classification) {
        Search searchClone = SerializationUtilities.clone(Search.class, search);
        String timeRange = new StringBuilder(Long.toString(startTime)).append("-").append(endTime).toString();
        searchClone.getSearchStrings().add(timeRange);
        searchClone.getSearchFields().add(CREATED_AT);
        searchClone.getOccurrenceFields().add(OCCURRENCE);
        searchClone.getTypeFields().add(IConstants.RANGE);

        searchClone.getSearchStrings().add(classification);
        searchClone.getSearchFields().add(IConstants.CLASSIFICATION);
        searchClone.getOccurrenceFields().add(OCCURRENCE);
        searchClone.getTypeFields().add(IConstants.STRING);

        // This is not necessary
        // searchClone.setSortFields(Arrays.asList(CREATED_AT));
        // searchClone.setSortDirections(Arrays.asList(Boolean.TRUE.toString()));

        /*if (logger.isDebugEnabled()) {
            logger.debug("Search range : " + timeRange);
        }*/

        searchClone = searcherService.search(searchClone);
        ArrayList<HashMap<String, String>> searchCloneResults = searchClone.getSearchResults();
        HashMap<String, String> statistics = searchCloneResults.get(searchCloneResults.size() - 1);
        String total = statistics.get(IConstants.TOTAL);
        search.setSearchResults(searchCloneResults);
        // logger.info("Total : " + total);
        return Integer.valueOf(total);
    }

}