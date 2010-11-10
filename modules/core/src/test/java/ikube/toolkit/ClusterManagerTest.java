package ikube.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.database.IDataBase;
import ikube.model.Server;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ClusterManagerTest extends BaseTest {

	private String actionName = "actionName";
	IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);

	@Before
	public void before() {
		delete(dataBase, Server.class);
	}

	@Test
	public void getServer() {
		// IndexContext
		List<Server> servers = dataBase.find(Server.class, 0, Integer.MAX_VALUE);
		assertTrue(servers.size() == 0);
		Server server = ClusterManager.getServer(indexContext);
		assertNotNull(server);
		Server workingAgain = ClusterManager.getServer(indexContext);
		assertEquals(server, workingAgain);
	}

	@Test
	public void setServer() {
		// Working, Boolean
		ClusterManager.setWorking(indexContext, actionName, Boolean.TRUE);
		boolean isWorking = ClusterManager.isWorking(indexContext);
		assertTrue(isWorking);
		ClusterManager.setWorking(indexContext, actionName, Boolean.FALSE);
	}

	@Test
	public void isWorking() {
		// IndexContext
		boolean isWorking = ClusterManager.isWorking(indexContext);
		assertFalse(isWorking);
		ClusterManager.setWorking(indexContext, actionName, Boolean.TRUE);
		isWorking = ClusterManager.isWorking(indexContext);
		assertTrue(isWorking);
		ClusterManager.setWorking(indexContext, actionName, Boolean.FALSE);
	}

}
