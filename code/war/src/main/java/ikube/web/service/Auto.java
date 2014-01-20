package ikube.web.service;

import ikube.IConstants;
import ikube.model.Search;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.Timer;
import org.apache.commons.lang.StringUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Michael couck
 * @version 01.00
 * @since 01.03.12
 */
@Component
@Path(Auto.AUTO)
@Scope(Auto.REQUEST)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Auto extends Resource {

    /**
     * Constants for the paths to the web services.
     */
    public static final String AUTO = "/auto";
    public static final String SUGGEST = "/suggest";
    private static final Pattern CONJUNCTIONS = Pattern.compile(".*(AND).*|.*(OR).*|.*(NOT).*");

    /**
     * This method will return suggestions based on the closest match of the word in the index. The index can be a word list, which is probably the best choice,
     * but doesn't have to be. If there are three words the, there will be suggestions for each word, and combinations of those suggestions, sorted by the score
     * for the words.
     * <p/>
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
    @SuppressWarnings("unused")
    public Response auto(//
                         @Context final HttpServletRequest request, //
                         @Context final UriInfo uriInfo) {
        final Search search = unmarshall(Search.class, request);
        final ArrayList<HashMap<String, String>> autoResults = new ArrayList<>();
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                List<String> searchStrings = search.getSearchStrings();
                for (final String searchString : searchStrings) {
                    StringBuilder[] autocompleteSuggestions = suggestions(searchString, search);
                    for (final StringBuilder autocompleteSuggestion : autocompleteSuggestions) {
                        HashMap<String, String> result = new HashMap<>();
                        result.put(IConstants.FRAGMENT, autocompleteSuggestion.toString().trim());
                        autoResults.add(result);
                    }
                }
            }
        });
        // Add the statistics
        HashMap<String, String> statistics = new HashMap<>();
        statistics.put(IConstants.TOTAL, Long.toString(autoResults.size()));
        statistics.put(IConstants.DURATION, Double.toString(duration));
        statistics.put(IConstants.SEARCH_STRINGS, search.getSearchStrings().toString());
        autoResults.add(statistics);
        search.setSearchResults(autoResults);
        return buildJsonResponse(search);
    }

    /**
     * For search string 'hello AND world' we expect multiple results, in the form of.
     * <pre>
     *     [hello] [AND] [world]
     *     [helloed] [AND] [worldly]
     *     [helloes] [AND] [worldliness]
     * </pre>
     *
     * @param searchString the string to create a suggestion list for
     * @param search       the search object for adding the results to
     * @return the array of strings that are a concatenation of the suggestions for each word
     */
    StringBuilder[] suggestions(final String searchString, final Search search) {
        String[] words = StringUtils.split(searchString, ' ');
        // Search for all the words and get the results
        List<Search> searches = new ArrayList<>();
        int maxSuggestions = 0;
        for (final String word : words) {
            if (CONJUNCTIONS.matcher(word).matches()) {
                continue;
            }
            Search cloneSearch = (Search) SerializationUtilities.clone(search);
            cloneSearch.setSearchStrings(Arrays.asList(word));
            cloneSearch = searcherService.search(cloneSearch);
            searches.add(cloneSearch);
            ArrayList<HashMap<String, String>> results = cloneSearch.getSearchResults();
            HashMap<String, String> statistics = results.remove(results.size() - 1);
            int total = Integer.parseInt(statistics.get(IConstants.TOTAL));
            maxSuggestions = Math.max(maxSuggestions, total);
        }
        maxSuggestions = Math.min(maxSuggestions, search.getMaxResults());
        // Iterate over the words and if the results for the words run out then use the word it's self
        StringBuilder[] autocompleteSuggestions = new StringBuilder[maxSuggestions];
        for (int i = 0; i < autocompleteSuggestions.length; i++) {
            autocompleteSuggestions[i] = new StringBuilder();
            for (int j = 0, k = 0; j < words.length; j++) {
                String word = words[j];
                if (CONJUNCTIONS.matcher(word).matches()) {
                    autocompleteSuggestions[i].append(' ');
                    autocompleteSuggestions[i].append(word);
                    autocompleteSuggestions[i].append(' ');
                    continue;
                }
                String suggestion;
                Search cloneSearch = searches.get(k);
                ArrayList<HashMap<String, String>> results = cloneSearch.getSearchResults();
                if (results.size() > i) {
                    suggestion = results.get(i).get(IConstants.FRAGMENT);
                } else {
                    suggestion = "<b>" + word + "</b>";
                }
                autocompleteSuggestions[i].append(suggestion);
                k++;
            }
        }
        return autocompleteSuggestions;
    }

    /**
     * TODO Implement this method, with the top three results based on the words, as in the {@link Auto#auto(HttpServletRequest, UriInfo)} method, then the next
     * three based on a thesaurus perhaps, i.e. similar words to the search phrase, could be anything. And finally suggestions based on similar searches, that
     * will have to be classified with a k-means or similar algorithm.
     *
     * @param request the request from the gui
     * @param uriInfo the uri info if necessary
     * @return the suggestions based on the thesaurus of words for the language
     */
    @POST
    @Path(Auto.SUGGEST)
    @SuppressWarnings("unused")
    public Response suggestions(//
                                @Context final HttpServletRequest request, //
                                @Context final UriInfo uriInfo) {
        Search search = unmarshall(Search.class, request);
        Object results = searcherService.search(search);
        return buildJsonResponse(results);
    }

}