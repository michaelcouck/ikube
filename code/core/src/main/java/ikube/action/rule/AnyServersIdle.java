package ikube.action.rule;

import ikube.model.IndexContext;
import ikube.model.Server;

import java.util.Map;

/**
 * This rule checks to see if there are any servers idle in the cluster.
 * 
 * @author Michael Couck
 * @since 19.12.2012
 * @version 01.00
 */
public class AnyServersIdle extends ARule<IndexContext<?>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		Server thisServer = clusterManager.getServer();
		Map<String, Server> servers = clusterManager.getServers();
		for (Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			if (thisServer.getAddress().equals(server.getAddress())) {
				continue;
			}
			if (!server.isWorking()) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

}
