package ikube.mock;

import static org.mockito.Mockito.mock;
import ikube.cluster.hzc.ClusterManagerHazelcast;
import ikube.model.Action;
import ikube.model.Server;
import mockit.Mock;
import mockit.MockClass;

import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 29.04.11
 * @version 01.00
 */
@MockClass(realClass = ClusterManagerHazelcast.class)
public class ClusterManagerMock {

	@Mock()
	public Server getServer() {
		return mock(Server.class);
	}

	@Mock()
	public synchronized Action startWorking(final String actionName, final String indexName, final String indexableName) {
		return Mockito.mock(Action.class);
	}

	@Mock()
	public boolean lock(String name) {
		return Boolean.TRUE;
	}

}
