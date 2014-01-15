package ikube.web.service;

import com.sun.jersey.api.spring.Autowire;
import ikube.IConstants;
import ikube.analytics.IAnalyticsService;
import ikube.model.Search;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.Timer;
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
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Strangely enough, if this class name is changed to 'Twitter', Spring and Jersey do not inject the services. Hmmm..
 * . what's in a name huh?
 *
 * @author Michael couck
 * @version 01.00
 * @since 17.12.13
 */
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
    public static class TwitterSearch extends Search {

        long startHour;
        long minutsOfHistory;
        Object[][] timeLineSentiment;
        Object[][] positiveHeatMapData;

        public long getStartHour() {
            return startHour;
        }

        public void setStartHour(final long startHour) {
            this.startHour = startHour;
        }

        public long getMinutsOfHistory() {
            return minutsOfHistory;
        }

        public void setMinutsOfHistory(final long minutsOfHistory) {
            this.minutsOfHistory = minutsOfHistory;
        }

        public Object[][] getNegativeHeatMapData() {
            return negativeHeatMapData;
        }

        public void setNegativeHeatMapData(final Object[][] negativeHeatMapData) {
            this.negativeHeatMapData = negativeHeatMapData;
        }

        Object[][] negativeHeatMapData;

        public Object[][] getPositiveHeatMapData() {
            return positiveHeatMapData;
        }

        public void setPositiveHeatMapData(final Object[][] positiveHeatMapData) {
            this.positiveHeatMapData = positiveHeatMapData;
        }

        public Object[][] getTimeLineSentiment() {
            return timeLineSentiment;
        }

        public void setTimeLineSentiment(final Object[][] timeLineSentiment) {
            this.timeLineSentiment = timeLineSentiment;
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
    public Response happy(@Context final HttpServletRequest request, @Context final UriInfo uriInfo) {
        // Google Maps API heat map data format is {lat, lng, weight} eg. {42, 1.8, 3}
        final TwitterSearch twitterSearch = unmarshall(TwitterSearch.class, request);
        final long endTime = System.currentTimeMillis();
        final long minutesOfHistory = twitterSearch.getMinutsOfHistory();
        final long startTime = endTime - (minutesOfHistory * MINUTE_MILLIS);

        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                search(twitterSearch, startTime, endTime, "posi");
                twitterSearch.getSearchResults();

                search(twitterSearch, startTime, endTime, "nega");
            }
        });
        logger.info("Duration for heat map data : " + duration);
        return buildJsonResponse(twitterSearch);
    }

    private Object[][] heatMapData(final ArrayList<HashMap<String, String>> results) {
        Object[][] heatMapData = new Object[0][];
        for (final HashMap<String, String> result : results) {

        }
        return heatMapData;
    }

    @POST
    @Path(Anal.ANALYZE)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response twitter(@Context final HttpServletRequest request, @Context final UriInfo uriInfo) {
        final TwitterSearch search = unmarshall(TwitterSearch.class, request);
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                // First do the primary search for the term, language, etc...
                searcherService.search(search);
                // Get the time line for both positive and negative
                Object[][] invertedTimeLineSentiment = timeLineSentiment(search);
                search.setTimeLineSentiment(invertedTimeLineSentiment);
            }
        });

        HashMap<String, String> statistics = search.getSearchResults().get(search.getSearchResults().size() - 1);
        statistics.put(IConstants.DURATION, Double.toString(duration));
        return buildJsonResponse(search);
    }

    Object[][] timeLineSentiment(final TwitterSearch search) {
        // Now we have to search for positive and negative for each hour
        // going back as far as the user specified, aggregate the results in an
        // array for the chart
        int hour = 0;
        int period = (int) Math.abs(search.getStartHour());
        long startTime = System.currentTimeMillis() - (((long) (period)) * HOUR_MILLIS);
        long endTime = System.currentTimeMillis();
        logger.info("Start : " + startTime + ", end : " + endTime);
        // Periods plus one for the headers
        final Object[][] timeLineSentiment = new Object[3][period + 1];

        List<Future<?>> futures = new ArrayList<>();
        do {
            Future<?> future = search(search, period, endTime - HOUR_MILLIS, endTime, hour, timeLineSentiment);
            futures.add(future);
            // Plus an hour
            hour--;
            period--;
            endTime -= HOUR_MILLIS;
        } while (startTime < endTime);
        ThreadUtilities.waitForFutures(futures, 300);

        HashMap<String, String> statistics = new HashMap<>();
        addCount(timeLineSentiment[0], IConstants.POSITIVE, statistics);
        addCount(timeLineSentiment[1], IConstants.NEGATIVE, statistics);
        search.getSearchResults().add(statistics);

        // Invert the matrix
        Object[][] invertedTimeLineSentiment = invertMatrix(timeLineSentiment);
        // Set the headers
        invertedTimeLineSentiment[0][0] = "Hour";
        invertedTimeLineSentiment[0][1] = "Negative";
        invertedTimeLineSentiment[0][2] = "Positive";

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
                int positiveCount = search(search, periodTime, endTime, "posi");
                int negativeCount = search(search, periodTime, endTime, "nega");
                logger.info("Positive/negative : " + hour + "-" + positiveCount + "-" + negativeCount);
                timeLineSentiment[0][periods] = positiveCount;
                timeLineSentiment[1][periods] = negativeCount;
                timeLineSentiment[2][periods] = hour;
            }
        });
    }

    int search(final Search search, final long startTime, final long endTime, final String classification) {
        Search searchClone = SerializationUtilities.clone(Search.class, search);

        searchClone.getSearchStrings().add(startTime + "-" + endTime);
        searchClone.getSearchFields().add(CREATED_AT);
        searchClone.getOccurrenceFields().add(OCCURRENCE);
        searchClone.getTypeFields().add(IConstants.RANGE);

        searchClone.getSearchStrings().add(classification);
        searchClone.getSearchFields().add(IConstants.CLASSIFICATION);
        searchClone.getOccurrenceFields().add(OCCURRENCE);
        searchClone.getTypeFields().add(IConstants.STRING);

        // searchClone.setSortFields(Arrays.asList(CREATED_AT));
        // searchClone.setSortDirections(Arrays.asList(Boolean.TRUE.toString()));

        searchClone = searcherService.search(searchClone);
        ArrayList<HashMap<String, String>> searchResults = searchClone.getSearchResults();
        HashMap<String, String> statistics = searchResults.remove(searchResults.size() - 1);
        String total = statistics.get(IConstants.TOTAL);
        search.setSearchResults(searchResults);
        return Integer.valueOf(total);
    }

}