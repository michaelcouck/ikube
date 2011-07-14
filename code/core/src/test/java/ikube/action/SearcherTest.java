package ikube.action;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.cluster.ClusterManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.service.IMonitorWebService;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 17.04.11
 * @version 01.00
 */
public class SearcherTest extends ATest {

	@MockClass(realClass = ServiceLocator.class)
	public static class ServiceLocatorMock {

		@Mock()
		public static <T> T getService(final Class<T> klass, final String protocol, final String host, final int port, final String path,
				final String nameSpace, final String serviceName) {
			return mock(klass);
		}

		@Mock()
		@SuppressWarnings("unchecked")
		public static <T> T getService(final Class<T> klass, final String url, final String nameSpace, final String serviceName) {
			if (ISearcherWebService.class.isAssignableFrom(klass)) {
				return mock(klass);
			} else if (IMonitorWebService.class.isAssignableFrom(klass)) {
				IMonitorWebService monitorWebService = (IMonitorWebService) mock(klass);
				when(monitorWebService.getIndexFieldNames(any(String.class))).thenReturn(new String[] { "name", "latitude", "longitude" });
				return (T) monitorWebService;
			}
			return mock(klass);
		}

	}

	public SearcherTest() {
		super(SearcherTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(IndexManagerMock.class, ApplicationContextManagerMock.class, ClusterManagerMock.class, ServiceLocatorMock.class);
		when(INDEX.getIndexWriter()).thenReturn(INDEX_WRITER);
		when(CLUSTER_MANAGER.setWorking(anyString(), anyString(), anyString(), anyBoolean())).thenReturn(System.currentTimeMillis());
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ApplicationContextManager.class, ClusterManager.class);
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@Test
	public void validate() throws Exception {
		Searcher searcher = new Searcher();
		searcher.setEnd(10);
		searcher.setFragment(Boolean.TRUE);
		searcher.setIterations(10);
		searcher.setResultsSizeMinimum(0);
		searcher.setRuleExpression("");
		searcher.setRules(null);
		searcher.setSearchString("Hello World!");
		searcher.setStart(0);
		boolean result = searcher.execute(INDEX_CONTEXT);
		assertTrue(result);
	}
}