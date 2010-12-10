package ikube.cluster;

import ikube.model.Server;
import ikube.model.Url;

import java.util.List;
import java.util.Set;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface IClusterManager {

	/** Cluster wide cache. */

	public List<Url> getBatch(int size);

	public <T> T get(Class<T> klass, String sql);

	public <T> void set(Class<T> klass, Long id, T t);

	public <T> void clear(Class<T> klass);

	public <T> int size(Class<T> klass);

	/**
	 * Sets the working flag for this index, this action and that handler that is executing the indexable.
	 * 
	 * @param indexName
	 * @param actionName
	 * @param handlerName
	 * @param isWorking
	 * @return
	 */
	public long setWorking(String indexName, String actionName, String handlerName, boolean isWorking);

	/**
	 * @return whether there are any servers in the cluster that are working
	 */
	public boolean anyWorking();

	/**
	 * Returns the next id from one of the servers. The id would be the id in the table for this index.
	 * 
	 * @param indexName
	 *            the name of the index currently getting executed
	 * @return the id of the next row in the table for this index
	 */
	public long getIdNumber(String indexName, long batchSize);

	/**
	 * @return the servers in the cluster
	 */
	public Set<Server> getServers();

	/**
	 * @return this server object
	 */
	public Server getServer();

}