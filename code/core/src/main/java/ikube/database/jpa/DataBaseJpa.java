package ikube.database.jpa;

import ikube.IConstants;
import ikube.database.IDataBase;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

import org.apache.log4j.Logger;

/**
 * This class is the API to the database, specifically the JPA implementation. This class will typically be wired for
 * transactions either using a transaction handling library like Spring or in the server.
 * 
 * @author Michael Couck
 * @since 28.04.10
 * @version 01.00
 */
public class DataBaseJpa implements IDataBase {

	/** The logger for the bean. */
	protected static final Logger	LOGGER	= Logger.getLogger(DataBaseJpa.class);

	@PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = IConstants.PERSISTENCE_UNIT_H2)
	protected EntityManager			entityManager;

	/**
	 * {@inheritDoc}
	 */
	public <T> T remove(final Class<T> klass, Long id) {
		T toBeRemoved = find(klass, id);
		if (toBeRemoved != null) {
			entityManager.remove(toBeRemoved);
		}
		return toBeRemoved;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void removeBatch(final List<T> batch) {
		for (T t : batch) {
			t = entityManager.merge(t);
			entityManager.remove(t);
		}
		entityManager.flush();
		entityManager.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T remove(final T object) {
		Object result = entityManager.merge(object);
		entityManager.remove(result);
		return (T) result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int remove(final String sql) {
		return entityManager.createNamedQuery(sql).executeUpdate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T persist(final T object) {
		if (object != null) {
			entityManager.persist(object);
		}
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void persistBatch(final List<T> list) {
		for (T t : list) {
			entityManager.persist(t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T merge(final T object) {
		Object result = null;
		if (object != null) {
			result = entityManager.merge(object);
		}
		return (T) result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void mergeBatch(final List<T> batch) {
		for (T t : batch) {
			t = entityManager.merge(t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T find(final Class<T> klass, final Long id) {
		return entityManager.find(klass, id);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> find(final Class<T> klass, final int firstResult, final int maxResults) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T find(final Long objectId) {
		try {
			Set<EntityType<?>> entityTypes = entityManager.getMetamodel().getEntities();
			for (EntityType<?> entityType : entityTypes) {
				Object object = entityManager.find(entityType.getJavaType(), objectId);
				if (object != null) {
					return (T) object;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception looking up the entity : " + objectId, e);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T find(final Class<T> klass, final String sql, final Map<String, Object> parameters) {
		Query query = entityManager.createNamedQuery(sql, klass);
		setParameters(query, parameters);
		return (T) query.getSingleResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> find(final Class<T> klass, final String sql, final Map<String, Object> parameters, final int startPosition,
			final int maxResults) {
		Query query = entityManager.createNamedQuery(sql, klass);
		query.setFirstResult(startPosition);
		query.setMaxResults(maxResults);
		setParameters(query, parameters);
		return query.getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T execute(final Class<T> klass, final String sql) {
		return (T) entityManager.createNamedQuery(sql).getSingleResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T execute(Class<T> klass, String sql, Map<String, Object> parameters) {
		Query query = entityManager.createNamedQuery(sql);
		setParameters(query, parameters);
		return (T) query.getSingleResult();
	}

	/**
	 * This method sets the parameters in the query.
	 * 
	 * @param query
	 *            the query to set the parameters for
	 * @param parameters
	 *            and the parameter map, key value pairs
	 */
	private void setParameters(final Query query, final Map<String, Object> parameters) {
		for (Map.Entry<String, Object> entry : parameters.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			query.setParameter(key, value);
		}
	}

}