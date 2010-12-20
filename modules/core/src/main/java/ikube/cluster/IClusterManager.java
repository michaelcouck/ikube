package ikube.cluster;

import ikube.cluster.cache.ICache;
import ikube.cluster.cache.ICache.IAction;
import ikube.cluster.cache.ICache.ICriteria;
import ikube.model.Server;
import ikube.model.Url;

import java.util.List;

import com.hazelcast.core.ILock;

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
	public long setWorking(String indexName, String indexableName, boolean isWorking);

	/**
	 * @return whether there are any servers in the cluster that are working
	 */
	public boolean anyWorking();

	/**
	 * @return whether there are any servers in the cluster that are working on this index
	 */
	public boolean anyWorking(String indexName);

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
	public long getIdNumber(String indexableName, String indexName, long batchSize);

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
	public boolean isHandled(String indexableName, String indexName);

	/**
	 * @return the servers in the cluster
	 */
	public List<Server> getServers();

	/**
	 * @return this server object
	 */
	public Server getServer();

	/** Cluster wide cache access. */

	/**
	 * @see ICache#get(String, ICriteria, IAction, int)
	 */
	public List<Url> getBatch(int size);

	/**
	 * @see ICache#get(String, String)
	 */
	public <T> T get(Class<T> klass, String sql);

	/**
	 * @see ICache#set(String, Long, Object)
	 */
	public <T> void set(Class<T> klass, Long id, T t);

	/**
	 * @see ICache#clear(String)
	 */
	public <T> void clear(Class<T> klass);

	/**
	 * @see ICache#size(String)
	 */
	public <T> int size(Class<T> klass);

	public ILock lock(String lockName);

	public void unlock(ILock lock);

}