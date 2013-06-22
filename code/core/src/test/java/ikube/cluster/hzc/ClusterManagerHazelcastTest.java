package ikube.cluster.hzc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.cluster.IMonitorService;
import ikube.cluster.MonitorService;
import ikube.cluster.listener.IListener;
import ikube.cluster.listener.hzc.StartListener;
import ikube.cluster.listener.hzc.StopListener;
import ikube.database.IDataBase;
import ikube.mock.SpellingCheckerMock;
import ikube.model.Action;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.scheduling.schedule.Event;
import ikube.toolkit.ThreadUtilities;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.impl.LockProxyImpl;

/**
 * @author Michael Couck
 * @since 17.04.11
 * @version 01.00
 */
public class ClusterManagerHazelcastTest extends AbstractTest {

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

	@MockClass(realClass = LockProxyImpl.class)
	public static class LockProxyImplMock {

		static boolean locked = false;

		@Mock
		public synchronized boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException {
			locked = !locked;
			System.err.println("LockProxyImplMock : " + locked);
			return locked;
		}
	}

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
	@Cascading
	private Snapshot snapshot;

	private StartListener startListener;
	private StopListener stopListener;

	private ClusterManagerHazelcast clusterManagerHazelcast;

	@Before
	@SuppressWarnings("unchecked")
	public void before() {
		Mockit.setUpMocks(HazelcastMock.class);
		clusterManagerHazelcast = new ClusterManagerHazelcast();

		server = new Server();
		entry = mock(Map.Entry.class);
		entrySet = new HashSet<Map.Entry<String, Server>>();

		when(entry.getValue()).thenReturn(server);
		when(HazelcastMock.servers.entrySet()).thenReturn(entrySet);
		when(HazelcastMock.servers.get(anyString())).thenReturn(server);

		Deencapsulation.setField(clusterManagerHazelcast, server);
		Deencapsulation.setField(clusterManagerHazelcast, dataBase);
		Deencapsulation.setField(clusterManagerHazelcast, monitorService);

		startListener = new StartListener();
		stopListener = new StopListener();
		List<MessageListener<Object>> listeners = new ArrayList<MessageListener<Object>>(Arrays.asList(startListener, stopListener));
		clusterManagerHazelcast.setListeners(listeners);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(HazelcastMock.class);
		clusterManagerHazelcast.unlock(IConstants.IKUBE);
	}

	@Test
	public void lock() throws Exception {
		try {
			Mockit.setUpMock(LockProxyImplMock.class);
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
		} finally {
			Mockit.tearDownMocks(LockProxyImplMock.class);
		}
	}

	@Test
	public void unlock() throws Exception {
		try {
			boolean gotLock = clusterManagerHazelcast.lock(IConstants.IKUBE);
			assertTrue(gotLock);
			Thread thread = new Thread(new Runnable() {
				public void run() {
					boolean gotLock = clusterManagerHazelcast.lock(IConstants.IKUBE);
					assertFalse(gotLock);
				}
			});
			thread.start();
			Thread.sleep(100);

			clusterManagerHazelcast.unlock(IConstants.IKUBE);
			thread = new Thread(new Runnable() {
				public void run() {
					boolean gotLock = clusterManagerHazelcast.lock(IConstants.IKUBE);
					assertTrue(gotLock);
				}
			});
			thread.start();
			thread.join();
			Thread.sleep(100);
		} finally {
			Mockit.tearDownMocks(LockProxyImplMock.class);
		}
	}

	@Test
	public void anyWorking() {
		boolean anyworking = clusterManagerHazelcast.anyWorking();
		assertFalse(anyworking);

		entrySet.add(entry);
		anyworking = clusterManagerHazelcast.anyWorking();
		assertFalse(anyworking);

		Action action = new Action();
		server.setActions(Arrays.asList(action));
		// clusterManagerHazelcast.put(server.getIp(), server);
		Hazelcast.getMap(IConstants.IKUBE).put(server.getAddress(), server);

		anyworking = clusterManagerHazelcast.anyWorking();
		assertTrue(anyworking);
	}

	@Test
	public void anyWorkingIndex() {
		boolean anyWorkingIndex = clusterManagerHazelcast.anyWorking(indexName);
		assertFalse(anyWorkingIndex);

		Action action = mock(Action.class);
		when(action.getIndexName()).thenReturn(indexName);
		server.setActions(Arrays.asList(action));

		entrySet.add(entry);
		anyWorkingIndex = clusterManagerHazelcast.anyWorking(indexName);
		assertTrue(anyWorkingIndex);
	}

	@Test
	public void startWorking() {
		Action action = clusterManagerHazelcast.startWorking(actionName, indexName, indexableName);
		assertEquals(indexName, action.getIndexName());
	}

	@Test
	public void stopWorking() {
		Action action = new Action();
		action.setId(0);
		action.setServer(server);
		action.setSnapshot(snapshot);
		action.setStartTime(new Date());
		action.setIndexName("indexName");
		action.setActionName("actinName");
		action.setIndexableName("indexableName");
		action.setTimestamp(new Timestamp(System.currentTimeMillis()));
		server.setActions(new ArrayList<Action>(Arrays.asList(action)));

		clusterManagerHazelcast.stopWorking(action);

		assertNotNull(action.getEndTime());
		assertEquals(0, server.getActions().size());
	}

	@Test
	public void getServerAndServers() {
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
		MessageListener messageListener = Mockito.mock(MessageListener.class);
		Hazelcast.getTopic(IConstants.TOPIC).addMessageListener(messageListener);
		clusterManagerHazelcast.sendMessage(new Event());
		ThreadUtilities.sleep(1000);
		Mockito.verify(messageListener, Mockito.atLeastOnce()).onMessage(Mockito.any(Message.class));
	}

	@Test
	public void submitDestroy() {
		try {
			Mockit.tearDownMocks();
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					while (true) {
						ThreadUtilities.sleep(10000);
					}
				}
			};
			String name = "name";
			Future<?> future = ThreadUtilities.submit(name, runnable);
			logger.info("Future : " + future.isCancelled() + ", " + future.isDone() + ", " + future);
			Event event = IListener.EventGenerator.getEvent(Event.TERMINATE, System.currentTimeMillis(), name, Boolean.FALSE);
			clusterManagerHazelcast.sendMessage(event);
			ThreadUtilities.sleep(5000);
			assertTrue(future.isCancelled());
		} finally {
			Mockit.setUpMock(SpellingCheckerMock.class);
		}
	}

	@Test
	public void threaded() {
		Hazelcast.getLock(IConstants.IKUBE).forceUnlock();
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
			Future<?> future = ThreadUtilities.submit(null, runnable);
			futures.add(future);
		}
		ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
	}

	private void validate(final Boolean[] locks) {
		int count = 0;
		for (Boolean lock : locks) {
			count = lock == null ? count : lock ? count + 1 : count;
		}
		assertTrue(count <= 1);
	}

}