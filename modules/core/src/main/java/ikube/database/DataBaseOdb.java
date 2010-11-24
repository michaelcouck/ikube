package ikube.database;

import ikube.IConstants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import org.apache.log4j.Logger;
import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.OID;
import org.neodatis.odb.Objects;
import org.neodatis.odb.OdbConfiguration;
import org.neodatis.odb.core.query.IQuery;
import org.neodatis.odb.core.query.criteria.And;
import org.neodatis.odb.core.query.criteria.ICriterion;
import org.neodatis.odb.core.query.criteria.Where;
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery;
import org.neodatis.tool.DLogger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class DataBaseOdb implements IDataBase {

	private Logger logger;
	private ODB odb;
	private Map<Class<?>, Field> idFields;
	private List<Index> indexes;

	public void initialise() {
		this.logger = Logger.getLogger(this.getClass());
		this.idFields = new HashMap<Class<?>, Field>();

		DLogger.register(new ikube.database.Logger());
		OdbConfiguration.setDebugEnabled(Boolean.FALSE);
		OdbConfiguration.setDebugEnabled(5, Boolean.FALSE);
		OdbConfiguration.setLogAll(Boolean.FALSE);
		// OdbConfiguration.lockObjectsOnSelect(Boolean.TRUE);
		OdbConfiguration.useMultiThread(Boolean.FALSE, 3);
		// OdbConfiguration.setAutomaticallyIncreaseCacheSize(Boolean.TRUE);
		// OdbConfiguration.setAutomaticCloseFileOnExit(Boolean.TRUE);
		OdbConfiguration.setDisplayWarnings(Boolean.FALSE);
		OdbConfiguration.setMultiThreadExclusive(Boolean.FALSE);
		// OdbConfiguration.setReconnectObjectsToSession(Boolean.TRUE);
		OdbConfiguration.setUseCache(Boolean.TRUE);
		OdbConfiguration.setUseIndex(Boolean.TRUE);
		OdbConfiguration.setUseMultiBuffer(Boolean.FALSE);
		OdbConfiguration.setShareSameVmConnectionMultiThread(Boolean.FALSE);

		if (indexes != null) {
			for (Index index : indexes) {
				Class<?> klass = null;
				try {
					klass = Class.forName(index.getClassName());
				} catch (ClassNotFoundException e) {
					logger.error("Exception creating index for class : " + index.getClassName(), e);
					continue;
				}
				this.odb = ODBFactory.open(IConstants.DATABASE_FILE);
				if (!this.odb.getClassRepresentation(klass).existIndex(klass.getSimpleName())) {
					String[] fieldNames = index.getFieldNames().toArray(new String[index.getFieldNames().size()]);
					this.odb.getClassRepresentation(klass).addIndexOn(klass.getSimpleName(), fieldNames, Boolean.TRUE);
				}
			}
		}
	}

	@Override
	public synchronized <T> T persist(T object) {
		try {
			// Set the id of this object
			Object idFieldValue = getIdFieldValue(object);
			if (idFieldValue != null && Long.class.isAssignableFrom(idFieldValue.getClass()) && ((Long) idFieldValue).longValue() != 0l) {
				logger.info("Object already stored : " + object + ", will merge");
			} else {
				OID oid = this.odb.store(object);
				long id = oid.getObjectId();
				setIdField(object, id);
			}
			this.odb.store(object);
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			this.odb.commit();
			notifyAll();
		}
		return object;
	}

	@Override
	public synchronized <T> T find(Class<T> klass, Long id) {
		try {
			String idFieldName = getIdFieldName(klass);
			ICriterion criterion = Where.equal(idFieldName, id);
			CriteriaQuery criteriaQuery = new CriteriaQuery(klass, criterion);
			Objects<T> objects = this.odb.getObjects(criteriaQuery);
			if (objects.size() > 1) {
				throw new RuntimeException("Object id not unique : ");
			}
			if (objects.size() == 0) {
				return null;
			}
			return objects.getFirst();
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			notifyAll();
		}
		return null;
	}

	@Override
	public synchronized <T> T find(Class<T> klass, Map<String, Object> parameters, boolean unique) {
		try {
			And and = new And();
			for (String field : parameters.keySet()) {
				Object value = parameters.get(field);
				if (value != null) {
					and.add(Where.equal(field, value));
				}
			}
			IQuery query = new CriteriaQuery(klass, and);
			Objects<T> objects = odb.getObjects(query);
			if (objects.size() == 0) {
				return null;
			}
			if (unique && objects.size() > 1) {
				logger.warn("More than one objects returned from : " + klass + "," + parameters);
				throw new RuntimeException("More than one object returned by unique query : " + klass + ", " + parameters);
			}
			return objects.getFirst();
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			notifyAll();
		}
		return null;
	}

	@Override
	public synchronized <T> List<T> find(Class<T> klass, int firstResult, int maxResults) {
		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			return find(klass, parameters, firstResult, maxResults);
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized <T> List<T> find(Class<T> klass, Map<String, Object> parameters, int firstResult, int maxResults) {
		List<T> list = new ArrayList<T>();
		try {
			And criterion = new And();
			for (String field : parameters.keySet()) {
				Object value = parameters.get(field);
				criterion.add(Where.equal(field, value));
			}
			IQuery query = new CriteriaQuery(klass, criterion);
			Objects<T> objects = odb.getObjects(query);
			if (objects.size() == 0) {
				return list;
			}
			for (int i = 0; i < firstResult && objects.hasNext(); i++) {
				objects.next();
			}
			for (int i = 0; i < maxResults && objects.hasNext(); i++) {
				T t = objects.next();
				list.add(t);
			}
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			notifyAll();
		}
		return list;
	}

	@Override
	public synchronized <T> T merge(T t) {
		try {
			this.odb.store(t);
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			this.odb.commit();
			notifyAll();
		}
		return t;
	}

	@Override
	public synchronized <T> T remove(T t) {
		try {
			this.odb.delete(t);
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			this.odb.commit();
			notifyAll();
		}
		return t;
	}

	@Override
	public synchronized <T> T remove(Class<T> klass, Long id) {
		try {
			T object = find(klass, id);
			if (object != null) {
				remove(object);
			}
			return object;
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			this.odb.commit();
			notifyAll();
		}
		return null;
	}

	public void setOdb(ODB odb) {
		this.odb = odb;
	}

	protected synchronized void close() {
		try {
			if (this.odb != null && !this.odb.isClosed()) {
				this.odb.commit();
				this.odb.close();
			}
		} finally {
			notifyAll();
		}
	}

	protected synchronized <T> void setIdField(T object, long id) {
		Field idField = getIdField(object.getClass(), null);
		if (idField != null) {
			try {
				idField.set(object, id);
			} catch (IllegalArgumentException e) {
				logger.error("Can't set the id : " + id + ", in the field : " + idField + ", of object : " + object, e);
			} catch (IllegalAccessException e) {
				logger.error("Field not accessible : " + idField, e);
			}
		} else {
			logger.warn("No id field defined for object : " + object);
		}
	}

	protected synchronized Field getIdField(Class<?> klass, Class<?> superKlass) {
		Field idField = idFields.get(klass);
		if (idField != null) {
			return idField;
		}
		// logger.info("Getting id for field : " + klass + ", " + superKlass);
		Field[] fields = superKlass != null ? superKlass.getDeclaredFields() : klass.getDeclaredFields();
		for (Field field : fields) {
			Id idAnnotation = field.getAnnotation(Id.class);
			if (idAnnotation != null) {
				idFields.put(klass, field);
				field.setAccessible(Boolean.TRUE);
				return field;
			}
		}
		// Try the super classes
		Class<?> superClass = superKlass != null ? superKlass.getSuperclass() : klass.getSuperclass();
		if (superClass != null && !Object.class.getName().equals(superClass.getName())) {
			return getIdField(klass, superClass);
		}
		return null;
	}

	protected synchronized <T> Object getIdFieldValue(T object) {
		Field idField = getIdField(object.getClass(), null);
		if (idField != null) {
			try {
				return idField.get(object);
			} catch (IllegalArgumentException e) {
				logger.error("Can't get the id in the field : " + idField + ", of object : " + object, e);
			} catch (IllegalAccessException e) {
				logger.error("Field not accessible : " + idField, e);
			}
		} else {
			logger.warn("No id field defined for object : " + object);
		}
		return null;
	}

	protected synchronized String getIdFieldName(Class<?> klass) {
		Field field = getIdField(klass, null);
		return field != null ? field.getName() : null;
	}

	public void setIndexes(List<Index> indexes) {
		this.indexes = indexes;
	}

}