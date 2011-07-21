package ikube;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import ikube.action.IAction;
import ikube.action.Process;
import ikube.listener.Event;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import java.util.ArrayList;
import java.util.List;

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class IndexEngineTest extends ATest {

	private Process process;
	private boolean invoked;
	/** Class under test. */
	private IndexEngine indexEngine;
	private List<IAction<IndexContext<?>, Boolean>> actions;

	public IndexEngineTest() {
		super(IndexEngineTest.class);
	}

	@Before
	public void before() {
		process = spy(new Process());
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				logger.info("Invocation : " + invocation);
				invoked = Boolean.TRUE;
				return null;
			}
		}).when(process).execute(any(IndexContext.class));
		when(SERVER.getWorking()).thenReturn(Boolean.FALSE);
		when(CLUSTER_MANAGER.getServer()).thenReturn(SERVER);
		Mockit.setUpMocks(ApplicationContextManagerMock.class);
		actions = new ArrayList<IAction<IndexContext<?>, Boolean>>();
		actions.add(process);
		indexEngine = new IndexEngine();
		indexEngine.setActions(actions);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ApplicationContextManager.class);
	}

	@Test
	public void handleNotification() {
		Event event = new Event();
		event.setTimestamp(System.currentTimeMillis());
		event.setType(Event.TIMER);
		indexEngine.handleNotification(event);
		assertTrue("The process action should be invoked : ", invoked);
		invoked = Boolean.FALSE;

		when(SERVER.getWorking()).thenReturn(Boolean.TRUE);
		indexEngine.handleNotification(event);
		assertTrue("The process action should be invoked : ", invoked);
	}

	public static void main(String[] args) {
		new IndexEngineTest().handleNotification();
	}

}
