package ikube.cluster;

import ikube.model.IndexContext;
import ikube.model.Server;

import java.util.Set;

public interface IClusterManager {

	/**
	 * Sets the working flag for this index context.
	 *
	 * @param indexContext
	 *            the index context that will start working
	 * @param actionName
	 *            the action that will be executed on the context
	 * @param isWorking
	 *            whether it is working or not
	 * @param start
	 *            the start time of the action
	 */
	public void setWorking(IndexContext indexContext, String actionName, boolean isWorking, long start);

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
	 * @param actionName
	 *            the name of the action we want to see if there are any servers working on
	 * @return whether there are any servers working on this index with this action, other than ourselves of course
	 */
	public boolean areWorking(String indexName, String actionName);

	/**
	 * Returns the last working time of any context in any server that has this name and is doing this action.
	 *
	 * @param indexName
	 *            the name of the index, could be 'faq' for example
	 * @param actionName
	 *            the name of the action that is being executed by the server, something like ikube.action.Index for example
	 * @return the start time of the first index context to start, or the current time if no other servers are executing any actions on this
	 *         context
	 */
	public long getLastWorkingTime(String indexName, String actionName);

	/**
	 * Returns the next id from one of the servers. The id would be the id in the table for this index.
	 *
	 * @param indexName
	 *            the name of the index currently getting executed
	 * @return the id of the next row in the table for this index
	 */
	public long getIdNumber(String indexName);

	public void setIdNumber(String indexName, long idNumber);

	public Set<Server> getServers();

	public Server getServer();

	/**
	 * Returns whether there are any servers working on this index.
	 *
	 * @return whether there are any servers working on the index specified
	 */
	public boolean anyWorkingOnIndex(IndexContext indexContext);

}