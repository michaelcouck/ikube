package ikube.data;

import ikube.database.ADataBaseJpa;
import ikube.database.DataBaseJpaH2;
import ikube.model.geospatial.GeoName;
import mockit.Deencapsulation;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class will copy tables from one database to other databases, possible of another type using Jpa.<br>
 * <p/>
 * Logic is as follows:
 * <p/>
 * <pre>
 * 		* Create the entity manager for the source database
 * 		* Create the entity managers for the target databases
 * 		* Select a batch from the source table
 * 		* Clone the entities and remove the identifier
 * 		* Batch the clones and persist as a batch in the target
 * 		* Repeat until no entities left int he source
 * </pre>
 *
 * @author Michael Couck
 * @version 01.00
 * @since at least 18-11-2013
 */
public final class DatabaseCopy extends ADatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCopy.class);

    /**
     * This method sets up the source entity manager and target entity managers and iterates over the collections
     * that are selected from the source.
     * <p/>
     * Usage:
     * <p/>
     * <pre>
     * 	  java -jar ikube.jar ikube.data.DatabaseCopy 0 10000 IkubePersistenceUnitPostgres IkubePersistenceUnitOracle IkubePersistenceUnitDb2 ...
     * </pre>
     * <p/>
     * => java -jar [ikube-tool.jar] [first identifier] [maximum records] [source persistence unit] [target persistence units...]
     *
     * @param args the first and last record id
     */
    public static void main(final String[] args) {
        try {
            int counter = 0;
            int firstResult = Integer.parseInt(args[0]);
            int maxResults = Integer.parseInt(args[1]);

            ADataBaseJpa aDataBaseJpa = getDataBase(DataBaseJpaH2.class, args[2]);
            String[] targetPersistenceUnits = new String[args.length - 3];
            System.arraycopy(args, 3, targetPersistenceUnits, 0, targetPersistenceUnits.length);

            List<ADataBaseJpa> dataBases = getDataBases(targetPersistenceUnits);
            Collection<GeoName> geoNames = aDataBaseJpa.find(GeoName.class, firstResult, maxResults);
            do {
                for (final ADataBaseJpa dataBaseJpa : dataBases) {
                    copyEntities(dataBaseJpa, geoNames);
                }
                counter += maxResults;
                LOGGER.info("Counter : " + counter);
                firstResult += maxResults;
                geoNames = aDataBaseJpa.find(GeoName.class, firstResult, maxResults);
            } while (geoNames != null && geoNames.size() > 0);
        } catch (Exception e) {
            LOGGER.error(null, e);
        }
    }

    /**
     * This method clones the source entities, removes the identifier, batches them and persists them in the target database.
     *
     * @param dataBaseJpa the database(dao) helper class for the entity manager
     * @param geoNames    the source entities to persist in the target database
     * @throws Exception
     */
    private static void copyEntities(final ADataBaseJpa dataBaseJpa, final Collection<GeoName> geoNames) throws Exception {
        EntityManager entityManager = Deencapsulation.getField(dataBaseJpa, EntityManager.class);
        entityManager.getTransaction().begin();
        List<Object> batch = new ArrayList<Object>();
        for (final GeoName geoName : geoNames) {
            GeoName geoNameCopy = new GeoName();
            BeanUtilsBean.getInstance().copyProperties(geoNameCopy, geoName);
            geoNameCopy.setId(0);
            batch.add(geoNameCopy);
        }
        dataBaseJpa.persistBatch(batch);
        entityManager.flush();
        entityManager.clear();
        entityManager.getTransaction().commit();
    }

}
