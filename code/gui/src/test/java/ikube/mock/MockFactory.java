package ikube.mock;

import static org.mockito.Mockito.mock;
import ikube.toolkit.ApplicationContextManager;

import java.util.HashMap;
import java.util.Map;

import mockit.Mock;
import mockit.MockClass;

import org.apache.log4j.Logger;

public class MockFactory {

	private static final Logger LOGGER = Logger.getLogger(MockFactory.class);
	private static final Map<Class<?>, Object> MOCKS = new HashMap<Class<?>, Object>();

	@MockClass(realClass = ApplicationContextManager.class)
	public static class ApplicationContextManagerMock {

		@Mock()
		@SuppressWarnings("unchecked")
		public static synchronized <T> T getBean(final Class<T> klass) {
			return (T) MockFactory.getMock(klass);
		}

		@Mock()
		@SuppressWarnings("unchecked")
		public static synchronized <T> Map<String, T> getBeans(final Class<T> klass) {
			Map<String, T> beans = new HashMap<String, T>();
			Object object = MockFactory.getMock(klass);
			beans.put(object.getClass().getSimpleName(), (T) object);
			return beans;
		}

		@Mock
		@SuppressWarnings("unchecked")
		public static synchronized <T> T getBean(final String name) {
			try {
				Class<?> klass = Class.forName(name);
				return (T) MockFactory.getMock(klass);
			} catch (ClassNotFoundException e) {
				LOGGER.error("Class not found : ", e);
			}
			return null;
		}
	}

	public static Object getMock(Class<?> klass) {
		if (MOCKS.get(klass) == null) {
			Object mock = mock(klass);
			MOCKS.put(klass, mock);
		}
		return MOCKS.get(klass);
	}

	public static void removeMock(Class<?> klass) {
		MOCKS.remove(klass);
	}
}
