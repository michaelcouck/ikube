package ikube.cluster.hzc;

import com.hazelcast.core.MapStore;
import com.hazelcast.spring.context.SpringAware;
import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ClusterManagerCacheSearch implements MapStore<Long, Search> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterManagerCacheSearch.class);

    @Autowired
    private IDataBase dataBase;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void store(final Long hash, final Search search) {
        if (search.getId() >= 0) {
            dataBase.merge(search);
        } else {
            dataBase.persist(search);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeAll(final Map<Long, Search> searches) {
        for (final Map.Entry<Long, Search> mapEntry : searches.entrySet()) {
            store(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final Long hash) {
        Search search = dataBase.find(Search.class, new String[]{IConstants.HASH}, new Object[]{hash});
        if (search != null) {
            dataBase.remove(search);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll(final Collection<Long> hashes) {
        for (final Long hash : hashes) {
            delete(hash);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Search load(final Long hash) {
        return dataBase.find(Search.class, new String[]{IConstants.HASH}, new Object[]{hash});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Long, Search> loadAll(final Collection<Long> hashes) {
        Map<Long, Search> searches = new HashMap<>();
        for (final Long hash : hashes) {
            Search search = load(hash);
            if (search != null) {
                searches.put(hash, search);
            }
        }
        return searches;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Long> loadAllKeys() {
        Set<Long> hashes = new TreeSet<>();
        List<Search> searches = dataBase.find(Search.class, 0, 10000);
        for (final Search search : searches) {
            hashes.add(search.getHash());
        }
        return hashes;
    }

}