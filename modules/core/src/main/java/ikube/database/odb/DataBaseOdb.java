package ikube.database.odb;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.Persistable;
import ikube.toolkit.DatabaseUtilities;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.OID;
import org.neodatis.odb.Objects;
import org.neodatis.odb.OdbConfiguration;
import org.neodatis.odb.core.query.IQuery;
import org.neodatis.odb.core.query.criteria.And;
import org.neodatis.odb.core.query.criteria.Where;
import org.neodatis.odb.core.query.nq.NativeQuery;
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

	private boolean initialised = Boolean.FALSE;

	public DataBaseOdb() {
		this.logger = Logger.getLogger(this.getClass());
	}

	public void initialise() {
		initialise(System.nanoTime() + "." + IConstants.DATABASE_FILE);
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
		OdbConfiguration.setMultiThreadExclusive(Boolean.TRUE);
		// OdbConfiguration.setReconnectObjectsToSession(Boolean.TRUE);
		OdbConfiguration.setUseCache(Boolean.TRUE);
		OdbConfiguration.setUseIndex(Boolean.TRUE);
		OdbConfiguration.setUseMultiBuffer(Boolean.FALSE);
		// OdbConfiguration.setShareSameVmConnectionMultiThread(Boolean.FALSE);
	}

	protected void openDataBase(String dataBaseFile) {
		this.odb = ODBFactory.open(dataBaseFile);
	}

	@Override
	public synchronized <T> T persist(T object) {
		try {
			if (object != null) {
				Object idFieldValue = DatabaseUtilities.getIdFieldValue(object);
				if (idFieldValue == null) {
					DatabaseUtilities.setIdField(object, System.nanoTime());
				}
				// Check for duplicate keys
				Object duplicate = find(object.getClass(), (Long) idFieldValue);
				if (duplicate != null) {
					throw new RuntimeException("Duplicate key for object : " + idFieldValue + ", " + object + ", " + object.getClass());
				}
				this.odb.store(object);
			}
		} catch (Exception e) {
			logger.error("Exception persisting object : " + object, e);
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
			logger.error("Exception removing object : " + t, e);
		} finally {
			this.odb.commit();
			notifyAll();
		}
		return t;
	}

	public synchronized <T> T remove(Class<T> klass, Long id) {
		try {
			T t = find(klass, id);
			this.odb.delete(t);
			return t;
		} finally {
			notifyAll();
		}
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
			logger.error("Exception merging object : " + t, e);
		} finally {
			this.odb.commit();
			notifyAll();
		}
		return t;
	}

	/**
	 * Note: This method is EXPENSIVE! Use only in emergency.
	 */
	@Override
	public synchronized <T> T find(final Long id) {
		try {
			IQuery query = new NativeQuery() {
				{
					setPolymorphic(Boolean.TRUE);
				}

				@Override
				public boolean match(Object object) {
					if (!Persistable.class.isAssignableFrom(object.getClass())) {
						return Boolean.FALSE;
					}
					Object idFieldValue = DatabaseUtilities.getIdFieldValue(object);
					return id.equals(idFieldValue);
				}

				@Override
				public Class<Object> getObjectType() {
					return Object.class;
				}
			};

			Objects<T> objects = this.odb.getObjects(query);
			if (objects.size() == 0) {
				return null;
			}
			if (objects.size() > 1) {
				logger.info("More than one object returned from query : ");
			}
			return objects.getFirst();
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized <T> T find(Class<T> klass, Long id) {
		try {
			String idFieldName = DatabaseUtilities.getIdFieldName(klass);
			IQuery query = new CriteriaQuery(klass, Where.equal(idFieldName, id));
			Objects<T> objects = this.odb.getObjects(query);
			if (objects.size() > 1) {
				throw new RuntimeException("Duplicate key : " + id + ", " + klass);
			}
			if (objects.size() == 1) {
				return objects.getFirst();
			}
			return null;
		} finally {
			notifyAll();
		}
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
				throw new RuntimeException("More than one object returned by unique query : " + klass + ", " + parameters + ", " + unique);
			}
			if (objects.size() > 0) {
				return objects.getFirst();
			}
		} catch (Exception e) {
			logger.error("Exception finding object : " + klass + ", " + parameters + ", " + unique, e);
		} finally {
			notifyAll();
		}
		return null;
	}

	@Override
	public synchronized <T> List<T> find(Class<T> klass, int startIndex, int endIndex) {
		List<T> list = new ArrayList<T>();
		try {
			IQuery query = new CriteriaQuery(klass);
			Objects<T> objects = odb.getObjects(query, Boolean.TRUE, startIndex, endIndex);
			// return getFirstMax(objects, startIndex, endIndex);
			@SuppressWarnings("unchecked")
			T[] array = (T[]) Array.newInstance(klass, objects.size());
			System.arraycopy(objects.toArray(), 0, array, 0, array.length);
			return Arrays.asList(array);
		} catch (Exception e) {
			logger.error("Exception finding object : " + klass + ", " + startIndex + ", " + endIndex, e);
		} finally {
			notifyAll();
		}
		return list;
	}

	@Override
	public synchronized <T> List<T> find(Class<T> klass, Map<String, Object> parameters, int startIndex, int endIndex) {
		List<T> list = new ArrayList<T>();
		try {
			And criterion = new And();
			for (String field : parameters.keySet()) {
				Object value = parameters.get(field);
				criterion.add(Where.equal(field, value));
			}
			IQuery query = new CriteriaQuery(klass, criterion);
			Objects<T> objects = odb.getObjects(query, Boolean.TRUE, startIndex, endIndex);
			// return getFirstMax(objects, startIndex, endIndex);
			@SuppressWarnings("unchecked")
			T[] array = (T[]) Array.newInstance(klass, objects.size());
			System.arraycopy(objects.toArray(), 0, array, 0, array.length);
			return Arrays.asList(array);
		} catch (Exception e) {
			logger.error("Exception finding objects : " + klass + ", " + parameters + ", " + startIndex + ", " + endIndex, e);
		} finally {
			notifyAll();
		}
		return list;
	}

	protected <T> List<T> getFirstMax(Objects<T> objects, int firstResult, int maxResults) {
		List<T> list = new ArrayList<T>();
		if (objects.size() == 0) {
			return list;
		}
		for (int i = 0; i < firstResult && objects.hasNext(); i++) {
			objects.next();
		}
		for (int i = 0; i < maxResults && objects.hasNext(); i++) {
			list.add(objects.next());
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

	public void setIndexes(List<Index> indexes) {
		this.indexes = indexes;
	}

}