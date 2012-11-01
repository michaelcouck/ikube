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

public class AutoCompleteTest extends ATest {

	private AutoComplete autoComplete;

	public AutoCompleteTest() {
		super(AutoCompleteTest.class);
	}

	@Before
	@SuppressWarnings("unchecked")
	public void before() throws Exception {
		autoComplete = new AutoComplete();
		File file = FileUtilities.findFileRecursively(new File("."), "autocomplete.results.xml");
		String contents = FileUtilities.getContents(file, IConstants.ENCODING);
		ArrayList<HashMap<String, String>> defaultResults = (ArrayList<HashMap<String, String>>) SerializationUtilities
				.deserialize(contents);
		ISearcherService searcherService = Mockito.mock(ISearcherService.class);
		Mockito.when(searcherService.searchSingle(anyString(), anyString(), anyString(), anyBoolean(), anyInt(), anyInt())).thenReturn(
				defaultResults);
		Deencapsulation.setField(autoComplete, searcherService);
	}

	@Test
	public void suggestions() throws Exception {
		String[] suggestions = autoComplete.suggestions("sear");
		String suggestionsString = Arrays.deepToString(suggestions);
		logger.info("Suggestions : " + suggestionsString);
		assertTrue(
				"All the suggestions from the results file should be in the suggestions : ",
				suggestionsString
						.contains("to European company setup. Navigation Home Serenity <B>Ikube</B> Enterprise Search The ftp ant task Free ,  Serenity <B>Ikube</B> Enterprise Search "
								+ "The ftp ant task Free Interesting Java Links Hudson CI Spring ,  on a blog of some sort. Navigation Home Serenity <B>Ikube</B> Enterprise Search The ftp ant task Free"));
	}

}
