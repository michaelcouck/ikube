package ikube.database.odb;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.Persistable;
import ikube.toolkit.DatabaseUtilities;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.neodatis.odb.ClassRepresentation;
import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.OID;
import org.neodatis.odb.Objects;
import org.neodatis.odb.OdbConfiguration;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.MetaModel;
import org.neodatis.odb.core.query.IQuery;
import org.neodatis.odb.core.query.criteria.And;
import org.neodatis.odb.core.query.criteria.Where;
import org.neodatis.odb.core.query.nq.NativeQuery;
import org.neodatis.odb.core.transaction.ISession;
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery;
import org.neodatis.odb.impl.main.ODBAdapter;

/**
 * @see IDataBase
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class DataBaseOdb implements IDataBase {

	private Logger logger;

	/** The Neodatis persistence object. */
	private ODB odb;
	/** The list of indexes to create on the objects. */
	private List<Index> indexes;
	/** The initialization flag. */
	private boolean initialised = Boolean.FALSE;
	private Runnable dataBaseOdbDefragmenter;

	protected void initialise() {
		initialise(System.nanoTime() + "." + IConstants.DATABASE_FILE);
	}

	protected synchronized void initialise(final String dataBaseFile) {
		try {
			if (initialised && odb != null) {
				return;
			}
			initialised = Boolean.TRUE;
			logger = Logger.getLogger(getClass());
			configureDataBase();
			openDataBase(dataBaseFile);
			createIndexes();

			if (dataBaseOdbDefragmenter == null) {
				dataBaseOdbDefragmenter = new DataBaseOdbDefragmenter(dataBaseFile, this);
				new Thread(dataBaseOdbDefragmenter).start();
			}
		} finally {
			notifyAll();
		}
	}

	protected synchronized long getTotalObjects() {
		try {
			if (odb == null) {
				return 0;
			}
			odb.commit();
			ISession session = ((ODBAdapter) odb).getSession();
			MetaModel metaModel = session.getMetaModel();
			Collection<ClassInfo> classInfos = metaModel.getUserClasses();
			long totalObjects = 0;
			for (ClassInfo classInfo : classInfos) {
				// logger.info("Class info : " + classInfo);
				String className = classInfo.getFullClassName();
				if (className.startsWith(IConstants.IKUBE)) {
					CriteriaQuery query = null;
					try {
						query = new CriteriaQuery(Class.forName(className));
					} catch (ClassNotFoundException e) {
						logger.error("", e);
						continue;
					}
					BigInteger bigInteger = odb.count(query);
					// logger.info("Total objects : " + totalObjects + ", " + bigInteger.longValue() + ", " + className);
					totalObjects += bigInteger.longValue();
				}
			}
			return totalObjects;
		} finally {
			notifyAll();
		}
	}

	protected synchronized String defragment(final String oldDataBaseFile) {
		try {
			logger.info("Defragmenting database : ");
			odb.commit();
			String dataBaseFile = System.nanoTime() + "." + IConstants.DATABASE_FILE;
			odb.defragmentTo(dataBaseFile);
			close();
			initialise(dataBaseFile);
			// Delete the old database
			boolean deleted = FileUtilities.deleteFile(new File(oldDataBaseFile), 1);
			if (!deleted) {
				logger.info("Didn't delete old database file : " + deleted + ", " + oldDataBaseFile);
			}
			return dataBaseFile;
		} finally {
			notifyAll();
		}
	}

	protected synchronized void createIndexes() {
		try {
			if (this.indexes != null && this.odb != null) {
				logger.info("Creating indexes : " + indexes);
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
		} finally {
			notifyAll();
		}
	}

	protected synchronized void updateIndex(Class<?> klass) {
		try {
			this.odb.getClassRepresentation(klass).rebuildIndex(klass.getSimpleName(), Boolean.TRUE);
		} finally {
			notifyAll();
		}
	}

	protected synchronized void deleteIndex(Class<?> klass) {
		try {
			String indexName = klass.getSimpleName();
			ClassRepresentation classRepresentation = this.odb.getClassRepresentation(klass);
			if (classRepresentation.existIndex(indexName)) {
				classRepresentation.deleteIndex(indexName, Boolean.TRUE);
			}
		} finally {
			notifyAll();
		}
	}

	protected void configureDataBase() {
		try {
			logger.info("Configuring database : ");
			// DLogger.register(new ikube.database.odb.Logger());
			OdbConfiguration.setDebugEnabled(Boolean.FALSE);
			OdbConfiguration.setDebugEnabled(5, Boolean.FALSE);
			OdbConfiguration.setLogAll(Boolean.FALSE);
			// OdbConfiguration.lockObjectsOnSelect(Boolean.TRUE);
			OdbConfiguration.useMultiThread(Boolean.FALSE, 1);
			// OdbConfiguration.setAutomaticallyIncreaseCacheSize(Boolean.TRUE);
			// OdbConfiguration.setAutomaticCloseFileOnExit(Boolean.TRUE);
			OdbConfiguration.setDisplayWarnings(Boolean.TRUE);
			OdbConfiguration.setMultiThreadExclusive(Boolean.TRUE);
			OdbConfiguration.setReconnectObjectsToSession(Boolean.TRUE);
			OdbConfiguration.setUseCache(Boolean.TRUE);
			OdbConfiguration.setUseIndex(Boolean.TRUE);
			OdbConfiguration.setUseMultiBuffer(Boolean.TRUE);
			// OdbConfiguration.setShareSameVmConnectionMultiThread(Boolean.FALSE);
		} finally {
			notifyAll();
		}
	}

	protected synchronized void openDataBase(String dataBaseFile) {
		try {
			logger.info("Opening database : " + dataBaseFile);
			this.odb = ODBFactory.open(dataBaseFile);
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> T persist(T object) {
		try {
			if (object != null) {
				Long idFieldValue = (Long) DatabaseUtilities.getIdFieldValue(object);
				if (idFieldValue == null) {
					DatabaseUtilities.setIdField(object, System.nanoTime());
				}
				// Check for duplicate keys
				Object duplicate = find(object.getClass(), idFieldValue);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> T remove(T t) {
		try {
			if (t != null) {
				Long idFieldValue = (Long) DatabaseUtilities.getIdFieldValue(t);
				Object dbT = find(t.getClass(), idFieldValue);
				if (dbT != null) {
					this.odb.deleteCascade(dbT);
				}
			}
		} catch (Exception e) {
			logger.error("Exception removing object : " + t, e);
		} finally {
			this.odb.commit();
			notifyAll();
		}
		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> T remove(Class<T> klass, Long id) {
		try {
			T t = find(klass, id);
			this.odb.delete(t);
			return t;
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> T merge(T t) {
		try {
			if (t != null) {
				Long idFieldValue = (Long) DatabaseUtilities.getIdFieldValue(t);
				Object dbT = find(t.getClass(), idFieldValue);
				if (dbT != null) {
					OID oid = this.odb.getObjectId(dbT);
					if (oid != null) {
						this.odb.ext().replace(oid, t);
					}
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

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> List<T> find(Class<T> klass, int startIndex, int endIndex) {
		List<T> list = new ArrayList<T>();
		try {
			IQuery query = new CriteriaQuery(klass);
			Objects<T> objects = odb.getObjects(query, Boolean.TRUE, startIndex, endIndex);
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

	/**
	 * {@inheritDoc}
	 */
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

	protected synchronized void close() {
		try {
			logger.info("Closing database : ");
			if (this.odb != null && !this.odb.isClosed()) {
				this.odb.commit();
				this.odb.close();
			}
			this.odb = null;
			this.initialised = Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	/**
	 * Called from Spring to create indexes if necessary.
	 * 
	 * @param indexes
	 *            the indexes to create on the objects that will be persisted in the database
	 */
	public void setIndexes(List<Index> indexes) {
		this.indexes = indexes;
	}

}