package ikube.database;

import ikube.Constants;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

/**
 * @author Michael Couck
 * @version 01.00
 * @see IDataBase
 * @since 19-11-2011
 */
public class DataBaseJpaPostgres extends ADataBaseJpa {

    @PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = Constants.PERSISTENCE_UNIT_POSTGRES)
    protected EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

}