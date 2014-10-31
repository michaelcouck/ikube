package ikube.cluster.hzc;

import com.hazelcast.core.MapStore;
import ikube.model.Persistable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This is the implementation for Hazelcast to access the database on startup. We don't
 * populate the cache on startup, as we don't know where this instance will be in the life
 * cycle of the cluster.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-07-2012
 */
public class ClusterManagerCache implements MapStore<Integer, Persistable> {

    @Override
    public void store(final Integer key, final Persistable value) {
    }

    @Override
    public void storeAll(final Map<Integer, Persistable> map) {
    }

    @Override
    public void delete(final Integer key) {
    }

    @Override
    public void deleteAll(final Collection<Integer> keys) {
    }

    @Override
    public Persistable load(final Integer key) {
        return null;
    }

    @Override
    public Map<Integer, Persistable> loadAll(final Collection<Integer> keys) {
        return null;
    }

    @Override
    public Set<Integer> loadAllKeys() {
        return null;
    }
}