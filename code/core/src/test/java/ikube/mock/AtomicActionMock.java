package ikube.mock;

import ikube.cluster.AtomicAction;
import mockit.Mock;
import mockit.MockClass;

import com.hazelcast.core.ILock;

/**
 * @author Michael Couck
 * @since 12.06.2011
 * @version 01.00
 */
@MockClass(realClass = AtomicAction.class)
public class AtomicActionMock {

	public static int INVOCATIONS = 0;

	@Mock()
	public static ILock lock(String lockName) {
		INVOCATIONS++;
		return null;
	}

}
