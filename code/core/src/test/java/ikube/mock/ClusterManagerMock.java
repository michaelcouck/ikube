package ikube.mock;

import static org.mockito.Mockito.mock;
import ikube.cluster.jms.ClusterManagerJms;
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
@MockClass(realClass = ClusterManagerJms.class)
public class ClusterManagerMock {

	@Mock()
	public Server getServer() {
		return mock(Server.class);
	}

	@Mock()
	@SuppressWarnings("unused")
	public synchronized Action startWorking(final String actionName, final String indexName, final String indexableName) {
		return Mockito.mock(Action.class);
	}

	@Mock()
	@SuppressWarnings("unused")
	public boolean lock(String name) {
		return Boolean.TRUE;
	}

}
