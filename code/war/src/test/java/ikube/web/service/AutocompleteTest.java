package ikube.web.service;

import static org.junit.Assert.*;

import ikube.database.jpa.ADataBaseJpa;

import ikube.database.jpa.DataBaseJpa;
import ikube.model.Search;

import java.util.Arrays;
import java.util.List;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutocompleteTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutocompleteTest.class);
	private static String SEARCH_STRINGS = "something interesting";

	/** Class under test. */
	private Autocomplete autocomplete;

	@MockClass(realClass = ADataBaseJpa.class)
	public static class IDataBaseMock {
		@Mock()
		@SuppressWarnings("unchecked")
		public <T> List<T> find(Class<T> klass, String sql, String[] names, Object[] values, int startPosition, int maxResults) {
			Search search = new Search();
			search.setCount(10);
			search.setSearchStrings(SEARCH_STRINGS);
			return (List<T>) Arrays.asList(search);
		}
	}

	@Before
	public void before() {
		Mockit.setUpMocks(IDataBaseMock.class);
		autocomplete = new Autocomplete();
		Deencapsulation.setField(autocomplete, new DataBaseJpa());
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void autocomplete() {
		String expectedResult = "[{\"count\":10,\"results\":0,\"searchStrings\":\"something interesting\",\"highScore\":0.0,\"corrections\":false,\"id\":0}]";
		String result = autocomplete.autocomplete(SEARCH_STRINGS);
		LOGGER.info("Result : " + result);
		assertEquals("This should be a serialized version of the search object : ", expectedResult, result);
	}
	

}