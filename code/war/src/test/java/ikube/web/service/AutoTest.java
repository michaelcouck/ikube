package ikube.web.service;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.model.Search;
import ikube.search.ISearcherService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AutoTest extends BaseTest {

	/** Class under test */
	private Auto auto;
	private Search search;
	private ISearcherService searcherService;

	@Before
	public void before() throws Exception {
		searcherService = mock(ISearcherService.class);
		search = new Search();
		search.setMaxResults(6);
		search.setSearchStrings(Arrays.asList("hello java world", "michael couck the omnipotent", "pearls before swine"));
		auto = new Auto() {
			@SuppressWarnings("unchecked")
			<T> T unmarshall(final Class<T> clazz, final HttpServletRequest request) {
				return (T) search;
			}
		};
		Deencapsulation.setField(auto, searcherService);
	}

	@Test
	public void auto() {
		Search search = getSearch("hello", "hellos", "helloed", "helloes", "hellova", "chello");
		Search[] searches = {//
		getSearch("java", "javan", "javas", "javali", "javari", "javary"), //
				getSearch("world", "worlds", "worldy", "worlded", "worldly", "worldful"), //
				getSearch("michael"), //
				getSearch("couch", "coucal", "coucha", "couchy", "couchä", "scouch"), //
				getSearch("the", "thea", "theb", "thed", "thee", "them"), //
				getSearch("omnipotent", "omnipotently", "omnipotents", "omnipotentces", "omnipotentcies"), //
				getSearch("pearls", "pearls", "pearlspar", "pearla", "pearle", "pearly"), //
				getSearch("before", "beforehand", "beforeness", "beforesaid", "beforested", "beforetime"), //
				getSearch("swine", "swines", "swiney", "swinely") //
		};
		when(searcherService.search(any(Search.class))).thenReturn(search, searches);

		Response response = auto.auto(null, null);
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String string = (String) response.getEntity();
		Search resultSearch = gson.fromJson(string, Search.class);
		ArrayList<HashMap<String, String>> results = resultSearch.getSearchResults();
		String[] fragments = {//
		"hello java world", //
				"hellos javan worlds", //
				"helloed javas worldy", //
				"helloes javali worlded", //
				"hellova javari worldly", //
				"chello javary worldful", //
				"michael couch the omnipotent", //
				"<b>michael</b> coucal thea omnipotently", //
				"<b>michael</b> coucha theb omnipotents", //
				"<b>michael</b> couchy thed omnipotentces", //
				"<b>michael</b> couchä thee omnipotentcies", //
				"<b>michael</b> scouch them <b>omnipotent</b>", //
				"pearls before swine", //
				"pearls beforehand swines", //
				"pearlspar beforeness swiney", //
				"pearla beforesaid swinely", //
				"pearle beforested <b>swine</b>", //
				"pearly beforetime <b>swine</b>" };
		for (int i = 0; i < results.size() && i < fragments.length; i++) {
			HashMap<String, String> result = results.get(i);
			logger.info("" + result);
			assertEquals(result.get(IConstants.FRAGMENT), fragments[i]);
		}
	}

	private Search getSearch(final String... fragments) {
		Search search = new Search();
		ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
		for (final String fragment : fragments) {
			results.add(getResult(fragment));
		}
		// Add one for the statistics
		results.add(new HashMap<String, String>());
		search.setSearchResults(results);
		return search;
	}

	private HashMap<String, String> getResult(final String fragment) {
		HashMap<String, String> result = new HashMap<String, String>();
		result.put(IConstants.FRAGMENT, fragment);
		return result;
	}

	@Test
	@Ignore
	public void suggestions() throws Exception {
		@SuppressWarnings("unused")
		Response suggestions = auto.suggestions(null, null);
	}

}