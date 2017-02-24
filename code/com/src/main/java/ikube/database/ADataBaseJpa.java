package ikube.database;

import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is the API to the database, specifically the JPA implementation. This class
 * will typically be wired for transactions either using a transaction handling library like Spring
 * or in the server.
 *
 * @author Michael Couck
 * @version 01.00
 * @see IDataBase
 * @since 08-10-2011
 */
@Transactional
public abstract class ADataBaseJpa implements IDataBase {

    protected static final Logger LOGGER = Logger.getLogger(ADataBaseJpa.class);

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @SuppressWarnings("StringBufferReplaceableByString")
    public <T> Long count(final Class<T> klass) {
        StringBuilder query = new StringBuilder("select count(c) from ");
        query.append(klass.getSimpleName());
        query.append(" as c ");
        return (Long) getEntityManager().createQuery(query.toString()).getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public <T> T remove(final Class<T> klass, final Long id) {
        T toBeRemoved = find(klass, id);
        if (toBeRemoved != null) {
            remove(toBeRemoved);
        }
        return toBeRemoved;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public <T> void removeBatch(final List<T> batch) {
        for (final T t : batch) {
            remove(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
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
    @Transactional
    public int remove(final String sql) {
        return getEntityManager().createNamedQuery(sql).executeUpdate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
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
    @Transactional
    public <T> void persistBatch(final List<T> list) {
        for (final T t : list) {
            persist(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
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
    @Transactional
    public <T> void mergeBatch(final List<T> batch) {
        for (final T t : batch) {
            merge(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public <T> T find(final Class<T> klass, final Long id) {
        return getEntityManager().find(klass, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public <T> List<T> find(final Class<T> klass, final int firstResult, final int maxResults) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(klass);
        Root<T> root = criteriaQuery.from(klass);
        criteriaQuery = criteriaQuery.select(root);
        TypedQuery<T> typedQuery = getEntityManager().createQuery(criteriaQuery);
        typedQuery.setFirstResult(firstResult);
        typedQuery.setMaxResults(maxResults);
        return typedQuery.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public <T> List<T> find(final Class<T> klass, final String[] fieldsToSortOn, final Boolean[] directionOfSort,
                            final int firstResult, final int maxResults) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(klass);
        Root<T> root = criteriaQuery.from(klass);
        criteriaQuery = criteriaQuery.select(root);
        List<Order> orderByOrders = new ArrayList<>();
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
    @Transactional
    public <T> T find(final Class<T> klass, final String[] fieldsToFilterOn, final Object[] valuesToFilterOn) {
        List<T> entities = find(klass, fieldsToFilterOn, valuesToFilterOn, 0, 1);
        if (entities.size() == 0) {
            return null;
        }
        return entities.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public <T> List<T> find(final Class<T> klass, final String[] fieldsToFilterOn, final Object[] valuesToFilterOn,
                            final int firstResult, final int maxResults) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(klass);
        Root<T> root = criteriaQuery.from(klass);
        criteriaQuery = criteriaQuery.select(root);

        List<Predicate> predicates = new ArrayList<>();
        for (int i = 0; i < fieldsToFilterOn.length; i++) {
            Path<?> path = root.get(fieldsToFilterOn[i]);
            Predicate predicate = criteriaBuilder.equal(path, valuesToFilterOn[i]);
            predicates.add(predicate);
        }
        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

        TypedQuery<T> typedQuery = getEntityManager().createQuery(criteriaQuery);
        typedQuery.setFirstResult(firstResult);
        typedQuery.setMaxResults(maxResults);
        return typedQuery.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
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
    @Transactional
    @SuppressWarnings("unchecked")
    public <T> T find(final Class<T> klass, final String sql, final Map<String, Object> parameters) {
        String[] names = parameters.keySet().toArray(new String[parameters.keySet().size()]);
        Object[] values = parameters.values().toArray(new Object[parameters.values().size()]);
        return find(klass, sql, names, values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public <T> List<T> find(final Class<T> klass, final String sql, final Map<String, Object> parameters, final int firstResult,
                            final int maxResults) {
        String[] names = parameters.keySet().toArray(new String[parameters.keySet().size()]);
        Object[] values = parameters.values().toArray(new Object[parameters.values().size()]);
        return find(klass, sql, names, values, firstResult, maxResults);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public <T> T find(final Class<T> klass, final String sql, final String[] names, final Object[] values) {
        Query query = getEntityManager().createNamedQuery(sql, klass);
        setParameters(query, names, values);
        return (T) query.getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public <T> List<T> find(final Class<T> klass, final String sql, final String[] names, final Object[] values, final int firstResult,
                            final int maxResults) {
        Query query = getEntityManager().createNamedQuery(sql, klass);
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        setParameters(query, names, values);
        return query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public <T> T execute(final String sql, final String[] names, final Object[] values) {
        Query query = getEntityManager().createQuery(sql);
        setParameters(query, names, values);
        return (T) query.getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public <T> T refresh(final T t) {
        getEntityManager().refresh(t);
        return t;
    }

    private void setParameters(final Query query, final String[] names, final Object[] values) {
        if (names == null || names.length == 0 || values == null || values.length == 0) {
            return;
        }
        for (int i = 0; i < names.length; i++) {
            query.setParameter(names[i], values[i]);
        }
    }

    /**
     * This method is for sub-classes to implement. The entity managers are defined in annotations,
     * and essentially hard coded, meaning that each database like DB2 or H2 needs to have their own persistence
     * unit in the {@link PersistenceContext} annotation at compile time.
     *
     * @return the entity manager that is defined in the sub-class for a specific database and persistence unit
     */
    protected abstract EntityManager getEntityManager();

}