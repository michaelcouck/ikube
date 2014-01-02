package ikube.web.admin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.toolkit.Logging;
import ikube.web.toolkit.MockFactory.ApplicationContextManagerMock;

import javax.servlet.http.HttpServletRequest;

import mockit.Mocked;
import mockit.Mockit;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BaseControllerTest {

	private Logger logger = Logger.getLogger(BaseControllerTest.class);

	static {
		Logging.configure();
	}

	@Mocked(inverse = true, methods = { "getViewUri" })
	private BaseController baseController;
	private HttpServletRequest request = mock(HttpServletRequest.class);

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ApplicationContextManagerMock.class);
	}

	@Test
	public void getViewUri() {
		when(request.getRequestURI()).thenReturn("/ikube/admin/search.html");
		when(request.getContextPath()).thenReturn("/ikube");
		String viewUri = baseController.getViewUri(request);
		logger.info("View uri : " + viewUri);
		assertEquals("The context and the suffix should be removed for the view : ", "/admin/search", viewUri);
	}

}
