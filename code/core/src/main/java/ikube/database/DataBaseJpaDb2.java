package ikube.database;

import ikube.IConstants;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

/**
 * This is a convenience class to switch from the H2 database to Db2 for testing purposes and benchmarking.
 *
 * @author Michael Couck
 * @version 01.00
 * @see IDataBase
 * @since 28-04-2010
 */
public class DataBaseJpaDb2 extends ADataBaseJpa {

	@PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = IConstants.PERSISTENCE_UNIT_DB2)
	protected EntityManager entityManager;

	@Override
	protected EntityManager getEntityManager() {
		return entityManager;
	}

}