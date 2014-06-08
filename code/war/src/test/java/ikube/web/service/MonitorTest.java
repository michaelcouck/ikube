package ikube.web.service;

import ikube.BaseTest;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.web.toolkit.PerformanceTester;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Michael couck
 * @version 01.00
 * @since 16-10-2012
 */
public class MonitorTest extends BaseTest {

    private Monitor monitor;
    private IMonitorService monitorService;
    private IClusterManager clusterManager;
    @SuppressWarnings("rawtypes")
    private IndexContext indexContext;
    private Random random;

    @Before
    public void before() {
        random = new Random();
        monitor = new Monitor();
        indexContext = getIndexContext(Integer.toString(random.nextInt()));

        monitorService = mock(IMonitorService.class);
        clusterManager = mock(IClusterManager.class);
        Deencapsulation.setField(monitor, monitorService);
        Deencapsulation.setField(monitor, clusterManager);
    }

    @Test
    public void fields() throws Exception {
        when(monitorService.getIndexFieldNames(anyString())).thenReturn(new String[]{"one", "two", "three"});
        Response fields = monitor.fields("indexName");
        assertEquals("The string should be a concatenation of the fields : ", "[\"one\",\"two\",\"three\"]", fields.getEntity());
    }

    @Test
    public void indexContext() {
        when(monitorService.getIndexContext(anyString())).thenReturn(indexContext);
        Response indexContext = monitor.indexContext(IConstants.GEOSPATIAL);
        Object entity = indexContext.getEntity();
        assertTrue("The max age should be in the Json string : ", entity.toString().contains("maxAge"));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void indexContexts() {
        Map<String, IndexContext> indexContexts = new HashMap<>();
        IndexContext indexContextOne = getIndexContext("aaa");
        IndexContext indexContextTwo = getIndexContext("bbb");
        indexContexts.put(IConstants.GEOSPATIAL, indexContextOne);
        indexContexts.put(IConstants.ADDRESS, indexContextTwo);

        when(monitorService.getIndexContexts()).thenReturn(indexContexts);
        Response indexContext = monitor.indexContexts("name", true);
        Object entity = indexContext.getEntity();

        List<LinkedHashMap<String, String>> sortedIndexContexts = IConstants.GSON.fromJson(entity.toString(), List.class);
        String nameOne = sortedIndexContexts.get(0).get("name");
        String nameTwo = sortedIndexContexts.get(1).get("name");

        assertEquals("aaa", nameOne);
        assertEquals("bbb", nameTwo);

        indexContext = monitor.indexContexts("name", false);
        entity = indexContext.getEntity();
        sortedIndexContexts = IConstants.GSON.fromJson(entity.toString(), List.class);
        nameOne = sortedIndexContexts.get(0).get("name");
        nameTwo = sortedIndexContexts.get(1).get("name");
        assertEquals("bbb", nameOne);
        assertEquals("aaa", nameTwo);

        assertTrue("The max age should be in the Json string : ", entity.toString().contains("maxAge"));

        double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
            @Override
            public void execute() throws Throwable {
                monitor.indexContexts("name", false);
            }
        }, "Index contexts web service : ", 100, Boolean.TRUE);
        assertTrue("This function must be quite fast because it is online : ", executionsPerSecond > 10);
    }

    @Test
    public void servers() {
        Server server = getServer("127.0.0.1");
        Map<String, Server> servers = new HashMap<>();
        servers.put(IConstants.IKUBE, server);

        when(clusterManager.getServers()).thenReturn(servers);
        Response indexContext = monitor.servers();
        Object entity = indexContext.getEntity();
        assertTrue("The max age should be in the Json string : ", entity.toString().contains("averageCpuLoad"));
    }

    @Test
    public void indexingStatistics() {
        Map<String, Server> servers = getServers();
        when(clusterManager.getServers()).thenReturn(servers);
        Response response = monitor.indexingStatistics();
        Object entity = response.getEntity();
        assertEquals(
                "[[\"Times\", \"127.0.0.1-8002\", \"127.0.0.1-8003\", \"127.0.0.1-8000\", \"127.0.0.1-8001\"], " +
                        "[\"1.1\", 3, 3, 3, 3], [\"1.2\", 6, 6, 6, 6], [\"1.3\", 9, 9, 9, 9]]", entity);
        double perSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
            @Override
            public void execute() throws Throwable {
                monitor.indexingStatistics();
            }
        }, "Indexing statistics", 1000, Boolean.TRUE);
        assertTrue(perSecond > 100);
    }

    @Test
    public void searchingStatistics() {
        Map<String, Server> servers = getServers();
        when(clusterManager.getServers()).thenReturn(servers);
        Response response = monitor.searchingStatistics();
        Object entity = response.getEntity();
        assertEquals(
                "[[\"Times\", \"127.0.0.1-8002\", \"127.0.0.1-8003\", \"127.0.0.1-8000\", \"127.0.0.1-8001\"], " +
                        "[\"1.1\", 300, 300, 300, 300], [\"1.2\", 600, 600, 600, 600], [\"1.3\", 900, 900, 900, 900]]", entity);
        double perSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
            @Override
            public void execute() throws Throwable {
                monitor.searchingStatistics();
            }
        }, "Indexing statistics", 1000, Boolean.TRUE);
        assertTrue(perSecond > 100);
    }

    @Test
    public void actions() {
        Response response = monitor.actions();
        Object entity = response.getEntity();
        assertFalse(entity.toString().contains(Integer.toString(Integer.MAX_VALUE)));

        Map<String, Server> servers = getServers();
        when(clusterManager.getServers()).thenReturn(servers);
        response = monitor.actions();
        entity = response.getEntity();
        assertTrue(entity.toString().contains(Integer.toString(Integer.MAX_VALUE)));
    }

    private Map<String, Server> getServers() {
        Server serverOne = getServer("127.0.0.1-8000");
        Server serverTwo = getServer("127.0.0.1-8001");
        Server serverThree = getServer("127.0.0.1-8002");
        Server serverFour = getServer("127.0.0.1-8003");

        addAction(serverOne);
        addAction(serverTwo);
        addAction(serverThree);
        addAction(serverFour);

        Map<String, Server> servers = new HashMap<>();
        servers.put(serverOne.getAddress(), serverOne);
        servers.put(serverTwo.getAddress(), serverTwo);
        servers.put(serverThree.getAddress(), serverThree);
        servers.put(serverFour.getAddress(), serverFour);

        return servers;
    }

    private Action addAction(final Server server) {
        Action action = new Action();
        action.setServer(server);
        action.setActionName("action");
        action.setDuration(Integer.MAX_VALUE);
        action.setEndTime(null);
        action.setId(Integer.MAX_VALUE);
        action.setIndexableName("indexableName");
        action.setIndexName("indexName");
        action.setResult(Boolean.TRUE);
        action.setStartTime(new Date());
        action.setTimestamp(new Timestamp(System.currentTimeMillis()));
        server.getActions().add(action);
        return action;
    }

    @SuppressWarnings("rawtypes")
    private Server getServer(final String address) {
        Server server = new Server();
        server.setActions(new ArrayList<Action>());
        server.setAddress(address);

        List<IndexContext> indexContexts = new ArrayList<>();
        indexContexts.add(getIndexContext(Integer.toString(random.nextInt())));
        indexContexts.add(getIndexContext(Integer.toString(random.nextInt())));
        indexContexts.add(getIndexContext(Integer.toString(random.nextInt())));

        server.setIndexContexts(indexContexts);

        return server;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private IndexContext getIndexContext(final String indexName) {
        IndexContext indexContext = new IndexContext();
        indexContext.setIndexName(indexName);
        indexContext.setIndexDirectoryPath("index-directory-path");
        List<Snapshot> snapshots = new ArrayList<>();

        snapshots.add(getSnapshot(1, 100, 60000));
        snapshots.add(getSnapshot(2, 200, 120000));
        snapshots.add(getSnapshot(3, 300, 180000));
        indexContext.setSnapshots(snapshots);
        return indexContext;
    }

    private Snapshot getSnapshot(final long docsPerMinute, final long searchesPerMinute, final long time) {
        Snapshot snapshot = new Snapshot();
        snapshot.setTimestamp(new Timestamp(time));
        snapshot.setDocsPerMinute(docsPerMinute);
        snapshot.setSearchesPerMinute(searchesPerMinute);
        return snapshot;
    }

}