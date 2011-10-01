package ikube.mock;

import static org.mockito.Mockito.mock;
import ikube.cluster.ClusterManager;
import ikube.model.Server;
import mockit.Mock;
import mockit.MockClass;

/**
 * @author Michael Couck
 * @since 29.04.11
 * @version 01.00
 */
@MockClass(realClass = ClusterManager.class)
public class ClusterManagerMock {

	private Server	server	= mock(Server.class);

	@Mock()
	public Server getServer() {
		return server;
	}

	@Mock()
	public synchronized long startWorking(final String actionName, final String indexName, final String indexableName) {
		return System.currentTimeMillis();
	}

	@Mock()
	public <T> void set(String name, Long id, T object) {
		// Do nothing
	}

}
