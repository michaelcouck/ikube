package ikube.cluster.hzc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.listener.Event;
import ikube.listener.ListenerManager;
import ikube.model.Action;
import ikube.model.Server;
import ikube.service.IMonitorService;
import ikube.service.MonitorService;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class ClusterManagerHazelcastTest extends ATest {

	@MockClass(realClass = Hazelcast.class)
	public static class HazelcastMock {

		@SuppressWarnings("unchecked")
		static IMap<String, Server> servers = mock(IMap.class);

		@Mock
		@SuppressWarnings("unchecked")
		public static <K, V> IMap<K, V> getMap(String name) {
			return (IMap<K, V>) servers;
		}
	}

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private Server server;
	private Map.Entry<String, Server> entry;
	private Set<Map.Entry<String, Server>> entrySet;

	private String actionName = "actionName";
	private String indexName = "indexName";
	private String indexableName = "indexableName";

	@Cascading
	private IDataBase dataBase;
	@Cascading
	private IMonitorService monitorService;

	private StartListener startListener;
	private StopListener stopListener;

	private ClusterManagerHazelcast clusterManagerHazelcast;

	public ClusterManagerHazelcastTest() {
		super(ClusterManagerHazelcastTest.class);
	}

	@Before
	@SuppressWarnings("unchecked")
	public void before() {
		Mockit.setUpMocks();
		Mockit.setUpMocks(HazelcastMock.class);
		clusterManagerHazelcast = new ClusterManagerHazelcast();

		server = mock(Server.class);
		entry = mock(Map.Entry.class);
		startListener = Mockito.mock(StartListener.class);
		stopListener = Mockito.mock(StopListener.class);
		entrySet = new HashSet<Map.Entry<String, Server>>();

		when(server.isWorking()).thenReturn(false);
		when(entry.getValue()).thenReturn(server);
		when(HazelcastMock.servers.entrySet()).thenReturn(entrySet);
		when(HazelcastMock.servers.get(anyString())).thenReturn(server);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		clusterManagerHazelcast.unlock(IConstants.IKUBE);
	}

	@Test
	public void lock() throws Exception {
		injectServices();
		boolean gotLock = clusterManagerHazelcast.lock(IConstants.IKUBE);
		assertTrue(gotLock);
		Thread thread = new Thread(new Runnable() {
			public void run() {
				boolean gotLock = clusterManagerHazelcast.lock(IConstants.IKUBE);
				assertFalse(gotLock);
			}
		});
		thread.start();
		thread.join();
	}

	@Test
	public void unlock() throws Exception {
		injectServices();
		boolean gotLock = clusterManagerHazelcast.lock(IConstants.IKUBE);
		assertTrue(gotLock);
		Thread thread = new Thread(new Runnable() {
			public void run() {
				boolean gotLock = clusterManagerHazelcast.lock(IConstants.IKUBE);
				assertFalse(gotLock);
			}
		});
		thread.start();
		Thread.sleep(1000);

		clusterManagerHazelcast.unlock(IConstants.IKUBE);
		thread = new Thread(new Runnable() {
			public void run() {
				boolean gotLock = clusterManagerHazelcast.lock(IConstants.IKUBE);
				assertTrue(gotLock);
			}
		});
		thread.start();
		thread.join();
		Thread.sleep(1000);
	}

	@Test
	public void anyWorking() {
		injectServices();
		boolean anyworking = clusterManagerHazelcast.anyWorking();
		assertFalse(anyworking);

		entrySet.add(entry);
		anyworking = clusterManagerHazelcast.anyWorking();
		assertFalse(anyworking);

		when(server.isWorking()).thenReturn(true);
		anyworking = clusterManagerHazelcast.anyWorking();
		assertTrue(anyworking);
	}

	@Test
	public void anyWorkingIndex() {
		injectServices();
		boolean anyWorkingIndex = clusterManagerHazelcast.anyWorking(indexName);
		assertFalse(anyWorkingIndex);

		Action action = mock(Action.class);
		when(action.getIndexName()).thenReturn(indexName);
		when(server.getActions()).thenReturn(Arrays.asList(action));

		entrySet.add(entry);
		when(server.isWorking()).thenReturn(true);
		anyWorkingIndex = clusterManagerHazelcast.anyWorking(indexName);
		assertTrue(anyWorkingIndex);
	}

	@Test
	public void startWorking() {
		injectServices();
		Action action = clusterManagerHazelcast.startWorking(actionName, indexName, indexableName);
		assertEquals(indexName, action.getIndexName());
	}

	@Test
	public void stopWorking() {
		injectServices();
		Action action = mock(Action.class);
		when(action.getStartTime()).thenReturn(new Date());
		when(action.getEndTime()).thenReturn(new Date());
		when(action.getIndexName()).thenReturn(indexName);
		when(server.getActions()).thenReturn(new ArrayList<Action>(Arrays.asList(action)));

		clusterManagerHazelcast.stopWorking(action);
		assertNotNull(action.getEndTime());

		assertEquals(0, server.getActions().size());
	}

	@Test
	public void getServerAndServers() {
		injectServices();
		Mockit.tearDownMocks();
		monitorService = mock(MonitorService.class);
		Deencapsulation.setField(clusterManagerHazelcast, monitorService);
		Server server = clusterManagerHazelcast.getServer();
		assertNotNull(server);
		Map<String, Server> servers = clusterManagerHazelcast.getServers();
		assertNotNull(servers);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void sendMessage() {
		injectServices();
		clusterManagerHazelcast.initialize();
		MessageListener messageListener = Mockito.mock(MessageListener.class);
		Hazelcast.getTopic(IConstants.TOPIC).addMessageListener(messageListener);
		clusterManagerHazelcast.sendMessage(new Event());
		ThreadUtilities.sleep(1000);
		Mockito.verify(messageListener, Mockito.atLeastOnce()).onMessage(Mockito.any(Message.class));
	}

	@Test
	public void submitDestroy() {
		Mockit.tearDownMocks();
		Deencapsulation.setField(clusterManagerHazelcast, new StartListener());
		Deencapsulation.setField(clusterManagerHazelcast, new StopListener());
		ThreadUtilities.initialize();
		clusterManagerHazelcast.initialize();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (true) {
					ThreadUtilities.sleep(1000);
				}
			}
		};
		String name = "name";
		Future<?> future = ThreadUtilities.submit(name, runnable);
		logger.info("Future : " + future.isCancelled() + ", " + future.isDone());

		Event event = ListenerManager.getEvent(Event.TERMINATE, System.currentTimeMillis(), name, Boolean.FALSE);
		clusterManagerHazelcast.sendMessage(event);
		ThreadUtilities.sleep(1000);
		assertTrue(future.isCancelled());
		ThreadUtilities.destroy();
	}

	@Test
	public void getLocks() {
		// Not implemented
	}

	@Test
	public void threaded() {
		injectServices();
		Hazelcast.getLock(IConstants.IKUBE).forceUnlock();
		ThreadUtilities.initialize();
		int threads = 3;
		final int iterations = 100;
		final double sleep = 10;
		final Boolean[] locks = new Boolean[threads];
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (int i = 0; i < threads; i++) {
			final int thread = i;
			Runnable runnable = new Runnable() {
				public void run() {
					for (int i = 0; i < iterations; i++) {
						boolean lock = clusterManagerHazelcast.lock(IConstants.IKUBE);
						locks[thread] = lock;
						validate(locks);
						clusterManagerHazelcast.unlock(IConstants.IKUBE);
						locks[thread] = false;
						ThreadUtilities.sleep((long) (sleep * Math.random()));
					}
				}
			};
			Future<?> future = ThreadUtilities.submit(runnable);
			futures.add(future);
		}
		ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
		ThreadUtilities.destroy();
	}

	private void injectServices() {
		Deencapsulation.setField(clusterManagerHazelcast, dataBase);
		Deencapsulation.setField(clusterManagerHazelcast, monitorService);
		Deencapsulation.setField(clusterManagerHazelcast, startListener);
		Deencapsulation.setField(clusterManagerHazelcast, stopListener);
		clusterManagerHazelcast.initialize();
	}

	private void validate(final Boolean[] locks) {
		int count = 0;
		// logger.info("Validate ; " + Arrays.deepToString(locks));
		for (Boolean lock : locks) {
			count = lock == null ? count : lock ? count + 1 : count;
		}
		assertTrue(count <= 1);
	}

}