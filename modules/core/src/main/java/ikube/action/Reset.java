package ikube.action;

import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Url;

/**
 * This class resets the data in the cluster. It is imperative that nothing gets reset if there are any servers working of course. The urls
 * that are published into the cluster during the indexing need to be deleted. This deletion action will delete them not only from this
 * server's map but from all the servers' maps.
 * 
 * The actions need to be cleaned when all the servers are finished working.
 * 
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Reset extends Action {

	@Override
	public Boolean execute(IndexContext indexContext) {
		if (getClusterManager().anyWorking()) {
			logger.info("Servers working : ");
			return Boolean.FALSE;
		}
		getClusterManager().clear(Url.class);
		for (Server server : getClusterManager().getServers()) {
			server.getActions().clear();
			getClusterManager().set(Server.class, server.getId(), server);
		}
		return Boolean.TRUE;
	}

}