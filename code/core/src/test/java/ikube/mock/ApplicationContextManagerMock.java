package ikube.mock;

import static org.mockito.Mockito.*;

import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.index.handler.IHandler;
import ikube.index.handler.internet.IndexableInternetHandler;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import java.util.HashMap;
import java.util.Map;

import mockit.Mock;
import mockit.MockClass;

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

	public static IndexContext INDEX_CONTEXT;
	public static IClusterManager CLUSTER_MANAGER;
	public static IDataBase DATABASE = mock(IDataBase.class);
	public static IndexableInternetHandler HANDLER = mock(IndexableInternetHandler.class);

	@Mock()
	@SuppressWarnings("unchecked")
	public static synchronized <T> T getBean(final Class<T> klass) {
		if (IClusterManager.class.isAssignableFrom(klass)) {
			return (T) CLUSTER_MANAGER;
		} else if (IndexContext.class.isAssignableFrom(klass)) {
			return (T) INDEX_CONTEXT;
		} else if (IDataBase.class.isAssignableFrom(klass)) {
			return (T) DATABASE;
		}
		return null;
	}

	@Mock()
	@SuppressWarnings("unchecked")
	public static synchronized <T> Map<String, T> getBeans(final Class<T> klass) {
		Map<String, T> beans = new HashMap<String, T>();
		if (IndexContext.class.isAssignableFrom(klass)) {
			beans.put(INDEX_CONTEXT.getIndexName(), (T) INDEX_CONTEXT);
		} else if (IHandler.class.isAssignableFrom(klass)) {
			beans.put(HANDLER.getClass().toString(), (T) HANDLER);
		}
		return beans;
	}
}
