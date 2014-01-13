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
import java.util.Arrays;
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

    public static class TwitterSearch extends Search {

        String startHour;
        Object[][] timeLineSentiment;

        public String getStartHour() {
            return startHour;
        }

        public void setStartHour(String startHour) {
            this.startHour = startHour;
        }

        public Object[][] getTimeLineSentiment() {
            return timeLineSentiment;
        }

        public void setTimeLineSentiment(Object[][] timeLineSentiment) {
            this.timeLineSentiment = timeLineSentiment;
        }

    }

    public static final String TWITTER = "/twitter";
    public static final String HAPPY = "/happy";
    public static final String ANALYZE = "/analyze";

    static final String CREATED_AT = "created-at";
    static final String CLASSIFICATION = "classification";
    static final String OCCURRENCE = "must";

    static final long HOUR_MILLIS = 60 * 60 * 1000;

    @Autowired
    protected IAnalyticsService analyticsService;

    @POST
    @Path(Anal.HAPPY)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response happy(@Context final HttpServletRequest request, @Context final UriInfo uriInfo) {
        final TwitterSearch search = unmarshall(TwitterSearch.class, request);
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                // Now we have to search based on country and language and aggregate
                // the results for display on the map i.e. the global break down for positive and negative
            }
        });
        return buildJsonResponse(null);
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
        int period = Math.abs(Integer.parseInt(search.getStartHour()));
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
                int positiveCount = count(search, periodTime, endTime, IConstants.POSITIVE);
                int negativeCount = count(search, periodTime, endTime, IConstants.NEGATIVE);
                logger.info("Positive/negative : " + hour + "-" + positiveCount + "-" + negativeCount);
                timeLineSentiment[0][periods] = positiveCount;
                timeLineSentiment[1][periods] = negativeCount;
                timeLineSentiment[2][periods] = hour;
            }
        });
    }

    int count(final Search search, final long startTime, final long endTime, final String classification) {
        Search searchClone = SerializationUtilities.clone(Search.class, search);

        searchClone.getSearchStrings().add(startTime + "-" + endTime);
        searchClone.getSearchFields().add(CREATED_AT);
        searchClone.getOccurrenceFields().add(OCCURRENCE);
        searchClone.getTypeFields().add(IConstants.RANGE);

        searchClone.getSearchStrings().add(classification);
        searchClone.getSearchFields().add(CLASSIFICATION);
        searchClone.getOccurrenceFields().add(OCCURRENCE);
        searchClone.getTypeFields().add(IConstants.STRING);

        searchClone.setSortFields(Arrays.asList(CREATED_AT));
        searchClone.setSortDirections(Arrays.asList(Boolean.TRUE.toString()));

        searchClone = searcherService.search(searchClone);
        ArrayList<HashMap<String, String>> searchResults = searchClone.getSearchResults();
        HashMap<String, String> statistics = searchResults.get(searchResults.size() - 1);
        return Integer.valueOf(statistics.get(IConstants.TOTAL));
    }

}