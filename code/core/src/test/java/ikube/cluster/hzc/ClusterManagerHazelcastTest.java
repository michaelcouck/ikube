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
import ikube.model.Action;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.scheduling.schedule.Event;
import ikube.toolkit.ThreadUtilities;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.instance.HazelcastInstanceImpl;

/**
 * @author Michael Couck
 * @since 17.04.11
 * @version 01.00
 */
public class ClusterManagerHazelcastTest extends AbstractTest {

	@MockClass(realClass = HazelcastInstanceImpl.class)
	public static class HazelcastMock {

		@SuppressWarnings("unchecked")
		static IMap<String, Server> servers = mock(IMap.class);

		@Mock
		@SuppressWarnings("unchecked")
		public static <K, V> IMap<K, V> getMap(String name) {
			return (IMap<K, V>) servers;
		}
	}

	private Server server;
	private Map.Entry<String, Server> serverEntry;
	private Set<Map.Entry<String, Server>> serverEntrySet;

	private String actionName = "actionName";
	private String indexName = "indexName";
	private String indexableName = "indexableName";

	@Cascading
	private IDataBase dataBase;
	@Cascading
	private IMonitorService monitorService;
	@Cascading
	private Snapshot snapshot;

	private static ClusterManagerHazelcast clusterManagerHazelcast;

	@BeforeClass
	public static void beforeClass() {
		clusterManagerHazelcast = new ClusterManagerHazelcast();

		StartListener startListener = new StartListener();
		StopListener stopListener = new StopListener();
		List<MessageListener<Object>> listeners = new ArrayList<MessageListener<Object>>(Arrays.asList(startListener, stopListener));
		clusterManagerHazelcast.setListeners(listeners);
	}

	@Before
	@SuppressWarnings("unchecked")
	public void before() {
		dataBase = Mockito.mock(IDataBase.class);
		monitorService = Mockito.mock(IMonitorService.class);
		Mockit.setUpMocks(HazelcastMock.class);

		server = new Server();
		serverEntry = mock(Map.Entry.class);
		serverEntrySet = new HashSet<Map.Entry<String, Server>>();

		when(serverEntry.getValue()).thenReturn(server);
		when(HazelcastMock.servers.entrySet()).thenReturn(serverEntrySet);
		when(HazelcastMock.servers.get(anyString())).thenReturn(server);

		Deencapsulation.setField(clusterManagerHazelcast, server);
		Deencapsulation.setField(clusterManagerHazelcast, dataBase);
		Deencapsulation.setField(clusterManagerHazelcast, monitorService);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@AfterClass
	public static void afterClass() {
		Hazelcast.shutdownAll();
	}

	@Test
	public void lock() throws Exception {
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
	}

	@Test
	public void anyWorking() {
		boolean anyworking = clusterManagerHazelcast.anyWorking();
		assertFalse(anyworking);

		serverEntrySet.add(serverEntry);
		anyworking = clusterManagerHazelcast.anyWorking();
		assertFalse(anyworking);

		Action action = new Action();
		server.setActions(Arrays.asList(action));
		// clusterManagerHazelcast.put(server.getIp(), server);
		HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
		hazelcastInstance.getMap(IConstants.IKUBE).put(server.getAddress(), server);

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

		serverEntrySet.add(serverEntry);
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
		// Mockit.tearDownMocks();
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
		HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
		hazelcastInstance.getTopic(IConstants.TOPIC).addMessageListener(messageListener);
		clusterManagerHazelcast.sendMessage(new Event());
		ThreadUtilities.sleep(1000);
		Mockito.verify(messageListener, Mockito.atLeastOnce()).onMessage(Mockito.any(Message.class));
	}

	@Test
	public void submitDestroy() {
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
	}

	@Test
	@SuppressWarnings("unchecked")
	public void threaded() {
		int threads = 3;
		final int iterations = 100;
		final double sleep = 100;
		final Boolean[] locks = new Boolean[threads];
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (int i = 0; i < threads; i++) {
			final int thread = i;

			class ClusterRunnable implements Runnable {
				ClusterManagerHazelcast clusterManagerHazelcast;

				ClusterRunnable() {
					clusterManagerHazelcast = new ClusterManagerHazelcast();
					clusterManagerHazelcast.setListeners(Collections.EMPTY_LIST);
				}

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
			}

			Runnable runnable = new ClusterRunnable();
			Future<?> future = ThreadUtilities.submit(null, runnable);
			futures.add(future);
		}
		ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
	}

	/**
	 * The method validates that only one 'server'(thread) has the lock at any one time.
	 * 
	 * @param locks the locks of all the servers/threads
	 */
	private void validate(final Boolean[] locks) {
		int count = 0;
		for (Boolean lock : locks) {
			count = lock == null ? count : lock ? count + 1 : count;
		}
		assertTrue(count <= 1);
	}

}