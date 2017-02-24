package ikube.database;

import ikube.Constants;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.transaction.Transactional;

/**
 * @author Michael Couck
 * @version 01.00
 * @see IDataBase
 * @since 19-11-2011
 */
@Transactional
public class DataBaseJpaOracle extends ADataBaseJpa {

    @PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = Constants.PERSISTENCE_UNIT_ORACLE)
    protected EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

}