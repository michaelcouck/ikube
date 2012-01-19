package ikube.database.jpa;

import javax.persistence.EntityManager;

/**
 * @author Michael Couck
 * @since 19.11.11
 * @version 01.00
 */
public class DataBaseJpaOracle extends ADataBaseJpa {

	// @PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = IConstants.PERSISTENCE_UNIT_ORACLE)
	protected EntityManager entityManager;

	@Override
	protected EntityManager getEntityManager() {
		return entityManager;
	}

}