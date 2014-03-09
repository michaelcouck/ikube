package ikube.cluster.hzc;

import com.hazelcast.config.Config;
import com.hazelcast.core.*;
import com.hazelcast.monitor.LocalMapStats;
import ikube.IConstants;
import ikube.cluster.AClusterManager;
import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Michael Couck
 * @version 01.00
 * @see IClusterManager
 * @since 15-07-2012
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class ClusterManagerHazelcast extends AClusterManager {

    /**
     * The instance of this server.
     */
    private Server server;
    @Autowired
    private IMonitorService monitorService;
    @Autowired
    @Qualifier("ikube-hazelcast")
    private HazelcastInstance hazelcastInstance;
    @Autowired
    private OutOfMemoryHandler outOfMemoryHandler;

    @SuppressWarnings("StringBufferReplaceableByString")
    public void initialize() {
        ip = UriUtilities.getIp();
        Hazelcast.setOutOfMemoryHandler(outOfMemoryHandler);
        int port = hazelcastInstance.getCluster().getLocalMember().getInetSocketAddress().getPort();
        address = new StringBuilder(ip).append("-").append(port).toString();
        final Config config = hazelcastInstance.getConfig();
        config.getNetworkConfig().getInterfaces().setInterfaces(Arrays.asList(ip));
        // Start a thread that will keep an eye on Hazelcast

        class HazelcastWatcher implements Runnable {
            @SuppressWarnings("InfiniteLoopStatement")
            public void run() {
                do {
                    boolean reinitialize = Boolean.FALSE;
                    try {
                        boolean locked = lock(IConstants.HAZELCAST_WATCHER);
                        logger.info("Hazelcast watcher lock : " + locked);
                        printStatistics(hazelcastInstance);
                    } catch (final Exception e) {
                        reinitialize = Boolean.TRUE;
                        logger.error("Error...", e);
                    } finally {
                        unlock(IConstants.HAZELCAST_WATCHER);
                        if (reinitialize) {
                            logger.info("Restarting the grid!!!");
                            Hazelcast.shutdownAll();
                            hazelcastInstance = Hazelcast.newHazelcastInstance(config);
                        }
                    }
                    ThreadUtilities.sleep(IConstants.HUNDRED_THOUSAND * 6);
                } while (true);
            }
        }
        ThreadUtilities.submit(IConstants.HAZELCAST_WATCHER, new HazelcastWatcher());
    }

    static void printStatistics(final HazelcastInstance hazelcastInstance) {
        printStatistics(hazelcastInstance.getMap(IConstants.IKUBE));
        printStatistics(hazelcastInstance.getMap(IConstants.SEARCH));
        printStatistics(hazelcastInstance.getMap(IConstants.SERVER));
    }

    static void printStatistics(final IMap map) {
        System.out.println("Stats for map : " + map.getName() + ", size : " + map.size());
        final LocalMapStats localMapStats = map.getLocalMapStats();
        class MethodCallback implements ReflectionUtils.MethodCallback {
            @Override
            public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException {
                try {
                    String name = method.getName();
                    Object result = method.invoke(localMapStats);
                    System.out.println("        : " + name.replace("get", "") + " : " + result);
                } catch (final InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        class MethodFilter implements ReflectionUtils.MethodFilter {
            @Override
            public boolean matches(final Method method) {
                return method.getName().startsWith("get") && method.getParameterTypes().length == 0;
            }
        }
        ReflectionUtils.doWithMethods(LocalMapStats.class, new MethodCallback(), new MethodFilter());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean lock(final String name) {
        try {
            ILock lock = hazelcastInstance.getLock(name);
            boolean gotLock = false;
            try {
                gotLock = lock.tryLock(250, TimeUnit.MILLISECONDS);
                logger.debug("Got lock : {} , thread : {} ", gotLock, Thread.currentThread().hashCode());
            } catch (final InterruptedException e) {
                logger.error("Exception trying for the lock : ", e);
            }
            return gotLock;
        } finally {
            notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean unlock(final String name) {
        try {
            ILock lock = hazelcastInstance.getLock(name);
            if (lock.isLocked()) {
                if (lock.isLockedByCurrentThread()) {
                    logger.debug("Unlocking : {} ", Thread.currentThread().hashCode());
                    lock.unlock();
                }
            }
            return true;
        } finally {
            notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean anyWorking() {
        Map<String, Server> servers = getServers();
        for (final Map.Entry<String, Server> mapEntry : servers.entrySet()) {
            Server server = mapEntry.getValue();
            if (server.isWorking()) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean anyWorking(final String indexName) {
        Map<String, Server> servers = getServers();
        for (final Map.Entry<String, Server> mapEntry : servers.entrySet()) {
            Server server = mapEntry.getValue();
            if (!server.isWorking() || server.getActions() == null) {
                continue;
            }
            for (Action action : server.getActions()) {
                if (indexName.equals(action.getIndexName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Action startWorking(final String actionName, final String indexName, final String indexableName) {
        Action action = null;
        try {
            Server server = getServer();
            action = getAction(actionName, indexName, indexableName);
            server.getActions().add(action);
            hazelcastInstance.getMap(IConstants.SERVER).put(server.getAddress(), server);
        } catch (final Exception e) {
            logger.error("Exception starting action : " + actionName + ", " + indexName + ", " + indexableName, e);
        } finally {
            notifyAll();
        }
        return action;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void stopWorking(final Action action) {
        try {
            // Persist the action with the end date
            action.setEndTime(new Timestamp(System.currentTimeMillis()));
            action.setDuration(action.getEndTime().getTime() - action.getStartTime().getTime());
            dataBase.merge(action);
            Server server = getServer();
            List<Action> actions = server.getActions();
            // Remove the action from the grid
            Iterator<Action> actionIterator = actions.iterator();
            while (actionIterator.hasNext()) {
                Action gridAction = actionIterator.next();
                if (gridAction.getId() == action.getId()) {
                    actionIterator.remove();
                    logger.debug("Removed grid action : {} , ", gridAction.getId(), actions.size());
                }
            }
            hazelcastInstance.getMap(IConstants.SERVER).put(server.getAddress(), server);
        } finally {
            notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Server> getServers() {
        return hazelcastInstance.getMap(IConstants.SERVER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Server getServer() {
        if (server == null) {
            server = new Server();
            server.setIp(ip);
            server.setAddress(address);
            server.setAge(System.currentTimeMillis());
            logger.debug("Server null, creating new one : {} ", server);

            Collection<IndexContext> collection = monitorService.getIndexContexts().values();
            List<IndexContext> indexContexts = new ArrayList<>(collection);
            server.setIndexContexts(indexContexts);

            dataBase.persist(server);
        }
        return server;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(final Serializable serializable) {
        hazelcastInstance.getTopic(IConstants.TOPIC).publish(serializable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Future<T> sendTask(final Callable<T> callable) {
        IExecutorService executorService = hazelcastInstance.getExecutorService(IConstants.EXECUTOR_SERVICE);
        List<Member> members = new ArrayList<>(hazelcastInstance.getCluster().getMembers());
        Member member = members.get(new Random().nextInt(members.size()));
        return executorService.submitToMember(callable, member);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<Future<T>> sendTaskToAll(final Callable<T> callable) {
        IExecutorService executorService = hazelcastInstance.getExecutorService(IConstants.EXECUTOR_SERVICE);
        Map<Member, Future<T>> futures = executorService.submitToAllMembers(callable);
        return new ArrayList<>(futures.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(final Object key) {
        return get(IConstants.IKUBE, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(final Object key, final Serializable value) {
        put(IConstants.IKUBE, key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final Object key) {
        remove(IConstants.IKUBE, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(final String map, final Object key) {
        return (T) hazelcastInstance.getMap(map).get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(final String map, final Object key, final Serializable value) {
        hazelcastInstance.getMap(map).put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final String map, final Object key) {
        hazelcastInstance.getMap(map).remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        Hazelcast.shutdownAll();
    }

}