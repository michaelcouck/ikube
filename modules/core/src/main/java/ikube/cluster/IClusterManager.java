package ikube.cluster;

import ikube.model.IndexContext;
import ikube.model.Server;

import java.util.Map;

public interface IClusterManager {

	public Server getServer(IndexContext indexContext);

	public Boolean isWorking(IndexContext indexContext);

	public void setWorking(IndexContext indexContext, String actionName, boolean isWorking, long start);

	/**
	 * Returns whether any servers are working on any action other than this action.
	 *
	 *@param indexContext
	 *            the index context for the index
	 * @param actionName
	 *            the action name to check if any other servers are doing anything else
	 * @return whether there are any servers working on any other action than the one specified in the parameter list
	 */
	public boolean anyWorking(IndexContext indexContext, String actionName);

	/**
	 * Checks to see if there are any other servers working on this index, with this action.
	 *
	 * @param indexContext
	 *            the index context with the index name and the server name
	 * @param actionName
	 *            the name of the action we want to see if there are any servers working on
	 * @return whether there are any servers working on this index with this action, other than ourselves of course
	 */
	public boolean areWorking(IndexContext indexContext, String actionName);

	public long getLastWorkingTime(IndexContext indexContext, String actionName);

	public boolean resetWorkings(IndexContext indexContext, String actionName);

	public int getNextBatchNumber(IndexContext indexContext);

	public Map<String, Server> getServers(IndexContext indexContext);

}
