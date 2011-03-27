package ikube.database.jpa;

import ikube.IConstants;
import ikube.database.IDataBase;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;

import org.apache.log4j.Logger;

/**
 * Note: This class is not completely implemented.
 * 
 * @author Michael Couck
 * @since 28.04.10
 * @version 01.00
 */
public class DataBaseJpa implements IDataBase {

	/** The logger for the bean. */
	protected Logger logger = Logger.getLogger(DataBaseJpa.class);
	/** Entity manager for the bean will be injected. */
	@PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = IConstants.PERSISTENCE_UNIT_NAME)
	protected EntityManager entityManager;

	/**
	 * {@inheritDoc}
	 */
	public <T> T remove(Class<T> klass, Long id) {
		T toBeRemoved = find(klass, id);
		if (toBeRemoved != null) {
			entityManager.remove(toBeRemoved);
		}
		return toBeRemoved;
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T persist(T object) {
		if (object != null) {
			entityManager.persist(object);
		}
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T merge(T object) {
		if (object != null) {
			object = entityManager.merge(object);
		}
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T find(Class<T> klass, Long id) {
		return entityManager.find(klass, id);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> find(Class<T> klass, int firstResult, int maxResults) {
		String name = klass.getSimpleName();
		StringBuilder builder = new StringBuilder("select ");
		builder.append(name);
		builder.append(" from ");
		builder.append(name);
		builder.append(" as ");
		builder.append(name);
		Query query = entityManager.createQuery(builder.toString());
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}

	@Override
	public <T> T remove(T object) {
		entityManager.remove(object);
		return object;
	}

	@Override
	public <T> T find(Long objectId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T find(Class<T> klass, Map<String, Object> parameters, boolean unique) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> find(Class<T> klass, Map<String, Object> parameters, int startIndex, int endIndex) {
		// TODO Auto-generated method stub
		return null;
	}

}