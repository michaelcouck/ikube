package ikube.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import ikube.ATest;
import ikube.IConstants;
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

/**
 * @author Michael Couck
 * @since 29.10.12
 * @version 01.00
 */
public class AutoCompleteServiceTest extends ATest {

	private AutoCompleteService autoCompleteService;

	public AutoCompleteServiceTest() {
		super(AutoCompleteServiceTest.class);
	}

	@Before
	@SuppressWarnings("unchecked")
	public void before() throws Exception {
		autoCompleteService = new AutoCompleteService();
		File file = FileUtilities.findFileRecursively(new File("."), "autocomplete.results.xml");
		String contents = FileUtilities.getContents(file, IConstants.ENCODING);
		ArrayList<HashMap<String, String>> defaultResults = (ArrayList<HashMap<String, String>>) SerializationUtilities
				.deserialize(contents);
		ISearcherService searcherService = Mockito.mock(ISearcherService.class);
		Mockito.when(searcherService.searchSingle(anyString(), anyString(), anyString(), anyBoolean(), anyInt(), anyInt())).thenReturn(
				defaultResults);
		Deencapsulation.setField(autoCompleteService, searcherService);
	}

	@Test
	public void suggestions() throws Exception {
		String[] suggestions = autoCompleteService.suggestions("ikube");
		String suggestionsString = Arrays.deepToString(suggestions);
		logger.info("Suggestions : " + suggestionsString);
		assertTrue("All the suggestions from the results file should be in the suggestions : ",
				suggestionsString.contains("search string, something else altogether, the quick brown fox and the lazy dog"));
	}

}
