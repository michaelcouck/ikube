package ikube.web.servlet;

import static org.junit.Assert.assertTrue;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mockit.Cascading;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchServletTest {

	static {
		Logging.configure();
	}

	private SearchServlet searchServlet;
	@Cascading
	private HttpServletRequest request;
	@Cascading
	private HttpServletResponse response;

	@MockClass(realClass = ApplicationContextManager.class)
	public static class ApplicationContextManagerMock {
		@Mock()
		public static synchronized <T> Map<String, T> getBeans(Class<T> klass) {
			return new HashMap<String, T>();
		}
	}

	@Before
	public void before() {
		Mockit.setUpMock(ApplicationContextManager.class, ApplicationContextManagerMock.class);
		searchServlet = new SearchServlet();
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	@Ignore
	public void doGet() throws Exception {
		searchServlet.doGet(request, response);
		assertTrue(true);
	}

	@Test
	public void getParameters() {
		// TODO Implement me!
	}

	@Test
	public void getSearch() {
		// TODO Implement me!
	}

}
