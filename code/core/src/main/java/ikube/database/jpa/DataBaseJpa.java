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
 * TODO Comment me!
 * 
 * @author Michael Couck
 * @since 28.04.10
 * @version 01.00
 */
public class DataBaseJpa implements IDataBase {

	/** The logger for the bean. */
	protected static final Logger LOGGER = Logger.getLogger(DataBaseJpa.class);

	@PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = IConstants.PERSISTENCE_UNIT_H2)
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
	@Override
	public <T> void removeBatch(List<T> batch) {
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
	public <T> T remove(T object) {
		object = entityManager.merge(object);
		entityManager.remove(object);
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int remove(String sql) {
		return entityManager.createNamedQuery(sql).executeUpdate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T persist(T object) {
		if (object != null) {
			entityManager.persist(object);
		}
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void persistBatch(List<T> list) {
		for (T t : list) {
			entityManager.persist(t);
		}
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
	@Override
	public <T> void mergeBatch(List<T> batch) {
		for (T t : batch) {
			t = entityManager.merge(t);
		}
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T find(Long objectId) {
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
	public <T> T find(Class<T> klass, String sql, Map<String, Object> parameters) {
		Query query = entityManager.createNamedQuery(sql, klass);
		setParameters(query, parameters);
		return (T) query.getSingleResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> find(Class<T> klass, String sql, Map<String, Object> parameters, int startPosition, int maxResults) {
		Query query = entityManager.createNamedQuery(sql, klass);
		query.setFirstResult(startPosition);
		query.setMaxResults(maxResults);
		setParameters(query, parameters);
		return query.getResultList();
	}

	private void setParameters(Query query, Map<String, Object> parameters) {
		for (String parameter : parameters.keySet()) {
			query.setParameter(parameter, parameters.get(parameter));
		}
	}

}