package ikube.data;

import ikube.database.jpa.ADataBaseJpa;
import ikube.database.jpa.DataBaseJpaH2;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import mockit.Deencapsulation;

/**
 * Super class for database classes that need access to a {@link ADataBaseJpa}.
 * 
 * @author Michael Couck
 * @since at least 23.11.2013
 * @version 01.00
 */
public abstract class ADatabase {

	/**
	 * This method will instantiate the persistence units for the databases concerned.
	 * 
	 * @param persistenceUnits the name of the persistence units to use to instantiate the entity managers
	 * @return the list of {@link ADataBaseJpa} objects for the target databases
	 * @throws Exception
	 */
	static List<ADataBaseJpa> getDataBases(final String[] persistenceUnits) throws Exception {
		List<ADataBaseJpa> dataBases = new ArrayList<>();
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
	static ADataBaseJpa getDataBase(final Class<? extends ADataBaseJpa> type, final String persistenceUnit) throws Exception {
		EntityManager entityManager = Persistence.createEntityManagerFactory(persistenceUnit).createEntityManager();
		ADataBaseJpa aDataBaseJpa = type.newInstance();
		Deencapsulation.setField(aDataBaseJpa, entityManager);
		return aDataBaseJpa;
	}

}
