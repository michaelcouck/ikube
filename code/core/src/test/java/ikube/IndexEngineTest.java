package ikube;

import static org.mockito.Mockito.when;
import ikube.action.IAction;
import ikube.listener.Event;
import ikube.action.Process;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import java.util.ArrayList;
import java.util.List;

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class IndexEngineTest extends ATest {

	public IndexEngineTest() {
		super(IndexEngineTest.class);
	}

	@Before
	public void before() {
		when(SERVER.getWorking()).thenReturn(Boolean.FALSE);
		when(CLUSTER_MANAGER.getServer()).thenReturn(SERVER);
		Mockit.setUpMocks(ApplicationContextManagerMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ApplicationContextManager.class);
	}

	@Test
	public void handleNotification() {
		IndexEngine indexEngine = new IndexEngine();
		List<IAction<IndexContext, Boolean>> actions = new ArrayList<IAction<IndexContext, Boolean>>();
		actions.add(new Process());
		indexEngine.setActions(actions);

		Event event = new Event();
		event.setTimestamp(System.currentTimeMillis());
		event.setType(Event.TIMER);
		indexEngine.handleNotification(event);
	}

	public static void main(String[] args) {
		new IndexEngineTest().handleNotification();
	}

}
