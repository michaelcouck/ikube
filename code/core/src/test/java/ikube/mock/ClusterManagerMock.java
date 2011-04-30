package ikube.mock;

import ikube.index.IndexManager;
import mockit.Mock;
import mockit.MockClass;

/**
 * @author Michael Couck
 * @since 29.04.11
 * @version 01.00
 */
@MockClass(realClass = IndexManager.class)
public class ClusterManagerMock {

	@Mock()
	public synchronized long setWorking(final String indexName, final String indexableName, final boolean isWorking) {
		return System.currentTimeMillis();
	}

}
