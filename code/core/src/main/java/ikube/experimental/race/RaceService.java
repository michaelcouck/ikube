package ikube.experimental.race;

import ikube.database.IDataBase;
import ikube.model.geospatial.GeoName;
import ikube.toolkit.THREAD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29-07-2015
 */
public class RaceService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired(required = true)
    @Qualifier("ikube.database.IDataBase")
    private IDataBase dataBase;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long persist() {
        GeoName geoName = new GeoName();
        dataBase.persist(geoName);
        List<GeoName> geoNames = dataBase.find(GeoName.class, 0, 100);
        geoName = geoNames.get(0);
        logger.error("Id : " + geoName.getId() + ", " + geoName.hashCode());
        return geoName.getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void update(final long id) {
        logger.error("Looking for : " + id);
        GeoName geoName = dataBase.find(GeoName.class, id);
        int population = geoName.getPopulation();
        logger.error("Geoname : " + geoName.getId() + ", " + geoName.getPopulation() + ", " + geoName.getVersion() + ", " + geoName.hashCode());

        // Increment the population in another transaction
        // intercept(geoName.getId());
        THREAD.sleep(1000);

        // Should fail with stale object exception because this gets the geoname from
        // the cache by id, which is the out of date geoname with the population still at 0
        GeoName updateGeoName = dataBase.find(GeoName.class, id);
        logger.error("Intercepted population : " + updateGeoName.getId() + ", " + updateGeoName.getPopulation() + ", " + updateGeoName.getVersion() + ", " + geoName.hashCode());
        updateGeoName.setPopulation(++population);
        dataBase.merge(updateGeoName);

        // Should have failed already
        updateGeoName = dataBase.find(GeoName.class, id);
        logger.error("Updated population : " + updateGeoName.getId() + ", " + updateGeoName.getPopulation() + ", " + updateGeoName.getVersion() + ", " + geoName.hashCode());
        if (updateGeoName.getPopulation() != population) {
            throw new RuntimeException("Population updated in separate thread : " +
                    geoName.getPopulation() + ", " + updateGeoName.getPopulation());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void intercept(final long id) {
        logger.error("Interceptor looking for : " + id);
        GeoName geoName = dataBase.find(GeoName.class, id);
        logger.error("Intercepting geoname : " + geoName.getId() + ", " + geoName.getPopulation() + ", " + geoName.getVersion() + ", " + geoName.hashCode());
        int population = geoName.getPopulation();
        geoName.setPopulation(population + 100);
        dataBase.merge(geoName);
        geoName = dataBase.find(GeoName.class, id);
        logger.error("Intercepted geoname : " + geoName.getId() + ", " + geoName.getPopulation() + ", " + geoName.getVersion() + ", " + geoName.hashCode());
    }

    public IDataBase getDataBase() {
        return dataBase;
    }

    public void setDataBase(final IDataBase dataBase) {
        this.dataBase = dataBase;
    }

}