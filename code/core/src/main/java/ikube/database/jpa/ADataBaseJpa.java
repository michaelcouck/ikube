package ikube.database.jpa;

import ikube.database.IDataBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.apache.log4j.Logger;

/**
 * This class is the API to the database, specifically the JPA implementation. This class will typically be wired for transactions either
 * using a transaction handling library like Spring or in the server.
 * 
 * @see IDataBase
 * @author Michael Couck
 * @since 08.10.11
 * @version 01.00
 */
public abstract class ADataBaseJpa implements IDataBase {

	protected static final Logger LOGGER = Logger.getLogger(ADataBaseJpa.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Long count(Class<T> klass) {
		StringBuilder query = new StringBuilder("select count(c) from ");
		query.append(klass.getSimpleName());
		query.append(" as c ");
		return (Long) getEntityManager().createQuery(query.toString()).getSingleResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	@Override
	public <T> T find(final Class<T> klass, final Long id) {
		return getEntityManager().find(klass, id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	public <T> List<T> find(Class<T> klass, String[] fieldsToSortOn, Boolean[] directionOfSort, int firstResult, int maxResults) {
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(klass);
		Root<T> root = criteriaQuery.from(klass);
		criteriaQuery = criteriaQuery.select(root);
		List<Order> orderByOrders = new ArrayList<Order>();
		if (fieldsToSortOn.length > 0) {
			for (int i = 0; i < fieldsToSortOn.length; i++) {
				String fieldToSortOn = fieldsToSortOn[i];
				if (directionOfSort[i]) {
					orderByOrders.add(criteriaBuilder.asc(root.get(fieldToSortOn)));
				} else {
					orderByOrders.add(criteriaBuilder.desc(root.get(fieldToSortOn)));
				}
			}
			criteriaQuery.orderBy(orderByOrders);
		}
		TypedQuery<T> typedQuery = getEntityManager().createQuery(criteriaQuery);
		typedQuery.setFirstResult(firstResult);
		typedQuery.setMaxResults(maxResults);
		return typedQuery.getResultList();
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

	@Override
	public <T> T find(Class<T> klass, String sql, String[] names, Object[] values) {
		Map<String, Object> parameters = getParameterMap(names, values);
		return find(klass, sql, parameters);
	}

	@Override
	public <T> List<T> find(Class<T> klass, String sql, String[] names, Object[] values, int startPosition, int maxResults) {
		Map<String, Object> parameters = getParameterMap(names, values);
		return find(klass, sql, parameters, startPosition, maxResults);
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

	private Map<String, Object> getParameterMap(final String[] names, final Object[] values) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		for (int i = 0; i < names.length; i++) {
			parameters.put(names[i], values[i]);
		}
		return parameters;
	}

	/**
	 * This method sets the parameters in the query.
	 * 
	 * @param query the query to set the parameters for
	 * @param parameters and the parameter map, key value pairs
	 */
	private void setParameters(final Query query, final Map<String, Object> parameters) {
		for (Map.Entry<String, Object> entry : parameters.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			query.setParameter(key, value);
		}
	}

	/**
	 * This method is for sub-classes to implement. The entity managers are defined in annotations, and essentially hard coded, meaning that
	 * each database like DB2 or H2 needs to have their own persistence unit in the {@link PersistenceContext} annotation at compile time.
	 * 
	 * @return the entity manager that is defined in the sub-class for a specific database and persistence unit
	 */
	protected abstract EntityManager getEntityManager();

}