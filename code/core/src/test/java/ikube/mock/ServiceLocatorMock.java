package ikube.mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.IConstants;
import ikube.service.IMonitorWebService;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import mockit.Mock;
import mockit.MockClass;

import org.apache.log4j.Logger;

@MockClass(realClass = ServiceLocator.class)
public class ServiceLocatorMock {

	private static final Logger LOGGER = Logger.getLogger(ServiceLocatorMock.class);

	private static final IMonitorWebService MONITOR_WEB_SERVICE = mock(IMonitorWebService.class);
	private static final ISearcherWebService SEARCHER_WEB_SERVICE = mock(ISearcherWebService.class);

	static {
		when(MONITOR_WEB_SERVICE.getIndexFieldNames(any(String.class))).thenReturn(new String[] { "name", "latitude", "longitude" });
		File file = FileUtilities.findFileRecursively(new File("."), "default.results.xml");
		String xml = FileUtilities.getContents(file, IConstants.ENCODING);
		String[] searchStrings = new String[] { IConstants.IKUBE };
		@SuppressWarnings("unchecked")
		ArrayList<HashMap<String, String>> results = (ArrayList<HashMap<String, String>>) SerializationUtilities.deserialize(xml);
		when(SEARCHER_WEB_SERVICE.searchMultiAll(IConstants.IKUBE, searchStrings, true, 0, 10)).thenReturn(results);
	}

	@Mock()
	public static <T> T getService(final Class<T> klass, final String protocol, final String host, final int port, final String path,
			final String nameSpace, final String serviceName) {
		return mock(klass);
	}

	@Mock()
	@SuppressWarnings("unchecked")
	public static <T> T getService(final Class<T> klass, final String url, final String nameSpace, final String serviceName) {
		Field[] fields = ServiceLocatorMock.class.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(Boolean.TRUE);
			try {
				Object value = field.get(null);
				if (value != null && klass.isAssignableFrom(value.getClass())) {
					return (T) value;
				}
			} catch (Exception e) {
				LOGGER.error("Exception getting the object from the application context mock : " + klass, e);
			}
		}
		return mock(klass);
	}

}
