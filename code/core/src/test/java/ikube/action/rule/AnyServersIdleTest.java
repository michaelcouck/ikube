package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.model.Server;

import java.util.HashMap;
import java.util.Map;

import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 29.03.2011
 * @version 01.00
 */
public class AnyServersIdleTest extends AbstractTest {

	private AnyServersIdle anyServersIdle;

	@Before
	public void before() {
		anyServersIdle = new AnyServersIdle();
		Deencapsulation.setField(anyServersIdle, clusterManager);
	}

	@Test
	public void evaluate() {
		Map<String, Server> servers = new HashMap<String, Server>();

		Server thisServer = Mockito.mock(Server.class);
		Server server = Mockito.mock(Server.class);

		Mockito.when(thisServer.getAddress()).thenReturn("local-server");
		Mockito.when(server.getAddress()).thenReturn("remote-server");

		Mockito.when(server.isWorking()).thenReturn(Boolean.FALSE);
		servers.put("local-server", thisServer);
		servers.put("remote-server", server);

		Mockito.when(clusterManager.getServer()).thenReturn(thisServer);
		Mockito.when(clusterManager.getServers()).thenReturn(servers);
		boolean anyIdle = anyServersIdle.evaluate(indexContext);
		assertTrue(anyIdle);

		Mockito.when(server.isWorking()).thenReturn(Boolean.TRUE);
		anyIdle = anyServersIdle.evaluate(indexContext);
		assertFalse(anyIdle);

	}

}