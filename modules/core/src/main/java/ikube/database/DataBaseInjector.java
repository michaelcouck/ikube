package ikube.database;

import ikube.IConstants;
import ikube.model.Database;
import ikube.model.Ip;
import ikube.toolkit.InitialContextFactory;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.ODBServer;
import org.neodatis.odb.OdbConfiguration;
import org.neodatis.tool.DLogger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;

/**
 * @author Michael Couck
 * @since 11.11.10
 * @version 01.00
 */
public class DataBaseInjector implements BeanFactoryPostProcessor, Ordered {

	private Logger logger;
	private List<Ip> ips;
	private boolean local;
	private ODBServer server;

	public DataBaseInjector() {
		this.logger = Logger.getLogger(this.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		logger.info("Processing beans : ");
		configureDatabase();
		ODB odb = null;
		if (local) {
			odb = openLocalDatabase();
		} else {
			odb = openRemoteDatabase();
		}
		if (odb == null) {
			odb = openLocalDatabase();
		}
		logger.info("Got ODB : " + odb);
		String[] beanNames = beanFactory.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			Object bean = beanFactory.getBean(beanName);
			logger.debug("Processing bean : " + bean);
			Method[] methods = bean.getClass().getDeclaredMethods();
			for (Method method : methods) {
				try {
					Injection injection = method.getAnnotation(Injection.class);
					if (injection == null) {
						continue;
					}
					Class<?> type = injection.type();
					if (!type.isAssignableFrom(odb.getClass())) {
						continue;
					}
					logger.debug("Injecting bean : " + bean + ", with : " + odb + ", method : " + method);
					method.invoke(bean, new Object[] { odb });
				} catch (Exception e) {
					logger.error("Exception setting the property on method : " + method + ", on bean : " + bean, e);
				}
			}
		}
	}

	protected ODB openLocalDatabase() {
		try {
			logger.info("Trying to open the database on the file system : ");
			// Finally just get a file database if we can't connect to a server database
			ODB odb = ODBFactory.open(IConstants.DATABASE_FILE);
			logger.info("Finally got the database open locally. This is not an ideal situation!");
			return odb;
		} catch (Exception e) {
			logger.error("Couldn't open the database on the file system! System not operational!", e);
		}
		return null;
	}

	protected ODB openRemoteDatabase() {
		ODB odb = openRemoteDatabaseWithIps();
		if (odb != null) {
			return odb;
		}
		odb = openRemoteDatabaseWithJndi();
		if (odb != null) {
			return odb;
		}
		odb = openLocalDatabaseServer();
		return odb;
	}

	protected ODB openRemoteDatabaseWithIps() {
		// Find the databases defined in the configuration, this will be the
		// ip addresses of the other servers. We check them first to see if they have
		// a database open
		ODB odb = null;
		for (Ip ip : ips) {
			try {
				logger.info("Trying ip : " + ip);
				odb = ODBFactory.openClient(ip.getIp(), IConstants.PORT, IConstants.DATABASE);
				if (odb != null) {
					break;
				}
			} catch (Exception e) {
				logger.warn("Couldn't find database at : " + ip + ", reason : " + e.getMessage());
			}
		}
		return odb;
	}

	protected ODB openRemoteDatabaseWithJndi() {
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
				ODB odb = ODBFactory.openClient(ip, port, IConstants.DATABASE);
				logger.info("Got JNDI database : " + database + ", " + odb);
				return odb;
			}
		} catch (NameNotFoundException e) {
			logger.info("Database not found in JNDI, will open on localhost : " + e.getMessage());
		} catch (Exception e) {
			logger.info("Couldn't open database on ip, will try top open on localhost : ", e);
		}
		return null;
	}

	protected ODB openLocalDatabaseServer() {
		try {
			logger.info("Trying to open database server on localhost : ");
			if (server == null) {
				// Try to open the server and connect to it on this host
				server = ODBFactory.openServer(IConstants.PORT);
				server.addBase(IConstants.DATABASE, IConstants.DATABASE_FILE);
				server.startServer(Boolean.TRUE);
			}
			String ip = InetAddress.getLocalHost().getHostAddress();
			ODB odb = ODBFactory.openClient(ip, IConstants.PORT, IConstants.DATABASE);
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
			logger.info("Opened the database server on the localhost : " + database);
			return odb;
		} catch (Exception e) {
			logger.warn("Couldn't open server and client, will try to open database on local file : ", e);
		}
		return null;
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

	public void setIps(List<Ip> ips) {
		this.ips = ips;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public void close() {
		if (this.server != null) {
			try {
				this.server.close();
				this.server = null;
			} catch (Exception e) {
				logger.error("Exception closing the database server : ", e);
			}
		}
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
