package ikube.cluster;

import ikube.model.Action;
import ikube.model.Server;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class CulsterManagerDummy extends AClusterManager {

    @Override
    public boolean lock(final String name) {
        return false;
    }

    @Override
    public boolean unlock(final String name) {
        return false;
    }

    @Override
    public boolean anyWorking() {
        return false;
    }

    @Override
    public boolean anyWorking(final String indexName) {
        return false;
    }

    @Override
    public Action startWorking(final String actionName, final String indexName, final String indexableName) {
        return null;
    }

    @Override
    public void stopWorking(final Action action) {

    }

    @Override
    public Server getServer() {
        return null;
    }

    @Override
    public Map<String, Server> getServers() {
        return null;
    }

    @Override
    public void sendMessage(final Serializable serializable) {

    }

    @Override
    public <T> Future<T> sendTask(final Callable<T> callable) {
        return null;
    }

    @Override
    public <T> Future<T> sendTaskTo(final Server server, final Callable<T> callable) {
        return null;
    }

    @Override
    public <T> List<Future<T>> sendTaskToAll(final Callable<T> callable) {
        return null;
    }

    @Override
    public Object get(final Object key) {
        return null;
    }

    @Override
    public void put(final Object key, final Serializable object) {

    }

    @Override
    public void remove(final Object key) {

    }

    @Override
    public void clear(final String map) {

    }

    @Override
    public <T> T get(final String map, final Object key) {
        return null;
    }

    @Override
    public void put(final String map, final Object key, final Serializable object) {

    }

    @Override
    public void remove(final String map, final Object key) {

    }

    @Override
    public void destroy() {

    }

}
