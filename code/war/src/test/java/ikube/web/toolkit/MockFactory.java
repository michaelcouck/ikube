package ikube.web.toolkit;

import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.SERIALIZATION;
import mockit.Mock;
import mockit.MockClass;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class MockFactory {

    private static final Logger LOGGER = Logger.getLogger(MockFactory.class);

    private static final Map<Class<?>, Object> MOCKS = new HashMap<>();

    public static Object getMock(Class<?> klass) {
        if (MOCKS.get(klass) == null) {
            Object mock = mock(klass);
            MOCKS.put(klass, mock);
        }
        return MOCKS.get(klass);
    }

    public static void setMock(final Class<?> klass, final Object mock) {
        MOCKS.put(klass, mock);
    }

    public static void removeMock(final Class<?> klass) {
        MOCKS.remove(klass);
    }

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
            Map<String, T> beans = new HashMap<>();
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
            } catch (final ClassNotFoundException e) {
                LOGGER.error("Class not found : ", e);
            }
            return null;
        }
    }

    @MockClass(realClass = SERIALIZATION.class)
    public static class SerializationUtilitiesMock {
        @Mock
        @SuppressWarnings({"unchecked", "UnusedParameters"})
        public static <T> T clone(final Class<T> klass, T t) {
            return (T) getMock(klass);
        }
    }
}
