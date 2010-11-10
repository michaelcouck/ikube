package ikube.database;

import ikube.IConstants;
import ikube.model.Database;
import ikube.model.Lock;
import ikube.toolkit.InitialContextFactory;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.persistence.Id;

import org.apache.log4j.Logger;
import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.ODBServer;
import org.neodatis.odb.OID;
import org.neodatis.odb.Objects;
import org.neodatis.odb.OdbConfiguration;
import org.neodatis.odb.core.query.IQuery;
import org.neodatis.odb.core.query.criteria.And;
import org.neodatis.odb.core.query.criteria.ICriterion;
import org.neodatis.odb.core.query.criteria.Where;
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery;
import org.neodatis.tool.DLogger;

public class DataBaseOdb implements IDataBase {

	private Logger logger;

	private ODB odb;
	private ODBServer server;

	private long lockTimeout = 3000;
	private Map<Class<?>, Field> idFields;

	public DataBaseOdb(boolean local) {
		this.logger = Logger.getLogger(this.getClass());
		this.idFields = new HashMap<Class<?>, Field>();
		configureDatabase();
		openDatabase(local);
	}

	private void openDatabase(boolean local) {
		try {
			String initialContextFactory = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
			if (initialContextFactory == null) {
				// This should be taken out and put somewhere in the configuration
				System.setProperty(Context.INITIAL_CONTEXT_FACTORY, InitialContextFactory.class.getName());
			}
			logger.info("Using initial context factory : " + System.getProperty(Context.INITIAL_CONTEXT_FACTORY));
			logger.info("Looking for database in JNDI : " + IConstants.DATABASE);
			// Try to find a database connection in JNDI
			Database database = (Database) new InitialContext().lookup(IConstants.DATABASE);
			if (database != null) {
				String ip = database.getIp();
				int port = database.getPort();
				this.odb = ODBFactory.openClient(ip, port, IConstants.DATABASE);
				logger.info("Got JNDI database : " + database + ", " + this.odb);
			}
		} catch (NameNotFoundException e) {
			logger.info("Database not found in JNDI, will open on localhost : " + e.getMessage());
		} catch (Exception e) {
			logger.info("Couldn't open database on ip, will try top open on localhost : ", e);
		}
		if (this.odb == null) {
			try {
				logger.info("Trying to open database server on localhost : ");
				// Try to open the server and connect to it on this host
				this.server = ODBFactory.openServer(IConstants.PORT);
				this.server.addBase(IConstants.DATABASE, IConstants.DATABASE_FILE);
				this.server.startServer(Boolean.TRUE);
				String ip = InetAddress.getLocalHost().getHostAddress();
				this.odb = ODBFactory.openClient(ip, IConstants.PORT, IConstants.DATABASE);
				// Publish the database in JNDI
				Database database = new Database();
				database.setIp(ip);
				database.setPort(IConstants.PORT);
				database.setStart(new Timestamp(System.currentTimeMillis()));
				try {
					new InitialContext().rebind(IConstants.DATABASE, database);
					logger.info("Published the database to JNDI : " + database);
				} catch (Exception e) {
					logger.error("Exception publishing the database to JNDI : ", e);
				}
				logger.info("Opened the database no the localhost : " + database);
			} catch (Exception e) {
				logger.warn("Couldn't open server and client, will try to open database on local file : ", e);
			}
		}
		if (this.odb == null) {
			try {
				logger.info("Trying to open the database on the file system : ");
				// Finally just get a file database if we can't connect to a server database
				this.odb = ODBFactory.open(IConstants.DATABASE_FILE);
				logger.info("Finally got the database open locally. This is not an ideal situation!");
			} catch (Exception e) {
				logger.error("Couldn't open the database on the file system! System not operational!", e);
			}
		}
	}

	private void configureDatabase() {
		DLogger.register(new ikube.database.Logger());
		OdbConfiguration.setDebugEnabled(Boolean.FALSE);
		OdbConfiguration.setDebugEnabled(5, Boolean.FALSE);
		OdbConfiguration.setLogAll(Boolean.FALSE);
		// OdbConfiguration.lockObjectsOnSelect(Boolean.TRUE);
		// OdbConfiguration.useMultiThread(Boolean.FALSE, 1);
		// OdbConfiguration.setAutomaticallyIncreaseCacheSize(Boolean.TRUE);
		// OdbConfiguration.setAutomaticCloseFileOnExit(Boolean.TRUE);
		OdbConfiguration.setDisplayWarnings(Boolean.FALSE);
		// OdbConfiguration.setMultiThreadExclusive(Boolean.TRUE);
		// OdbConfiguration.setReconnectObjectsToSession(Boolean.TRUE);
		OdbConfiguration.setUseCache(Boolean.TRUE);
		OdbConfiguration.setUseIndex(Boolean.TRUE);
		OdbConfiguration.setUseMultiBuffer(Boolean.FALSE);
		OdbConfiguration.setShareSameVmConnectionMultiThread(Boolean.FALSE);
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
	public <T> T find(Class<T> klass, Long id) {
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
			this.odb.commit();
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
			this.odb.commit();
			notifyAll();
		}
		return null;
	}

	@Override
	public <T> List<T> find(Class<T> klass, int firstResult, int maxResults) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		return find(klass, parameters, firstResult, maxResults);
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
			this.odb.commit();
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

	@Override
	public synchronized Lock lock(Class<?> klass) {
		Lock lock = null;
		try {
			String className = klass.getName();
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put(IConstants.CLASS_NAME, className);
			lock = find(Lock.class, parameters, Boolean.TRUE);
			if (lock == null) {
				lock = new Lock();
				lock.setClassName(className);
				lock.setLocked(Boolean.TRUE);
				persist(lock);
			} else {
				while (lock.isLocked()) {
					try {
						// logger.debug("Going into wait : " + Thread.currentThread().hashCode() + ", " + lock);
						notifyAll();
						wait(100);
					} catch (InterruptedException e) {
						logger.error("Interrupted : ", e);
					}
					lock = find(Lock.class, parameters, Boolean.TRUE);
					// logger.debug("Woken up : " + Thread.currentThread().hashCode() + ", " + lock);
					if (lock.isLocked() && System.currentTimeMillis() - lock.getStart() > lockTimeout) {
						break;
					}
				}
				lock.setStart(System.currentTimeMillis());
				lock.setLocked(Boolean.TRUE);
				merge(lock);
			}
		} finally {
			// logger.debug("Notifying : " + Thread.currentThread().hashCode() + ", " + lock);
			notifyAll();
		}
		return lock;
	}

	@Override
	public synchronized void release(Lock lock) {
		try {
			// logger.debug("Releasing : " + Thread.currentThread().hashCode());
			if (lock != null) {
				lock.setLocked(Boolean.FALSE);
				merge(lock);
			}
		} finally {
			// logger.debug("Notifying : " + Thread.currentThread().hashCode() + ", " + lock);
			notifyAll();
		}
	}

	protected synchronized void close() {
		try {
			if (this.odb != null && !this.odb.isClosed()) {
				this.odb.commit();
				this.odb.close();
			}
			if (this.server != null) {
				this.server.close();
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

}
