package ikube.database.jpa;

import ikube.IConstants;
import ikube.database.IDataBase;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.springframework.beans.factory.annotation.Value;

/**
 * This class is the primary access to the database via Jpa.
 * 
 * @see IDataBase
 * @author Michael Couck
 * @since 28.04.10
 * @version 01.00
 */
public class DataBaseJpa extends ADataBaseJpa {

	@Value("${ikube.persistenceUnit}")
	protected static final String PERSISTENCE_UNIT_NAME = IConstants.PERSISTENCE_UNIT_H2;
	@PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = DataBaseJpa.PERSISTENCE_UNIT_NAME)
	protected EntityManager entityManager;
	
	public DataBaseJpa() {
		LOGGER.info("Entity manager : " + entityManager);
	}

	@Override
	protected EntityManager getEntityManager() {
		return entityManager;
	}

}