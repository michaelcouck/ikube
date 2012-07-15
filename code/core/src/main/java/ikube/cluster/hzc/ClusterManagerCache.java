package ikube.cluster.hzc;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.hazelcast.core.MapLoader;

public class ClusterManagerCache implements MapLoader<String, Object> {

	@Override
	public Object load(String key) {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> loadAll(Collection<String> keys) {
		return Collections.EMPTY_MAP;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> loadAllKeys() {
		return Collections.EMPTY_SET;
	}

}