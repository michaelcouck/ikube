package ikube.action;

import ikube.model.IndexContext;
import ikube.model.Server;

import java.util.Date;
import java.util.Map;

/**
 * This action will synchronize indexes between servers.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 08-04-2014
 */
@SuppressWarnings("UnusedDeclaration")
public class Synchronize extends Action<IndexContext, Boolean> {

    /**
     * {@inheritDoc}
     */
    @Override
    boolean internalExecute(final IndexContext indexContext) {
        Server server = clusterManager.getServer();
        Map<String, Server> servers = clusterManager.getServers();
        for (final Map.Entry<String, Server> mapEntry : servers.entrySet()) {
            if (mapEntry.getValue().getAddress().equals(server.getAddress())) {
                continue;
            }
            for (final IndexContext remoteIndexContext : server.getIndexContexts()) {
                if (!indexContext.getName().equals(remoteIndexContext.getName())) {
                    continue;
                }
                Date localTimestamp = indexContext.getSnapshot().getLatestIndexTimestamp();
                // Get the tiemstamp from the remote machines
                Date remoteTimestamp = remoteIndexContext.getSnapshot().getLatestIndexTimestamp();
                //noinspection StatementWithEmptyBody
                if (localTimestamp.before(remoteTimestamp)) {
                    // Get the latest one, and if it does not match the local one
                    // TODO: Copy the index to this server
                    // Write the index to a directory on the local file system
                    // Rename the directory once completely rewritten
                    // The automatic logic will open the index eventually
                }
            }
        }
        return Boolean.TRUE;
    }

}