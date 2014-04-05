package ikube.action;

import ikube.model.IndexContext;
import ikube.model.Server;

import java.util.Date;
import java.util.Map;

/**
 * TODO: This must still be completed.
 * <p/>
 * The possible implementations are:
 * 1) User ftp for linux
 * 2) User the java ssh library
 * 3) User a remote task in Hazelcast that compresses a chunk of file at a time
 * <p/>
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
        SynchronizeLatestIndexCallable synchronizeLatestIndexCallable = new SynchronizeLatestIndexCallable(indexContext);
        clusterManager.sendTaskToAll(synchronizeLatestIndexCallable);
        return Boolean.TRUE;
    }

}