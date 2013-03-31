package ikube.scheduling;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.action.IAction;
import ikube.action.Index;
import ikube.cluster.IMonitorService;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.scheduling.listener.Event;
import ikube.scheduling.listener.IndexEngine;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private IMonitorService monitorService;
	private List<IAction<IndexContext<?>, Boolean>> actions;

	public IndexEngineTest() {
		super(IndexEngineTest.class);
	}

	@Before
	@SuppressWarnings("rawtypes")
	public void before() {
		new ThreadUtilities().initialize();
		indexEngine = new IndexEngine();
		actions = new ArrayList<IAction<IndexContext<?>, Boolean>>();

		index = mock(Index.class);
		monitorService = mock(IMonitorService.class);
		Map<String, IndexContext> indexContexts = new HashMap<String, IndexContext>();
		indexContexts.put(indexContext.getName(), indexContext);
		when(monitorService.getIndexContexts()).thenReturn(indexContexts);
		actions.add(index);
		when(server.isWorking()).thenReturn(Boolean.FALSE);
		when(clusterManager.getServer()).thenReturn(server);

		Deencapsulation.setField(indexEngine, "actions", actions);
		Deencapsulation.setField(indexEngine, monitorService);

		Mockit.setUpMocks(ApplicationContextManagerMock.class);
	}

	@After
	public void after() {
		new ThreadUtilities().destroy();
		Mockit.tearDownMocks(ApplicationContextManager.class);
	}

	@Test
	public void handleNotification() throws Exception {
		Event event = new Event();
		event.setTimestamp(System.currentTimeMillis());
		event.setType(Event.TIMER);

		indexEngine.run();

		verify(index, atLeast(1)).execute(any(IndexContext.class));
		verify(index, atMost(1)).execute(any(IndexContext.class));

		when(server.isWorking()).thenReturn(Boolean.TRUE);
		indexEngine.run();

		verify(index, atLeast(1)).execute(any(IndexContext.class));
		verify(index, atMost(2)).execute(any(IndexContext.class));
	}

}