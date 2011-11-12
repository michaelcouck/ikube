package ikube.cluster;

import ikube.model.Server;

import java.util.List;

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
	boolean lock(String name);

	/**
	 * Unlocks the cluster. The server can only unlock the cluster if it already has the lock.
	 * 
	 * @param name the name of the lock, must be unique
	 * @return whether the cluster was unlocked by this server
	 */
	boolean unlock(String name);

	/**
	 * @return whether there are any servers in the cluster that are working excluding this one. If this server is working then the server
	 *         must be retrieved with the {@link IClusterManager#getServer()} and check the {@link Server#getWorking()} method
	 */
	boolean anyWorking();

	/**
	 * @return whether there are any servers in the cluster that are working on this index
	 */
	boolean anyWorking(String indexName);

	/**
	 * Sets the server working on this index and this indexable.
	 * 
	 * @param actionName the name of the action currently executing
	 * @param indexName the name of the index that this server will work on
	 * @param indexableName the name of the currently executing indexable
	 * @param isWorking whether the server is working or not
	 * @return this method returns the first time that was registered for any server that has executed this index and this indexable. This
	 *         needs to be in one method so that the servers can be locked before this operation
	 */
	long startWorking(String actionName, String indexName, String indexableName);

	/**
	 * Stops the server working. This will broadcast to the cluster that the action/job is finished. This indicates that another server can
	 * start working.
	 * 
	 * @param actionName the name of the action that has stopped or finished working
	 * @param indexName the name of the index that the action was working on
	 * @param indexableName the name of the indexable that the action was working on
	 */
	void stopWorking(String actionName, String indexName, String indexableName);

	/**
	 * Returns the next id from one of the servers. The id would be the id in the table for this index.
	 * 
	 * @param indexableName the name of the indexable that we want the id for
	 * @param indexName the name of the index currently getting executed
	 * @param batchSize the size of the batch for this index context. This batch size will then be added to the action and published into
	 *            the cluster effectively determining the next if of this table
	 * @return the id of the next row in the table for this index
	 */
	@Deprecated
	long getIdNumber(String indexableName, String indexName, long batchSize, long minId);

	/**
	 * @return the servers in the cluster
	 */
	List<Server> getServers();

	/**
	 * @return this server object
	 */
	Server getServer();

	@Deprecated
	boolean isException();

	@Deprecated
	void setException(boolean exception);

}