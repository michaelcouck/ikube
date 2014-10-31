package ikube.database;

import ikube.IConstants;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

/**
 * This class is the primary access to the database via Jpa.
 *
 * @author Michael Couck
 * @version 01.00
 * @see IDataBase
 * @since 28-04-2010
 */
public class DataBaseJpaH2 extends ADataBaseJpa {

	@PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = IConstants.PERSISTENCE_UNIT_H2)
	protected EntityManager entityManager;

	@Override
	protected EntityManager getEntityManager() {
		return entityManager;
	}

}