package ikube.data;

import ikube.database.jpa.ADataBaseJpa;
import ikube.database.jpa.DataBaseJpaH2;
import ikube.model.Persistable;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import mockit.Deencapsulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Couck
 * @since at least 23.11.2013
 * @version 01.00
 */
public final class DatabaseDelete {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseDelete.class);

	public static void main(final String[] args) {
		try {
			long batch = Long.parseLong(args[0]);
			ADataBaseJpa dataBaseJpa = getDataBase(DataBaseJpaH2.class, args[1]);
			Class<?> entityClass = Class.forName(args[2]);
			deleteEntities(dataBaseJpa, batch, entityClass);
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
	private static final void deleteEntities(final ADataBaseJpa dataBaseJpa, final long batch, final Class<?>... entityClasses) throws Exception {
		EntityManager entityManager = Deencapsulation.getField(dataBaseJpa, EntityManager.class);
		for (final Class<?> entityClass : entityClasses) {
			long count = dataBaseJpa.count(entityClass);
			if (count == 0) {
				return;
			}
			LOGGER.info("Deleteing : " + count);
			do {
				try {
					long firstResult = ((Persistable) dataBaseJpa.find(entityClass, 0, 1).get(0)).getId();
					long lastResult = firstResult + batch;
					entityManager.getTransaction().begin();
					StringBuilder builder = new StringBuilder("delete from ");
					builder.append(entityClass.getSimpleName());
					builder.append(" e where e.id >= :firstResult and e.id < :lastResult");
					String[] parameters = new String[] { "firstResult", "lastResult" };
					Object[] values = new Object[] { firstResult, lastResult };
					int result = dataBaseJpa.executeUpdate(builder.toString(), parameters, values);
					LOGGER.info("Result : " + result + ", count : " + count + ", firstResult : " + firstResult);

					count = count - result;
				} finally {
					entityManager.flush();
					entityManager.clear();
					entityManager.getTransaction().commit();
				}
			} while (count > 0);
		}
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
