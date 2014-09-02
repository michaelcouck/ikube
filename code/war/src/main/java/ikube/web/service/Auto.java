package ikube.web.service;

import ikube.IConstants;
import ikube.model.Search;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.Timer;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Michael couck
 * @version 01.00
 * @since 01-03-2012
 */
@Component
@Path(Auto.AUTO)
@Scope(Auto.REQUEST)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(description = "The autocomplete rest service, provides word lists based on " +
        "partial character input, including multiple words")
public class Auto extends Resource {

    /**
     * Constants for the paths to the web services.
     */
    public static final String AUTO = "/auto";
    public static final String SUGGEST = "/suggest";
    private static final Pattern CONJUNCTIONS = Pattern.compile("AND|OR|NOT|and|or|not");

    /**
     * <p/>
     * This method will return suggestions based on the closest match of the word in the index. The index can be a word list,
     * which is probably the best choice, but doesn't have to be. If there are three words the, there will be suggestions for
     * each word, and combinations of those suggestions, sorted by the score for the words.
     * <p/>
     * <pre>
     * 		Input: [disaster on island honshu again]
     * 		Output: [disease in islanddia honalulu again], [dissapation ...] // the number of results expected, typically 6 or 8
     * </pre>
     *
     * @return the search result, or all the suggested strings in the {@link Search} object as search results, and the suggestions
     * in the fragments
     */
    @POST
    @SuppressWarnings("unused")
    @Api(description = "This method will query the autocomplete index, which is an index of words, English, and potentially " +
            "other languages, and return a list of best matches for the word. The autocomplete index is an " +
            "n-grammed index, allowing for fuzzy matching.",
            produces = Search.class)
    public Response auto(@RequestBody(required = true) final Search search) {
        final ArrayList<HashMap<String, String>> autoResults = new ArrayList<>();
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                List<String> searchStrings = search.getSearchStrings();
                for (final String searchString : searchStrings) {
                    String[] autocompleteSuggestions = suggestions(searchString, search);
                    for (final String autocompleteSuggestion : autocompleteSuggestions) {
                        HashMap<String, String> result = new HashMap<>();
                        result.put(IConstants.FRAGMENT, autocompleteSuggestion);
                        autoResults.add(result);
                    }
                }
                search.setSearchStrings(searchStrings);
            }
        });
        // Add the statistics
        HashMap<String, String> statistics = new HashMap<>();
        statistics.put(IConstants.TOTAL, Long.toString(autoResults.size()));
        statistics.put(IConstants.DURATION, Double.toString(duration));
        statistics.put(IConstants.SEARCH_STRINGS, search.getSearchStrings().toString());
        autoResults.add(statistics);
        search.setSearchResults(autoResults);
        return buildResponse(search);
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
    @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
    String[] suggestions(final String searchString, final Search search) {
        Search clone = (Search) SerializationUtilities.clone(search);
        int rows = clone.getMaxResults();
        String[] words = StringUtils.split(searchString, ' ');
        String[][] matrix = new String[rows][words.length];
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (CONJUNCTIONS.matcher(word).matches()) {
                for (int j = 0; j < rows; j++) {
                    setWord(word, j, i, matrix);
                }
            } else {
                clone.setSearchStrings(Arrays.asList(word));
                clone = searcherService.search(clone);
                ArrayList<HashMap<String, String>> results = clone.getSearchResults();
                Map<String, String> statistics = results.remove(clone.getSearchResults().size() - 1);
                int total = Integer.parseInt(statistics.get(IConstants.TOTAL));
                // The j < total is redundant
                for (int j = 0; j < rows && j < results.size() && j < total; j++) {
                    Map<String, String> result = results.get(j);
                    String similar = "<b>" + result.get(IConstants.WORD) + "</b>";
                    setWord(similar, j, i, matrix);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Matrix : " + Arrays.deepToString(matrix));
        }
        // Concatenate all the rows into strings
        List<String> suggestions = new ArrayList<>();
        skipRow:
        for (final String[] row : matrix) {
            StringBuilder builder = new StringBuilder();
            for (final String column : row) {
                // If any of the suggested words are null, which could be
                // because of any number of reasons, we skip this row completely
                if (column == null) {
                    continue skipRow;
                }
                builder.append(column);
                builder.append(' ');
            }
            String suggestion = builder.toString().trim();
            if (!suggestions.contains(suggestion)) {
                suggestions.add(suggestion);
            }
        }
        return suggestions.toArray(new String[suggestions.size()]);
    }

    private void setWord(final String word, final int i, final int j, final String[][] matrix) {
        matrix[i][j] = word;
    }

    /**
     * TODO Implement this method, with the top three results based on the words, as in the
     * similar words to the search phrase, could be anything. And finally suggestions based on similar searches, that
     * will have to be classified with a k-means or similar algorithm.
     *
     * @param search the request from the gui
     * @return the suggestions based on the thesaurus of words for the language
     */
    @POST
    @Path(Auto.SUGGEST)
    @SuppressWarnings("unused")
    public Response suggestions(final Search search) {
        Object results = searcherService.search(search);
        return buildResponse(results);
    }

}