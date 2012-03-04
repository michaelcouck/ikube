package ikube.web.admin;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import ikube.service.ISearcherWebService;

import javax.servlet.http.HttpServletRequest;

import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.ModelAndView;

public class SearchBaseControllerTest {

	private HttpServletRequest httpServletRequest;
	private ISearcherWebService searcherWebservice;
	private SearchBaseController searchBaseController;

	@Before
	public void before() {
		searchBaseController = new SearchBaseController() {
		};
		httpServletRequest = mock(HttpServletRequest.class);
		searcherWebservice = mock(ISearcherWebService.class);
		Deencapsulation.setField(searchBaseController, searcherWebservice);
	}

	@Test
	public void doSearch() {
		ModelAndView modelAndView = new ModelAndView();
		String indexName = "indexName";
		String[] searchStrings = new String[] { "search strings", "search strings" };
		searchBaseController.doSearch(httpServletRequest, modelAndView, indexName, searchStrings);
		Mockito.verify(searcherWebservice, Mockito.atLeastOnce()).searchMultiAll(anyString(), any(String[].class), anyBoolean(), anyInt(),
				anyInt());
	}

}
