package ikube.action;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import ikube.ATest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.notify.IMailer;
import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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
		Deencapsulation.setField(action, clusterManager);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void executeAllMethods() throws Exception {
		action.setRuleExpression("predicate");
		action.getRuleExpression();
		action.setRules(null);
		action.getRules();
		// IClusterManager clusterManager = action.getClusterManager();
		IMailer mailer = mock(IMailer.class);
		Deencapsulation.setField(action, mailer);
		assertNotNull("This should be the mocked cluster manager : ", clusterManager);
		action.sendNotification("subject", "body");
		Mockito.verify(mailer, Mockito.atLeast(1)).sendMail(Mockito.any(String.class), Mockito.any(String.class));
	}

}