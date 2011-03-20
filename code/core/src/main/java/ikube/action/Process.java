package ikube.action;

import java.util.List;

import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Url;

/**
 * This class is for post processing the data, like for example getting all the GeoLocation data from the addresses.
 * 
 * @author Michael Couck
 * @since 05.03.2011
 * @version 01.00
 */
public class Process extends Action<IndexContext, Boolean> {

	@Override
	public Boolean execute(final IndexContext indexContext) {
		try {
			getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getName(), Boolean.TRUE);
			List<Server> servers = getClusterManager().getServers();
			for (Server server : servers) {
				server.getActions().clear();
			}
			for (Server server : servers) {
				getClusterManager().set(Server.class, server.getId(), server);
			}
			getClusterManager().clear(Url.class);
		} finally {
			getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getName(), Boolean.FALSE);
		}
		return Boolean.TRUE;
	}

}