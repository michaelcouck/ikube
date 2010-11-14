package ikube.database;

import static org.junit.Assert.assertNotNull;
import ikube.BaseTest;
import ikube.toolkit.ApplicationContextManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neodatis.odb.ODB;

@Ignore
public class DataBaseInjectorTest extends BaseTest {

	private DataBaseOdb dataBase = ApplicationContextManager.getBean(DataBaseOdb.class);
	private DataBaseInjector dataBaseInjector = ApplicationContextManager.getBean(DataBaseInjector.class);

	@Before
	public void before() {
		dataBase.close();
		dataBaseInjector.close();
	}

	@After
	public void after() {
		ODB odb = dataBaseInjector.openLocalDatabase();
		dataBase.setOdb(odb);
	}

	@Test
	public void openLocalDatabase() {
		ODB odb = dataBaseInjector.openLocalDatabase();
		assertNotNull(odb);
		odb.close();
	}

	@Test
	public void openLocalDatabaseServer() {
		ODB odb = dataBaseInjector.openLocalDatabaseServer();
		assertNotNull(odb);
		odb.close();
		dataBaseInjector.close();
	}

	@Test
	public void openRemoteDatabase() {
		ODB localOdbFomeServer = dataBaseInjector.openLocalDatabaseServer();
		assertNotNull(localOdbFomeServer);
		ODB remoteOdbFromServer = dataBaseInjector.openRemoteDatabase();
		assertNotNull(remoteOdbFromServer);
		localOdbFomeServer.close();
		remoteOdbFromServer.close();
		dataBaseInjector.close();
	}

	@Test
	public void openRemoteDatabaseWithIps() {
		ODB localOdbFomeServer = dataBaseInjector.openLocalDatabaseServer();
		assertNotNull(localOdbFomeServer);
		ODB remoteOdbFromServer = dataBaseInjector.openRemoteDatabaseWithIps();
		assertNotNull(remoteOdbFromServer);
		localOdbFomeServer.close();
		remoteOdbFromServer.close();
		dataBaseInjector.close();
	}

	@Test
	public void openRemoteDatabaseWithJndi() {
		ODB localOdbFomeServer = dataBaseInjector.openLocalDatabaseServer();
		assertNotNull(localOdbFomeServer);
		ODB remoteOdbFromServer = dataBaseInjector.openRemoteDatabaseWithJndi();
		assertNotNull(remoteOdbFromServer);
		localOdbFomeServer.close();
		remoteOdbFromServer.close();
		dataBaseInjector.close();
	}

	@Test
	public void postProcessBeanFactory() {
		// ConfigurableListableBeanFactory
		// dataBaseInjector.postProcessBeanFactory(ApplicationContextManager.getApplicationContext());
	}

}
