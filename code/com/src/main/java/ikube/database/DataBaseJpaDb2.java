package ikube.database;

import ikube.Constants;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.transaction.Transactional;

/**
 * This is a convenience class to switch from the H2 database to Db2 for testing purposes and benchmarking.
 *
 * @author Michael Couck
 * @version 01.00
 * @see IDataBase
 * @since 28-04-2010
 */
@Transactional
public class DataBaseJpaDb2 extends ADataBaseJpa {

    @PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = Constants.PERSISTENCE_UNIT_DB2)
    protected EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

}