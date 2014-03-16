package ikube.mock;

import ikube.IConstants;
import ikube.action.index.handler.IIndexableHandler;
import ikube.action.index.handler.database.IndexableTableHandler;
import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.IndexableTable;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;
import mockit.Mock;
import mockit.MockClass;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        Logging.configure();
    }

    private static final Logger LOGGER = Logger.getLogger(ApplicationContextManagerMock.class);

    public static Object BEAN;
    public static IndexContext<?> INDEX_CONTEXT;
    public static Server SERVER = mock(Server.class);
    public static IDataBase DATABASE = mock(IDataBase.class);
    public static IClusterManager CLUSTER_MANAGER = mock(IClusterManager.class);
    public static IndexableTableHandler HANDLER = mock(IndexableTableHandler.class);
    public static IMonitorService MONITOR_WEB_SERVICE = mock(IMonitorService.class);

    private static final Map<Class, Object> BEANS = new HashMap<>();

    static {
        when(CLUSTER_MANAGER.getServer()).thenReturn(SERVER);
        when(HANDLER.getIndexableClass()).thenReturn(IndexableTable.class);
        when(MONITOR_WEB_SERVICE.getIndexNames()).thenReturn(new String[]{IConstants.IKUBE, IConstants.IKUBE, IConstants.IKUBE});
    }

    @Mock()
    @SuppressWarnings("unchecked")
    public static synchronized <T> T getBean(final Class<T> klass) {
        Field[] fields = ApplicationContextManagerMock.class.getDeclaredFields();
        for (final Field field : fields) {
            field.setAccessible(Boolean.TRUE);
            try {
                Object value = field.get(null);
                if (value != null && klass.isAssignableFrom(value.getClass())) {
                    return (T) value;
                }
            } catch (final Exception e) {
                LOGGER.error("Exception getting the object from the application context mock : " + klass, e);
            }
        }
        if (BEANS.get(klass) != null) {
            return (T) BEANS.get(klass);
        }
        T t = mock(klass);
        BEANS.put(klass, t);
        return t;
    }

    @Mock()
    @SuppressWarnings("unchecked")
    public static synchronized <T> Map<String, T> getBeans(final Class<T> klass) {
        Map<String, T> beans = new HashMap<>();
        if (IndexContext.class.isAssignableFrom(klass)) {
            beans.put(INDEX_CONTEXT.getIndexName(), (T) INDEX_CONTEXT);
        } else if (IIndexableHandler.class.isAssignableFrom(klass)) {
            beans.put(HANDLER.getClass().toString(), (T) HANDLER);
        } else {
            T t = getBean(klass);
            if (t != null) {
                beans.put(t.getClass().getSimpleName(), t);
            }
        }
        return beans;
    }

    @Mock
    @SuppressWarnings({"unchecked"})
    public static synchronized <T> T getBean(final String name) {
        return (T) BEAN;
    }

    public static <T> void setBean(final T bean) {
        ApplicationContextManagerMock.BEAN = bean;
    }

    public static void setClusterManager(final IClusterManager clusterManager) {
        ApplicationContextManagerMock.CLUSTER_MANAGER = clusterManager;
    }

    public static void setIndexContext(final IndexContext<?> indexContext) {
        ApplicationContextManagerMock.INDEX_CONTEXT = indexContext;
    }

    public static void setDataBase(final IDataBase dataBase) {
        ApplicationContextManagerMock.DATABASE = dataBase;
    }

    public static void main(final String[] args) {
        ApplicationContextManagerMock.getBean(IClusterManager.class);
    }
}
