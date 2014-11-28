package ikube.mock;

import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.LOGGING;
import mockit.Mock;
import mockit.MockClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * This mock is for the application context manager so we can return mocked index contexts and so
 * on rather than instantiating the Spring context along with all the associated items like the data
 * sources etc. which take a very long time to initialize.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 20-03-2011
 */
@MockClass(realClass = ApplicationContextManager.class)
public class ApplicationContextManagerMock {

    static {
        LOGGING.configure();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContextManagerMock.class);

    private static final Map<Class, Object> BEANS = new HashMap<>();

    @Mock()
    @SuppressWarnings("unchecked")
    public static synchronized <T> T getBean(final Class<T> klass) {
        if (BEANS.get(klass) == null) {
            T t = mock(klass);
            BEANS.put(klass, t);
        }
        return (T) BEANS.get(klass);
    }

    @Mock()
    @SuppressWarnings({"unchecked", "UnusedDeclaration"})
    public static synchronized <T> Map<String, T> getBeans(final Class<T> klass) {
        T t = getBean(klass);
        Map<String, T> beans = new HashMap<>();
        beans.put(t.getClass().getSimpleName(), t);
        return beans;
    }

    public static synchronized <T> void setBean(final Class<T> klass, final Object bean) {
        BEANS.put(klass, bean);
    }

    @Mock
    @SuppressWarnings({"unchecked"})
    public static synchronized <T> T getBean(final String name) {
        try {
            Class<?> klass = Class.forName(name);
            return (T) getBean(klass);
        } catch (final ClassNotFoundException e) {
            LOGGER.error(null, e);
        }
        return null;
    }

}
