package ikube.cluster.hzc;

import com.hazelcast.config.Config;
import com.hazelcast.core.*;
import ikube.IConstants;
import ikube.cluster.AClusterManager;
import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.toolkit.UriUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.Serializable;
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
public final class ClusterManagerHazelcast extends AClusterManager {

    /**
     * The instance of this server.
     */
    private Server server;
    @Autowired
    private IMonitorService monitorService;
    @Autowired
    @Qualifier("ikube-hazelcast")
    private HazelcastInstance hazelcastInstance;
    // private List<MessageListener<Object>> listeners;

    public void initialize() {
        ip = UriUtilities.getIp();
        // hazelcastInstance = Hazelcast.getHazelcastInstanceByName(IConstants.IKUBE);
        if (hazelcastInstance == null) {
            hazelcastInstance = Hazelcast.newHazelcastInstance();
            logger.info("New Hazelcast instance : " + hazelcastInstance);
        }
        int port = hazelcastInstance.getCluster().getLocalMember().getInetSocketAddress().getPort();
        address = ip + "-" + port;
        Config config = hazelcastInstance.getConfig();
        config.getNetworkConfig().getInterfaces().setInterfaces(Arrays.asList(ip));
        /*if (listeners != null) {
            for (final MessageListener<Object> listener : listeners) {
                if (listener == null) {
                    continue;
                }
                hazelcastInstance.getTopic(IConstants.TOPIC).addMessageListener(listener);
            }
        }*/
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
                logger.debug("Got lock : " + gotLock + ", thread : " + Thread.currentThread().hashCode());
            } catch (InterruptedException e) {
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
                    logger.debug("Unlocking : " + Thread.currentThread().hashCode());
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
            hazelcastInstance.getMap(IConstants.IKUBE).put(server.getAddress(), server);
        } catch (Exception e) {
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
                    logger.debug("Removed grid action : " + gridAction.getId() + ", " + actions.size());
                }
            }
            hazelcastInstance.getMap(IConstants.IKUBE).put(server.getAddress(), server);
        } finally {
            notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Server> getServers() {
        return hazelcastInstance.getMap(IConstants.IKUBE);
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
            logger.debug("Server null, creating new one : " + server);

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

    /*public void setListeners(List<MessageListener<Object>> listeners) {
        this.listeners = listeners;
    }*/

}