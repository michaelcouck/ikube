package ikube.cluster.hzc;

import com.hazelcast.core.MapStore;
import com.hazelcast.spring.context.SpringAware;
import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.SavePoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-07-2014
 */
@Component
@SuppressWarnings("ALL")
@SpringAware(beanName = "ikube.cluster.hzc.ClusterManagerCacheSavePoint")
public class ClusterManagerCacheSavePoint implements MapStore<String, SavePoint> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterManagerCacheSavePoint.class);

    @Autowired
    private IDataBase dataBase;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void store(final String indexable, final SavePoint savePoint) {
        if (savePoint.getId() > 0) {
            try {
                SavePoint dbSavePoint = dataBase.find(SavePoint.class, new String[]{IConstants.INDEXABLE}, new Object[]{indexable});
                dbSavePoint.setIndexable(savePoint.getIndexable());
                dbSavePoint.setIdentifier(savePoint.getIdentifier());
                dbSavePoint.setIndexContext(savePoint.getIndexContext());
                dataBase.merge(savePoint);
            } catch (final Exception e) {
                LOGGER.error("Exception merging save point from cache : " + e.getMessage() + ", " + savePoint.getId());
                dataBase.remove(SavePoint.class, savePoint.getId());
            }
        } else {
            dataBase.persist(savePoint);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeAll(final Map<String, SavePoint> savePoints) {
        for (final Map.Entry<String, SavePoint> mapEntry : savePoints.entrySet()) {
            store(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final String indexable) {
        SavePoint savePoint = dataBase.find(SavePoint.class, new String[]{IConstants.INDEXABLE}, new Object[]{indexable});
        if (savePoint != null) {
            dataBase.remove(savePoint);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll(final Collection<String> indexables) {
        for (final String id : indexables) {
            delete(id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SavePoint load(final String indexable) {
        return dataBase.find(SavePoint.class, new String[]{IConstants.INDEXABLE}, new Object[]{indexable});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, SavePoint> loadAll(final Collection<String> indexables) {
        Map<String, SavePoint> savePoints = new HashMap<>();
        for (final String indexable : indexables) {
            LOGGER.warn("Loading save point : " + indexable);
            SavePoint savePoint = load(indexable);
            if (savePoint != null) {
                savePoints.put(indexable, savePoint);
            }
        }
        return savePoints;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> loadAllKeys() {
        Set<String> indexables = new TreeSet<>();
        List<SavePoint> savePoints = dataBase.find(SavePoint.class, 0, 10000);
        LOGGER.warn("Loading save points : " + savePoints.size());
        for (final SavePoint savePoint : savePoints) {
            indexables.add(savePoint.getIndexable());
        }
        return indexables;
    }

}