package ikube.web.service;

import static org.mockito.Matchers.any;
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
import java.util.HashMap;

import javax.ws.rs.core.Response;

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
		ArrayList<HashMap<String, String>> defaultResults = (ArrayList<HashMap<String, String>>) SerializationUtilities.deserialize(contents);
		ISearcherService searcherService = Mockito.mock(ISearcherService.class);
		Mockito.when(searcherService.search(anyString(), any(String[].class), any(String[].class), any(String[].class), anyBoolean(), anyInt(), anyInt()))
				.thenReturn(defaultResults);
		Deencapsulation.setField(auto, searcherService);
	}

	@Test
	public void suggestions() throws Exception {
		@SuppressWarnings("unused")
		Response suggestions = auto.suggestions(null, null);
	}

}
