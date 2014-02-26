package ikube.cluster.hzc;

import com.hazelcast.core.MapStore;
import com.hazelcast.spring.context.SpringAware;
import ikube.database.IDataBase;
import ikube.model.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * This is the implementation for Hazelcast to access the database on startup. We don't
 * populate the cache on startup, as we don't know where this instance will be in the life
 * cycle of the cluster.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-07-2012
 */
@Component
@SuppressWarnings("ALL")
@SpringAware(beanName = "ikube.cluster.hzc.ClusterManagerCacheSearch")
public class ClusterManagerCacheSearch implements MapStore<Integer, Search> {

    @Autowired
    private IDataBase dataBase;

    @Override
    public void store(final Integer hash, final Search search) {
        search.setHash(hash);
        if (search.getId() > 0) {
            dataBase.merge(search);
        } else {
            dataBase.persist(search);
        }
    }

    @Override
    public void storeAll(final Map<Integer, Search> searches) {
        for (final Map.Entry<Integer, Search> mapEntry : searches.entrySet()) {
            store(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    @Override
    public void delete(final Integer hash) {
        Search search = dataBase.find(Search.class, new String[]{"hash"}, new Object[]{hash});
        if (search != null) {
            dataBase.remove(search);
        }
    }

    @Override
    public void deleteAll(final Collection<Integer> hashes) {
        for (final Integer hash : hashes) {
            delete(hash);
        }
    }

    @Override
    public Search load(final Integer hash) {
        return dataBase.find(Search.class, new String[]{"hash"}, new Object[]{hash});
    }

    @Override
    public Map<Integer, Search> loadAll(final Collection<Integer> hashes) {
        Map<Integer, Search> searches = new HashMap<>();
        for (final Integer hash : hashes) {
            Search search = load(hash);
            if (search != null) {
                searches.put(hash, search);
            }
        }
        return searches;
    }

    @Override
    public Set<Integer> loadAllKeys() {
        Set<Integer> hashes = new TreeSet<>();
        List<Search> searches = dataBase.find(Search.class, 0, 1000);
        for (final Search search : searches) {
            hashes.add((int) search.getHash());
        }
        return hashes;
    }

}