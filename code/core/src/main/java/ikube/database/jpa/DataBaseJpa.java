package ikube.database.jpa;

import ikube.IConstants;
import ikube.database.IDataBase;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

/**
 * This class is the primary access to the database via Jpa.
 * 
 * @see IDataBase
 * @author Michael Couck
 * @since 28.04.10
 * @version 01.00
 */
public class DataBaseJpa extends ADataBaseJpa {

	@PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = IConstants.PERSISTENCE_UNIT_H2)
	protected EntityManager entityManager;

	@Override
	protected EntityManager getEntityManager() {
		return entityManager;
	}

}