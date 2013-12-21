package ikube.web.service;

import ikube.IConstants;
import ikube.analytics.IAnalyticsService;
import ikube.model.Search;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.spring.Autowire;

/**
 * Strangely enough, if this class name is changed to 'Twitter', Spring and Jersey do not inject the services. Hmmm... what's in a name huh?
 * 
 * @author Michael couck
 * @since 17.12.13
 * @version 01.00
 */
@Provider
@Autowire
@Component
@Path(Anal.TWITTER)
@Scope(Resource.REQUEST)
@Produces(MediaType.APPLICATION_JSON)
public class Anal extends Resource {

	public static class TwitterSearch extends Search {

		private Object[][] timeLineSentiment;

		public Object[][] getTimeLineSentiment() {
			return timeLineSentiment;
		}

		public void setTimeLineSentiment(Object[][] timeLineSentiment) {
			this.timeLineSentiment = timeLineSentiment;
		}

	}

	public static final String TWITTER = "/twitter";
	public static final String ANALYZE = "/analyze";

	static final String CREATED_AT = "created-at";
	static final String CLASSIFICATION = "classification";
	static final String OCCURRENCE = "must";

	@Autowired
	protected IAnalyticsService analyticsService;

	@POST
	@Path(Anal.ANALYZE)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response twitter(@Context final HttpServletRequest request, @Context final UriInfo uriInfo) {
		final TwitterSearch search = unmarshall(TwitterSearch.class, request);
		long duration = Timer.execute(new Timer.Timed() {
			@Override
			public void execute() {
				// First do the primary search for the term, language, etc...
				searcherService.search(search);
				// Get the time line for both positive and negative
				Object[][] invertedTimeLineSentiment = timeLineSentiment(search);
				search.setTimeLineSentiment(invertedTimeLineSentiment);
				// Now we have to search based on country and language and aggregate
				// the results for display on the map i.e. the global break down for positive and negative
			}
		});

		search.getSearchResults().get(search.getSearchResults().size() - 1).put(IConstants.DURATION, Long.toString(duration));
		return buildJsonResponse(search);
	}

	Object[][] timeLineSentiment(final Search search) {
		// Now we have to search for positive and negative for each hour
		// going back as far as the user specified, aggregate the results in an
		// array for the chart
		long[] range = range(search);
		long startTime = range[0];
		long endTime = range[1];
		long periodTime = endTime - (1000 * 60 * 60);
		int periods = (int) ((endTime - startTime) / 1000l / 60l / 60l);
		final Object[][] timeLineSentiment = new Object[3][periods + 1];
		int hour = 0;

		List<Future<?>> futures = new ArrayList<>();

		do {

			final long periodTime_ = periodTime;
			final long endTime_ = endTime;
			final int periods_ = periods;
			final int hour_ = hour;

			Future<?> future = ThreadUtilities.submit(this.getClass().getSimpleName(), new Runnable() {
				public void run() {
					int positiveCount = count(search, periodTime_, endTime_, IConstants.POSITIVE);
					int negativeCount = count(search, periodTime_, endTime_, IConstants.NEGATIVE);
					timeLineSentiment[0][periods_] = Integer.valueOf(positiveCount);
					timeLineSentiment[1][periods_] = Integer.valueOf(negativeCount);
					timeLineSentiment[2][periods_] = Integer.valueOf(hour_);
				}
			});
			futures.add(future);

			// Plus an hour
			hour--;
			periods--;
			endTime = periodTime;
			periodTime -= 1000 * 60 * 60;
		} while (startTime < endTime);
		ThreadUtilities.waitForFutures(futures, 180);
		// Invert the matrix
		Object[][] invertedTimeLineSentiment = invertMatrix(timeLineSentiment);
		// Set the headers
		invertedTimeLineSentiment[0][0] = "Hour";
		invertedTimeLineSentiment[0][1] = "Negative";
		invertedTimeLineSentiment[0][2] = "Positive";
		return invertedTimeLineSentiment;
	}

	long[] range(final Search search) {
		List<String> searchFields = search.getSearchFields();
		int createdAtIndex = searchFields.indexOf(CREATED_AT);
		String timeRange = search.getSearchStrings().get(createdAtIndex);
		String[] timeRangeArray = StringUtils.split(timeRange, '-');
		return new long[] { Long.parseLong(timeRangeArray[0]), Long.parseLong(timeRangeArray[1]) };
	}

	int count(final Search search, final long startTime, final long endTime, final String classification) {
		Search searchClone = SerializationUtilities.clone(Search.class, search);
		int createdAtIndex = searchClone.getSearchFields().indexOf(CREATED_AT);
		int classificationIndex = searchClone.getSearchFields().indexOf(CLASSIFICATION);
		if (classificationIndex < 0) {
			searchClone.getSearchStrings().add(classification);
			searchClone.getSearchFields().add(CLASSIFICATION);
			searchClone.getOccurrenceFields().add(OCCURRENCE);
			searchClone.getTypeFields().add(String.class.getSimpleName().toLowerCase());
		} else {
			searchClone.getSearchStrings().set(classificationIndex, classification);
		}
		searchClone.getSearchStrings().set(createdAtIndex, startTime + "-" + endTime);

		ArrayList<HashMap<String, String>> searchResults = searcherService.search(searchClone).getSearchResults();
		HashMap<String, String> statistics = searchResults.get(searchResults.size() - 1);
		return Integer.valueOf(statistics.get(IConstants.TOTAL));
	}

}