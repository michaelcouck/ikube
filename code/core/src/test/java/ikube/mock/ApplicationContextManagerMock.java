package ikube.mock;

import ikube.cluster.IClusterManager;
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

	@Mock()
	@SuppressWarnings("unchecked")
	public static synchronized <T> T getBean(final Class<T> klass) {
		if (IClusterManager.class.isAssignableFrom(klass)) {
			return (T) CLUSTER_MANAGER;
		} else if (IndexContext.class.isAssignableFrom(klass)) {
			return (T) INDEX_CONTEXT;
		}
		return null;
	}

	@Mock()
	@SuppressWarnings("unchecked")
	public static synchronized <T> Map<String, T> getBeans(final Class<T> klass) {
		Map<String, T> beans = new HashMap<String, T>();
		beans.put(INDEX_CONTEXT.getIndexName(), (T) INDEX_CONTEXT);
		return beans;
	}
}
