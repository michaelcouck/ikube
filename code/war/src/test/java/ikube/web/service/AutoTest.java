package ikube.web.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.search.ISearcherService;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AutoTest extends BaseTest {

	private Auto auto;

	@Before
	@SuppressWarnings("unchecked")
	public void before() throws Exception {
		auto = new Auto();
		File file = FileUtilities.findFileRecursively(new File("."), "autocomplete.results.xml");
		String contents = FileUtilities.getContents(file, IConstants.ENCODING);
		ArrayList<HashMap<String, String>> defaultResults = (ArrayList<HashMap<String, String>>) SerializationUtilities
				.deserialize(contents);
		ISearcherService searcherService = Mockito.mock(ISearcherService.class);
		Mockito.when(searcherService.searchSingle(anyString(), anyString(), anyString(), anyBoolean(), anyInt(), anyInt())).thenReturn(
				defaultResults);
		Deencapsulation.setField(auto, searcherService);
	}

	@Test
	public void suggestions() throws Exception {
		String[] suggestions = auto.suggestions("ikube");
		String suggestionsString = Arrays.deepToString(suggestions);
		logger.info("Suggestions : " + suggestionsString);
		assertTrue("All the suggestions from the results file should be in the suggestions : ",
				suggestionsString.contains("search string, something else altogether, the quick brown fox and the lazy dog"));
	}

}
