package ikube.cluster;

import ikube.model.Action;
import ikube.model.Server;

import java.io.Serializable;
import java.util.Map;

/**
 * This is the interface that will synchronize and coordinate the servers in the cluster. The implementors are critical to the functioning
 * of Ikube.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface IClusterManager {

	/**
	 * This method will lock, or try to lock the cluster.
	 * 
	 * @param name the name of the lock, must be unique
	 * @return whether the cluster was successfully locked
	 */
	boolean lock(final String name);

	/**
	 * Unlocks the cluster. The server can only unlock the cluster if it already has the lock.
	 * 
	 * @param name the name of the lock, must be unique
	 * @return whether the cluster was unlocked by this server
	 */
	boolean unlock(final String name);

	/**
	 * This method checks whether there are any servers that are working on any index, anywhere in the cluster.
	 * 
	 * @return whether there are any servers in the cluster that are working excluding this one. If this server is working then the server
	 *         must be retrieved with the {@link IClusterManager#getServer()} and check the {@link Server#getWorking()} method
	 */
	boolean anyWorking();

	/**
	 * This method checks whether there are any servers working on the index specified in the parameter list in the cluster.
	 * 
	 * @return whether there are any servers in the cluster that are working on this index
	 */
	boolean anyWorking(final String indexName);

	/**
	 * Sets the server working on this index and this indexable.
	 * 
	 * @param actionName the name of the action currently executing
	 * @param indexName the name of the index that this server will work on
	 * @param indexableName the name of the currently executing indexable
	 * @param isWorking whether the server is working or not
	 * @return the action that was started
	 */
	Action startWorking(final String actionName, final String indexName, final String indexableName);

	/**
	 * Stops the server working. This will broadcast to the cluster that the action/job is finished. This indicates that another server can
	 * start working.
	 * 
	 * @param action the action that was started
	 */
	void stopWorking(final Action action);

	/**
	 * Access to the current local server object.
	 * 
	 * @return this server object
	 */
	Server getServer();

	/**
	 * Access to the servers in the distributed cache.
	 * 
	 * @return the servers in the cluster
	 */
	Map<String, Server> getServers();

	/**
	 * Sends a message to the cluster. Messages may include actions that this server is working on, or a lock attempt, or the server object
	 * to stay in the cluster club.
	 * 
	 * @param serializable the object to send to the cluster
	 */
	void sendMessage(final Serializable serializable);

	/**
	 * This method will remove an object from the grid.
	 * 
	 * @param key the key of the object to remove
	 */
	void remove(final Object key);

	/**
	 * This method will release any resources and close down the cluster manager gracefully.
	 */
	void destroy();

}