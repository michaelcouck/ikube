package ikube.cluster.hzc;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.hazelcast.core.MapLoader;

/**
 * This is a dummy implementation because Hazelcast expects it.
 * 
 * @author Michael Couck
 * @since 15.07.12
 * @version 01.00
 */
public class ClusterManagerCache implements MapLoader<String, Object> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object load(String key) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> loadAll(Collection<String> keys) {
		return Collections.EMPTY_MAP;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Set<String> loadAllKeys() {
		return Collections.EMPTY_SET;
	}

}