package ikube.database.odb;

import ikube.IConstants;
import ikube.database.IDataBase;

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
	private List<Index> indexes;

	private Map<Class<?>, Field> idFields;
	private boolean initialised = Boolean.FALSE;

	public DataBaseOdb() {
		this.logger = Logger.getLogger(this.getClass());
		this.idFields = new HashMap<Class<?>, Field>();
	}

	public void initialise() {
		initialise(IConstants.DATABASE_FILE);
	}

	protected void initialise(String dataBaseFile) {
		if (initialised && odb != null) {
			return;
		}
		initialised = Boolean.TRUE;
		configureDataBase();
		openDataBase(dataBaseFile);
		createIndexes();
	}

	protected void createIndexes() {
		if (this.indexes != null && this.odb != null) {
			for (Index index : indexes) {
				try {
					Class<?> klass = Class.forName(index.getClassName());
					if (!this.odb.getClassRepresentation(klass).existIndex(klass.getSimpleName())) {
						String[] fieldNames = index.getFieldNames().toArray(new String[index.getFieldNames().size()]);
						this.odb.getClassRepresentation(klass).addIndexOn(klass.getSimpleName(), fieldNames, Boolean.TRUE);
					}
				} catch (Exception e) {
					logger.error("Exception creating index for class : " + index.getClassName(), e);
					continue;
				}
			}
		}
	}

	protected void updateIndex(Class<?> klass) {
		this.odb.getClassRepresentation(klass).rebuildIndex(klass.getSimpleName(), Boolean.TRUE);
	}

	protected void deleteIndex(Class<?> klass) {
		this.odb.getClassRepresentation(klass).deleteIndex(klass.getSimpleName(), Boolean.TRUE);
	}

	protected void configureDataBase() {
		DLogger.register(new ikube.database.odb.Logger());
		OdbConfiguration.setDebugEnabled(Boolean.FALSE);
		OdbConfiguration.setDebugEnabled(5, Boolean.FALSE);
		OdbConfiguration.setLogAll(Boolean.FALSE);
		// OdbConfiguration.lockObjectsOnSelect(Boolean.TRUE);
		OdbConfiguration.useMultiThread(Boolean.FALSE, 1);
		// OdbConfiguration.setAutomaticallyIncreaseCacheSize(Boolean.TRUE);
		// OdbConfiguration.setAutomaticCloseFileOnExit(Boolean.TRUE);
		OdbConfiguration.setDisplayWarnings(Boolean.TRUE);
		// OdbConfiguration.setMultiThreadExclusive(Boolean.FALSE);
		// OdbConfiguration.setReconnectObjectsToSession(Boolean.TRUE);
		OdbConfiguration.setUseCache(Boolean.TRUE);
		OdbConfiguration.setUseIndex(Boolean.TRUE);
		OdbConfiguration.setUseMultiBuffer(Boolean.TRUE);
		// OdbConfiguration.setShareSameVmConnectionMultiThread(Boolean.FALSE);
	}

	protected void openDataBase(String dataBaseFile) {
		this.odb = ODBFactory.open(dataBaseFile);
	}

	@Override
	public synchronized <T> T persist(T object) {
		try {
			if (object != null) {
				setIdField(object, System.nanoTime());
				this.odb.store(object);
			}
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			this.odb.commit();
			notifyAll();
		}
		return object;
	}

	@Override
	public synchronized <T> T remove(T t) {
		try {
			if (t != null) {
				this.odb.deleteCascade(t);
			}
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			this.odb.commit();
			notifyAll();
		}
		return t;
	}

	@Override
	public synchronized <T> T merge(T t) {
		try {
			if (t != null) {
				OID oid = this.odb.getObjectId(t);
				if (oid != null) {
					this.odb.ext().replace(oid, t);
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			this.odb.commit();
			notifyAll();
		}
		return t;
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
			if (unique && objects.size() > 1) {
				logger.warn("More than one objects returned from : " + klass + "," + parameters);
				throw new RuntimeException("More than one object returned by unique query : " + klass + ", " + parameters);
			}
			if (objects.size() > 0) {
				return objects.getFirst();
			}
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			// this.odb.commit();
			notifyAll();
		}
		return null;
	}

	@Override
	public synchronized <T> List<T> find(Class<T> klass, int firstResult, int maxResults) {
		List<T> list = new ArrayList<T>();
		try {
			IQuery query = new CriteriaQuery(klass);
			Objects<T> objects = odb.getObjects(query);
			for (int i = 0; i < firstResult && objects.hasNext(); i++) {
				objects.next();
			}
			for (int i = 0; i < maxResults && objects.hasNext(); i++) {
				list.add(objects.next());
			}
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			// this.odb.commit();
			notifyAll();
		}
		return list;
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
				list.add(objects.next());
			}
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			// this.odb.commit();
			notifyAll();
		}
		return list;
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
		if (object == null) {
			return;
		}
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
		if (object == null) {
			return null;
		}
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