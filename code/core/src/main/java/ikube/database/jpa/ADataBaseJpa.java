package ikube.database.jpa;

import ikube.database.IDataBase;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

import org.apache.log4j.Logger;

/**
 * This class is the API to the database, specifically the JPA implementation. This class will typically be wired for
 * transactions either using a transaction handling library like Spring or in the server.
 * 
 * @author Michael Couck
 * @since 08.10.11
 * @version 01.00
 */
public abstract class ADataBaseJpa implements IDataBase {

	protected static final Logger	LOGGER	= Logger.getLogger(ADataBaseJpa.class);

	/**
	 * {@inheritDoc}
	 */
	public <T> T remove(final Class<T> klass, Long id) {
		T toBeRemoved = find(klass, id);
		if (toBeRemoved != null) {
			getEntityManager().remove(toBeRemoved);
		}
		return toBeRemoved;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void removeBatch(final List<T> batch) {
		for (T t : batch) {
			t = getEntityManager().merge(t);
			getEntityManager().remove(t);
		}
		getEntityManager().flush();
		getEntityManager().clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T remove(final T object) {
		Object result = getEntityManager().merge(object);
		getEntityManager().remove(result);
		return (T) result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int remove(final String sql) {
		return getEntityManager().createNamedQuery(sql).executeUpdate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T persist(final T object) {
		if (object != null) {
			getEntityManager().persist(object);
		}
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void persistBatch(final List<T> list) {
		for (T t : list) {
			getEntityManager().persist(t);
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
			result = getEntityManager().merge(object);
		}
		return (T) result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void mergeBatch(final List<T> batch) {
		for (T t : batch) {
			t = getEntityManager().merge(t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T find(final Class<T> klass, final Long id) {
		return getEntityManager().find(klass, id);
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
		Query query = getEntityManager().createQuery(builder.toString());
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
			Set<EntityType<?>> entityTypes = getEntityManager().getMetamodel().getEntities();
			for (EntityType<?> entityType : entityTypes) {
				Object object = getEntityManager().find(entityType.getJavaType(), objectId);
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
		Query query = getEntityManager().createNamedQuery(sql, klass);
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
		Query query = getEntityManager().createNamedQuery(sql, klass);
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
		return (T) getEntityManager().createNamedQuery(sql).getSingleResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T execute(Class<T> klass, String sql, Map<String, Object> parameters) {
		Query query = getEntityManager().createNamedQuery(sql);
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

	protected abstract EntityManager getEntityManager();

}