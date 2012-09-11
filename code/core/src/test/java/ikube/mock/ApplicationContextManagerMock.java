package ikube.mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.index.handler.IHandler;
import ikube.index.handler.database.IndexableTableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableTable;
import ikube.model.Server;
import ikube.service.IMonitorService;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import mockit.Mock;
import mockit.MockClass;

import org.apache.log4j.Logger;

/**
 * This mock is for the application context manager so we can return mocked index contexts and so on rather than instantiating the Spring
 * context along with all the associated items like the data sources etc. which take a very long time to initialize.
 * 
 * @author Michael Couck
 * @since 20.03.11
 * @version 01.00
 */
@MockClass(realClass = ApplicationContextManager.class)
public class ApplicationContextManagerMock {

	static {
		Logging.configure();
	}

	private static final Logger LOGGER = Logger.getLogger(ApplicationContextManagerMock.class);

	public static Object BEAN;
	public static IndexContext<?> INDEX_CONTEXT;
	public static IClusterManager CLUSTER_MANAGER = mock(IClusterManager.class);
	public static IDataBase DATABASE = mock(IDataBase.class);
	public static IndexableTableHandler HANDLER = mock(IndexableTableHandler.class);
	public static Server SERVER = mock(Server.class);
	public static IMonitorService MONITOR_WEB_SERVICE = mock(IMonitorService.class);

	static {
		when(CLUSTER_MANAGER.getServer()).thenReturn(SERVER);
		when(HANDLER.getIndexableClass()).thenReturn(IndexableTable.class);
		when(MONITOR_WEB_SERVICE.getIndexNames()).thenReturn(new String[] { IConstants.IKUBE, IConstants.IKUBE, IConstants.IKUBE });
	}

	@Mock()
	@SuppressWarnings("unchecked")
	public static synchronized <T> T getBean(final Class<T> klass) {
		Field[] fields = ApplicationContextManagerMock.class.getDeclaredFields();
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

	@Mock()
	@SuppressWarnings("unchecked")
	public static synchronized <T> Map<String, T> getBeans(final Class<T> klass) {
		Map<String, T> beans = new HashMap<String, T>();
		if (IndexContext.class.isAssignableFrom(klass)) {
			beans.put(INDEX_CONTEXT.getIndexName(), (T) INDEX_CONTEXT);
		} else if (IHandler.class.isAssignableFrom(klass)) {
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
	@SuppressWarnings({ "unchecked" })
	public static synchronized <T> T getBean(final String name) {
		return (T) BEAN;
	}

	public static <T> void setBean(T bean) {
		ApplicationContextManagerMock.BEAN = bean;
	}

	public static void setClusterManager(IClusterManager clusterManager) {
		ApplicationContextManagerMock.CLUSTER_MANAGER = clusterManager;
	}

	public static void setIndexContext(IndexContext<?> indexContext) {
		ApplicationContextManagerMock.INDEX_CONTEXT = indexContext;
	}

	public static void setDataBase(IDataBase dataBase) {
		ApplicationContextManagerMock.DATABASE = dataBase;
	}

	public static void main(String[] args) {
		ApplicationContextManagerMock.getBean(IClusterManager.class);
	}
}
