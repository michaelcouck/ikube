package ikube.action;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.index.IndexManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.toolkit.ApplicationContextManager;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ResetTest extends ATest {

	private transient final Reset reset = new Reset();

	public ResetTest() {
		super(ResetTest.class);
	}

	@Before
	public void before() {
		// when()
		ApplicationContextManagerMock.INDEX_CONTEXT = INDEX_CONTEXT;
		ApplicationContextManagerMock.CLUSTER_MANAGER = CLUSTER_MANAGER;
		Mockit.setUpMocks(ApplicationContextManagerMock.class, IndexManagerMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ApplicationContextManager.class, IndexManager.class);
	}

	@Test
	public void execute() {
		boolean result = reset.execute(INDEX_CONTEXT);
		assertTrue(result);
	}

}
