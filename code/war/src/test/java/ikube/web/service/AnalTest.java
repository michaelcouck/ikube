package ikube.web.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.model.Search;
import ikube.search.ISearcherService;
import ikube.web.service.Anal.TwitterSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import mockit.Deencapsulation;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Michael couck
 * @since 17.12.13
 * @version 01.00
 */
public class AnalTest extends BaseTest {

	/** Class under test */
	private Anal anal;
	private Gson gson;
	private ISearcherService searcherService;

	@Before
	@SuppressWarnings("unchecked")
	public void before() throws Exception {
		gson = new GsonBuilder().disableHtmlEscaping().create();

		HashMap<String, String> statistics = new HashMap<>();
		statistics.put(IConstants.TOTAL, "100");
		ArrayList<HashMap<String, String>> searchResults = new ArrayList<>();

		TwitterSearch search = new TwitterSearch();
		search.setSearchStrings(Arrays.asList("1386765181186-1387369981186"));
		search.setSearchFields(Arrays.asList(Anal.CREATED_AT));
		searchResults.add(statistics);
		search.setSearchResults(searchResults);

		anal = mock(Anal.class);
		searcherService = mock(ISearcherService.class);

		when(anal.buildResponse()).thenCallRealMethod();
		when(anal.invertMatrix(any(Object[][].class))).thenCallRealMethod();
		when(anal.buildJsonResponse(any(Object.class))).thenCallRealMethod();
		when(anal.twitter(any(HttpServletRequest.class), any(UriInfo.class))).thenCallRealMethod();
		when(anal.unmarshall(any(Class.class), any(HttpServletRequest.class))).thenReturn(search);
		when(searcherService.search(any(Search.class))).thenReturn(search);

		Deencapsulation.setField(anal, gson);
		Deencapsulation.setField(anal, logger);
		Deencapsulation.setField(anal, searcherService);
	}

	@Test
	public void analyze() {
		Response response = anal.twitter((HttpServletRequest) null, (UriInfo) null);
		String string = (String) response.getEntity();
		logger.info("Results : " + string);
		TwitterSearch twitterSearch = (TwitterSearch) gson.fromJson(string, TwitterSearch.class);
		logger.info("Twitter results : " + ToStringBuilder.reflectionToString(twitterSearch));
		// Mockito.verify(anal, Mockito.atLeastOnce()).buildJsonResponse(any());
	}

}