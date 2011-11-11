package ikube.action;

import static org.junit.Assert.assertNotNull;
import ikube.ATest;
import ikube.cluster.IClusterManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ActionTest extends ATest {

	private Action<?, ?> action;

	public ActionTest() {
		super(ActionTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
		action = new Action<Object, Object>() {
			@Override
			public Object execute(Object context) throws Exception {
				return null;
			}
		};
		action.setClusterManager(clusterManager);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void executeAllMethods() {
		action.setRuleExpression("predicate");
		action.getRuleExpression();
		action.setRules(null);
		action.getRules();
		IClusterManager clusterManager = action.getClusterManager();
		assertNotNull("This should be the mocked cluster manager : ", clusterManager);
		action.sendNotification("subject", "body");
	}

}