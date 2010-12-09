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
	 * Returns whether any servers are working on any action other than this action.
	 * 
	 * @param actionName
	 *            the action name to check if any other servers are doing anything else
	 * @return whether there are any servers working on any other action than the one specified in the parameter list
	 */
	public boolean anyWorking(String actionName);

	/**
	 * Checks to see if there are any other servers working on this index, with this action.
	 * 
	 * @param indexName
	 *            the name of the index that we want to start working on
	 * @param actionName
	 *            the name of the action we want to see if there are any servers working on
	 * @return whether there are any servers working on this index with this action, other than ourselves of course
	 */
	public boolean areWorking(String indexName, String actionName);

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

	/**
	 * Returns whether there are any servers working on this index.
	 * 
	 * @param indexName
	 *            the name of the index
	 * @return whether there are any servers working on the index specified
	 */
	public boolean anyWorkingOnIndex(String indexName);

	/** Below here the access to the cluster wide cache. */

	public List<Url> getBatch(int size);

	public <T> T get(Class<T> klass, String sql);

	public <T> void set(Class<T> klass, Long id, T t);

	public <T> void clear(Class<T> klass);

	public <T> int size(Class<T> klass);

}