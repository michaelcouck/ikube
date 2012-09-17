package ikube;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.action.IAction;
import ikube.action.Index;
import ikube.listener.Event;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import java.util.ArrayList;
import java.util.List;

import mockit.Deencapsulation;
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

	private Index index;
	/** Class under test. */
	private IndexEngine indexEngine;
	private List<IAction<IndexContext<?>, Boolean>> actions;

	public IndexEngineTest() {
		super(IndexEngineTest.class);
	}

	@Before
	public void before() {
		indexEngine = new IndexEngine();
		actions = new ArrayList<IAction<IndexContext<?>, Boolean>>();

		index = mock(Index.class);
		actions.add(index);
		when(server.isWorking()).thenReturn(Boolean.FALSE);
		when(clusterManager.getServer()).thenReturn(server);

		// indexEngine.setActions(actions);
		Deencapsulation.setField(indexEngine, "actions", actions);
		// indexEngine.setClusterManager(clusterManager);
		// Deencapsulation.setField(indexEngine, clusterManager);

		Mockit.setUpMocks(ApplicationContextManagerMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ApplicationContextManager.class);
	}

	@Test
	public void handleNotification() throws Exception {
		Event event = new Event();
		event.setTimestamp(System.currentTimeMillis());
		event.setType(Event.TIMER);
		indexEngine.handleNotification(event);
		verify(index, atLeast(1)).execute(any(IndexContext.class));
		verify(index, atMost(1)).execute(any(IndexContext.class));

		when(server.isWorking()).thenReturn(Boolean.TRUE);
		indexEngine.handleNotification(event);
		verify(index, atLeast(1)).execute(any(IndexContext.class));
		verify(index, atMost(2)).execute(any(IndexContext.class));
	}

}