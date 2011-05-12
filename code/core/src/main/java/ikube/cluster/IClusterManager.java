package ikube.cluster;

import ikube.cluster.cache.ICache;
import ikube.model.Server;

import java.util.List;

import com.hazelcast.core.ILock;

/**
 * This is the interface that will synchronise and coordinate the servers in the cluster. The implementors are critical to the functioning
 * of Ikube.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface IClusterManager {

	/**
	 * Sets the server working on this index and this indexable.
	 * 
	 * @param indexName
	 *            the name of the index that this server will work on
	 * @param indexableName
	 *            the name of the currently executing indexable
	 * @param isWorking
	 *            whether the server is working or not
	 * @return this method returns the first time that was registered for any server that has executed this index and this indexable. This
	 *         needs to be in one method so that the servers can be locked before this operation
	 */
	long setWorking(String indexName, String indexableName, boolean isWorking);

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
	 * Returns the next id from one of the servers. The id would be the id in the table for this index.
	 * 
	 * @param indexableName
	 *            the name of the indexable that we want the id for
	 * @param indexName
	 *            the name of the index currently getting executed
	 * @param batchSize
	 *            the size of the batch for this index context. This batch size will then be added to the action and published into the
	 *            cluster effectively determining the next if of this table
	 * @return the id of the next row in the table for this index
	 */
	long getIdNumber(String indexableName, String indexName, long batchSize, long minId);

	/**
	 * Checks whether this indexable has already been handled, could be a file share that is not clusterable and only needs to be indexed by
	 * one thread.
	 * 
	 * @param indexableName
	 *            the name of the indexable
	 * @param indexName
	 *            the name of the index
	 * @return whether this indexable has been handled
	 */
	boolean isHandled(String indexableName, String indexName);

	/**
	 * @return the servers in the cluster
	 */
	List<Server> getServers();

	/**
	 * @return this server object
	 */
	Server getServer();

	/** Cluster wide cache access. */

	/**
	 * @see ICache#get(String, String)
	 */
	<T> T get(String name, String sql);

	/**
	 * @see ICache#get(String, Long)
	 */
	<T> T get(String name, Long id);

	/**
	 * @see ICache#get(Class<T>, ICache.ICriteria, ICache.IAction)
	 */
	<T> List<T> get(Class<T> klass, String name, ICache.ICriteria<T> criteria, ICache.IAction<T> action, int size);

	/**
	 * @see ICache#set(String, Long, Object)
	 */
	<T> void set(String name, Long id, T object);

	/**
	 * @see ICache#clear(String)
	 */
	<T> void clear(String name);

	/**
	 * @see ICache#size(String)
	 */
	<T> int size(String klass);

	/**
	 * @see ICache#remove(String, Long)
	 */
	<T> void remove(String name, Long id);

	/**
	 * @see ICache#lock(String)
	 */
	ILock lock(String lockName);

	/**
	 * @see ICache#unlock(ILock)
	 */
	void unlock(ILock lock);
	
	ICache getCache();

}