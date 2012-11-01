package ikube.service;

import ikube.ATest;
import ikube.database.IDataBase;
import ikube.model.Search;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AutoCompleteTest extends ATest {

	private String indexName = "indexName";
	private String[] searchStrings = { "search strings", "michael couck", "the quick brown fox jumped over the dog" };
	private String[] correctedSearchStrings = { "corrected search strings", "michael couch", "the quick brown fox jumped over the dog" };

	private IDataBase dataBase;
	private AutoComplete autoComplete;

	public AutoCompleteTest() {
		super(AutoCompleteTest.class);
	}

	@Before
	public void before() throws Exception {
		autoComplete = new AutoComplete();
		dataBase = Mockito.mock(IDataBase.class);

		List<Search> searches = new ArrayList<Search>();
		for (int i = 0; i < searchStrings.length; i++) {
			Search search = getSearch(indexName, searchStrings[i], correctedSearchStrings[i], 1.8526, 6, 246);
			searches.add(search);
		}

		Mockito.when(dataBase.find(Search.class, 0, 1000)).thenReturn(searches);
		Mockito.when(dataBase.find(Search.class, 1000, 2000)).thenReturn(new ArrayList<Search>());
		Deencapsulation.setField(autoComplete, dataBase);
		autoComplete.initialize();
	}

	@Test
	public void suggestions() throws Exception {
		for (int i = 0; i < searchStrings.length; i++) {
			String[] suggestions = autoComplete.suggestions(searchStrings[i]);
			logger.info("Suggestions : " + Arrays.deepToString(suggestions));
		}
		String[] suggestions = autoComplete.suggestions("sear");
		logger.info("Suggestions : " + Arrays.deepToString(suggestions));
	}

	private Search getSearch(final String indexName, final String searchStrings, final String correctedSearchStrings,
			final double highScore, final int results, final int count) {
		Search search = new Search();
		search.setSearchStrings(searchStrings);
		search.setCorrectedSearchStrings(correctedSearchStrings);

		search.setCount(count);
		search.setResults(results);
		search.setHighScore(highScore);
		search.setIndexName(indexName);
		search.setCorrections(Boolean.TRUE);
		search.setTimestamp(new Timestamp(System.currentTimeMillis()));
		return search;
	}

}
