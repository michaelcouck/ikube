package ikube;

import static org.mockito.Mockito.mock;
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
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class IndexEngineTest extends ATest {

	private Index									index;
	/** Class under test. */
	private IndexEngine								indexEngine;
	private List<IAction<IndexContext<?>, Boolean>>	actions;

	public IndexEngineTest() {
		super(IndexEngineTest.class);
	}

	@Before
	public void before() {
		indexEngine = new IndexEngine();
		actions = new ArrayList<IAction<IndexContext<?>, Boolean>>();

		index = mock(Index.class);
		actions.add(index);
		when(server.getWorking()).thenReturn(Boolean.FALSE);
		when(clusterManager.getServer()).thenReturn(server);

		indexEngine.setActions(actions);
		// indexEngine.setClusterManager(clusterManager);
		Deencapsulation.setField(indexEngine, clusterManager);

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
		Mockito.verify(index, Mockito.atLeast(1)).execute(Mockito.any(IndexContext.class));
		Mockito.verify(index, Mockito.atMost(1)).execute(Mockito.any(IndexContext.class));

		when(server.getWorking()).thenReturn(Boolean.TRUE);
		indexEngine.handleNotification(event);
		Mockito.verify(index, Mockito.atLeast(1)).execute(Mockito.any(IndexContext.class));
		Mockito.verify(index, Mockito.atMost(2)).execute(Mockito.any(IndexContext.class));
	}

}