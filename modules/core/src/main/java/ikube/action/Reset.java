package ikube.action;

import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Url;

/**
 * This class resets the data in the cluster. It is imperative that nothing gets reset if there are any servers working of course. The urls
 * that are published into the cluster during the indexing need to be deleted. This deletion action will delete them not only from this
 * server's map buy from all the servers' maps.
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
		getClusterManager().clear(Server.class);
		return Boolean.TRUE;
	}

}