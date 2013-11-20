package ikube.data;

import ikube.database.jpa.ADataBaseJpa;
import ikube.database.jpa.DataBaseJpaH2;
import ikube.model.geospatial.GeoName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import mockit.Deencapsulation;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will copy tables from one database to other databases, possible of another type using Jpa.<br>
 * 
 * Logic is as follows:
 * 
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
 * @since at least 18.11.2013
 * @version 01.00
 */
public final class DatabaseCopy {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCopy.class);

	/**
	 * This method sets up the source entity manager and target entity managers and iterates over the collections that are selected from the source.
	 * 
	 * Usage:
	 * 
	 * <pre>
	 * 		java -jar ikube.jar ikube.data.DatabaseCopy 4290000 10000 IkubePersistenceUnitH2 IkubePersistenceUnitDb2 IkubePersistenceUnitPostgres IkubePersistenceUnitOracle...
	 * </pre>
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		try {

			int counter = 0;
			int firstResult = Integer.parseInt(args[0]);
			int maxResults = Integer.parseInt(args[1]);

			ADataBaseJpa aDataBaseJpa = getDataBase(DataBaseJpaH2.class, args[2]);
			String[] targetPersistenceUnits = new String[args.length - 3];
			System.arraycopy(args, 2, targetPersistenceUnits, 0, targetPersistenceUnits.length);
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
	 * @param geoNames the source entities to persist in the target database
	 * @throws Exception
	 */
	private static final void copyEntities(final ADataBaseJpa dataBaseJpa, final Collection<GeoName> geoNames) throws Exception {
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

	/**
	 * This method will instantiate the persistence units for the databases concerned.
	 * 
	 * @param types the types of {@link ADataBaseJpa} classes to instantiate and inject the entity manager into
	 * @param persistenceUnits the name of the persistence units to use to instantiate the entity managers
	 * @return the list of {@link ADataBaseJpa} objects for the target databases
	 * @throws Exception
	 */
	private static final List<ADataBaseJpa> getDataBases(final String[] persistenceUnits) throws Exception {
		List<ADataBaseJpa> dataBases = new ArrayList<ADataBaseJpa>();
		for (final String persistenceUnit : persistenceUnits) {
			ADataBaseJpa aDataBaseJpa = getDataBase(DataBaseJpaH2.class, persistenceUnit);
			dataBases.add(aDataBaseJpa);
		}
		return dataBases;
	}

	/**
	 * This method will instantiate a single database(dao, {@link ADataBaseJpa}) object and inject the entity manager into it.
	 * 
	 * @param type the type of database dao to instantiate
	 * @param persistenceUnit the persistence unit name, same as in the persistence.xml
	 * @return the database dao with the entity manager injected
	 * @throws Exception
	 */
	private static final ADataBaseJpa getDataBase(final Class<? extends ADataBaseJpa> type, final String persistenceUnit) throws Exception {
		EntityManager entityManager = Persistence.createEntityManagerFactory(persistenceUnit).createEntityManager();
		ADataBaseJpa aDataBaseJpa = type.newInstance();
		Deencapsulation.setField(aDataBaseJpa, entityManager);
		return aDataBaseJpa;
	}

}
