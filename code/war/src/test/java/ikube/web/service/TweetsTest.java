package ikube.web.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Search;
import ikube.search.SearcherService;
import ikube.toolkit.ObjectToolkit;

/**
 * @author Michael couck
 * @version 01.00
 * @since 08-08-2014
 */
public class TweetsTest extends AbstractTest {

	@Spy
	@InjectMocks
	private Tweets tweets;
	@Mock
	private SearcherService searcherService;

	@Test
	public void search() {
		String total = "100";
		HashMap<String, String> statistics = new HashMap<>();
		statistics.put(IConstants.TOTAL, total);

		ArrayList<HashMap<String, String>> searchResults = new ArrayList<>();
		searchResults.add(statistics);

		Search search = ObjectToolkit.populateFields(new Search(), Boolean.TRUE, 3);
		search.setSearchResults(searchResults);
		int result = tweets.search(search, 0, 60, IConstants.POSITIVE);
		Assert.assertEquals(Integer.valueOf(total).intValue(), result);
	}

}
