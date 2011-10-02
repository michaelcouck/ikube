package ikube.cluster.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.infinispan.Cache;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

/**
 * @see ICache
 * @author Michael Couck
 * @since 01.10.11
 * @version 01.00
 */
public class CacheInfinispan implements ICache {

	private EmbeddedCacheManager	manager;

	public void initialise() throws Exception {
		GlobalConfiguration globalConfiguration = GlobalConfiguration.getClusteredDefault();
		// new GlobalConfiguration();
		Properties p = new Properties();
		p.setProperty("configurationFile", "jgroups-udp.xml");
		globalConfiguration.setTransportProperties(p);
		manager = new DefaultCacheManager(globalConfiguration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size(final String name) {
		return getMap(name).size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Object> T get(final String name, final Long id) {
		return (T) getMap(name).get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Object> void set(final String name, final Long id, final T object) {
		getMap(name).put(id, object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(final String name, final Long id) {
		getMap(name).remove(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Object> List<T> get(final String name, final ICriteria<T> criteria, final IAction<T> action, final int size) {
		List<T> result = new ArrayList<T>();
		Map<Long, Object> map = getMap(name);
		for (Map.Entry<Long, Object> mapEntry : map.entrySet()) {
			if (result.size() >= size) {
				break;
			}
			T t = (T) mapEntry.getValue();
			if (criteria == null) {
				result.add(t);
			} else {
				if (criteria.evaluate(t)) {
					result.add(t);
				}
			}
			if (action != null) {
				action.execute(t);
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear(final String name) {
		getMap(name).clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Object> T get(final String name, final String sql) {
		throw new RuntimeException("This operation is not really required : ");
	}

	public boolean lock(final String name) {
		return Boolean.TRUE;
	}

	public boolean unlock(String name) {
		return Boolean.TRUE;
	}

	private Map<Long, Object> getMap(String name) {
		Cache<Long, Object> cache = manager.getCache(name);
		return cache;
	}

}