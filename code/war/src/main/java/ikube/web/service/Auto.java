package ikube.web.service;

import ikube.IConstants;
import ikube.model.Search;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.Timer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Michael couck
 * @since 01.03.12
 * @version 01.00
 */
@Component
@Path(Auto.AUTO)
@Scope(Auto.REQUEST)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Auto extends Resource {

	static final Logger LOGGER = LoggerFactory.getLogger(Auto.class);

	/** Constants for the paths to the web services. */
	public static final String AUTO = "/auto";
	public static final String SUGGEST = "/suggest";

	/**
	 * This method will return suggestions based on the closest match of the word in the index. The index can be a word list, which is probably the best choice,
	 * but doesn't have to be. If there are three words the, there will be suggestions for each word, and combinations of those suggestions, sorted by the score
	 * for the words.
	 * 
	 * <pre>
	 * 		Input: [disaster on island honshu again]
	 * 		Output: [disease in islanddia honalulu again], [dissapation ...] * the number of results expected, typically 6 or 8
	 * </pre>
	 * 
	 * @param request the Json request, with the {@link Search} object in it
	 * @param uriInfo information about the uri if any, currently not used
	 * @return the search result, or all the suggested strings in the {@link Search} object as search results, and the suggestions in the fragments
	 */
	@POST
	public Response auto(//
			@Context final HttpServletRequest request, //
			@Context final UriInfo uriInfo) {
		final Search search = unmarshall(Search.class, request);
		final ArrayList<HashMap<String, String>> autoResults = new ArrayList<HashMap<String, String>>();
		long duration = Timer.execute(new Timer.Timed() {
			@Override
			public void execute() {
				List<String> searchStrings = search.getSearchStrings();
				for (final String searchString : searchStrings) {
					StringBuilder[] autocompleteSuggestions = buildString(searchString, search);
					for (final StringBuilder autocompleteSuggestion : autocompleteSuggestions) {
						HashMap<String, String> result = new HashMap<String, String>();
						result.put(IConstants.FRAGMENT, autocompleteSuggestion.toString().trim());
						autoResults.add(result);
					}
				}
			}
		});
		// Add the statistics
		HashMap<String, String> statistics = new HashMap<String, String>();
		statistics.put(IConstants.TOTAL, Long.toString(autoResults.size()));
		statistics.put(IConstants.DURATION, Long.toString(duration));
		statistics.put(IConstants.SEARCH_STRINGS, search.getSearchStrings().toString());
		autoResults.add(statistics);
		search.setSearchResults(autoResults);
		return buildJsonResponse(search);
	}

	private StringBuilder[] buildString(final String searchString, final Search search) {
		StringBuilder[] autocompleteSuggestions = new StringBuilder[search.getMaxResults()];
		for (int i = 0; i < autocompleteSuggestions.length; i++) {
			autocompleteSuggestions[i] = new StringBuilder();
		}
		String[] words = StringUtils.split(searchString, ' ');
		Search cloneSearch = (Search) SerializationUtilities.clone(search);
		for (final String word : words) {
			cloneSearch.setSearchStrings(Arrays.asList(word));
			cloneSearch = searcherService.search(cloneSearch);
			ArrayList<HashMap<String, String>> results = cloneSearch.getSearchResults();
			// Remove the statistics, we don't need it
			results.remove(results.size() - 1);
			for (int i = 0; i < autocompleteSuggestions.length; i++) {
				if (results.size() > i) {
					String fragment = results.get(i).get(IConstants.FRAGMENT);
					autocompleteSuggestions[i].append(fragment);
				} else {
					autocompleteSuggestions[i].append(word);
				}
				autocompleteSuggestions[i].append(' ');
			}
		}
		return autocompleteSuggestions;
	}

	/**
	 * TODO Implement this method, with the top three results based on the words, as in the {@link Auto#auto(HttpServletRequest, UriInfo)} method, then the next
	 * three based on a thesaurus perhaps, i.e. similar words to the search phrase, could be anything. And finally suggestions based on similar searches, that
	 * will have to be classified with a k-means or similar algorithm.
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 */
	@POST
	@Path(Auto.SUGGEST)
	public Response suggestions(//
			@Context final HttpServletRequest request, //
			@Context final UriInfo uriInfo) {
		Search search = unmarshall(Search.class, request);
		Object results = searcherService.search(search);
		return buildJsonResponse(results);
	}

}