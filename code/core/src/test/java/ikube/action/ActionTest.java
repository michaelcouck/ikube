package ikube.action;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import ikube.AbstractTest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.model.IndexContext;
import ikube.toolkit.IMailer;
import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 16.01.12
 * @version 01.00
 */
public class ActionTest extends AbstractTest {
	
	private Action<?, ?> action;

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
		action = new Action<Object, Object>() {
			@Override
			public Boolean execute(IndexContext<?> context) throws Exception {
				return null;
			}

			@Override
			public boolean internalExecute(IndexContext<?> indexContext) throws Exception {
				return false;
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
		IMailer mailer = mock(IMailer.class);
		Deencapsulation.setField(action, mailer);
		assertNotNull("This should be the mocked cluster manager : ", clusterManager);
		action.sendNotification("subject", "body");
		Mockito.verify(mailer, Mockito.atLeast(1)).sendMail(Mockito.any(String.class), Mockito.any(String.class));
	}

}