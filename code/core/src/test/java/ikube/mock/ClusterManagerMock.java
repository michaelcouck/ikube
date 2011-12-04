package ikube.mock;

import static org.mockito.Mockito.mock;
import ikube.cluster.jms.ClusterManagerJms;
import ikube.model.Server;
import mockit.Mock;
import mockit.MockClass;

/**
 * @author Michael Couck
 * @since 29.04.11
 * @version 01.00
 */
@MockClass(realClass = ClusterManagerJms.class)
public class ClusterManagerMock {

	@Mock()
	public Server getServer() {
		return mock(Server.class);
	}

	@Mock()
	public synchronized long startWorking(final String actionName, final String indexName, final String indexableName) {
		return System.currentTimeMillis();
	}

	@Mock()
	public boolean lock(String name) {
		return Boolean.TRUE;
	}

}
