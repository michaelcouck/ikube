package ikube.mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.service.IMonitorWebService;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import mockit.Mock;
import mockit.MockClass;

@MockClass(realClass = ServiceLocator.class)
public class ServiceLocatorMock {

	private static IMonitorWebService MONITOR_WEB_SERVICE;
	private static ISearcherWebService SEARCHER_WEB_SERVICE;

	@Mock()
	public static <T> T getService(final Class<T> klass, final String protocol, final String host, final int port, final String path,
			final String nameSpace, final String serviceName) {
		return mock(klass);
	}

	@Mock()
	@SuppressWarnings("unchecked")
	public static <T> T getService(final Class<T> klass, final String url, final String nameSpace, final String serviceName) {
		if (ISearcherWebService.class.isAssignableFrom(klass)) {
			if (SEARCHER_WEB_SERVICE == null) {
				SEARCHER_WEB_SERVICE = (ISearcherWebService) mock(klass);
			}
			return (T) SEARCHER_WEB_SERVICE;
		} else if (IMonitorWebService.class.isAssignableFrom(klass)) {
			if (MONITOR_WEB_SERVICE == null) {
				MONITOR_WEB_SERVICE = (IMonitorWebService) mock(klass);
				// TODO Move this to the class that needs this return
				when(MONITOR_WEB_SERVICE.getIndexFieldNames(any(String.class)))
						.thenReturn(new String[] { "name", "latitude", "longitude" });
			}
			return (T) MONITOR_WEB_SERVICE;
		}
		return mock(klass);
	}

}
