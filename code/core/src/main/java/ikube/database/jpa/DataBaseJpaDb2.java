package ikube.database.jpa;

import ikube.IConstants;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

/**
 * @author Michael Couck
 * @since 28.04.10
 * @version 01.00
 */
public class DataBaseJpaDb2 extends ADataBaseJpa {

	@PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = IConstants.PERSISTENCE_UNIT_DB2)
	protected EntityManager	entityManager;

	@Override
	protected EntityManager getEntityManager() {
		return entityManager;
	}

}