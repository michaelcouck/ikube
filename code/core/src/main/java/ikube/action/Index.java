package ikube.action;

import ikube.action.index.IndexManager;
import ikube.action.index.handler.IIndexableHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.Server;
import ikube.toolkit.ThreadUtilities;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.IndexWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * This class executes the handlers on the indexables, effectively creating the index. Each indexable has a handler
 * that is implemented to handle it. Each handler will return a list of threads that will do the indexing. The caller(in
 * this case, this class) must then wait for the threads to finish.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public class Index extends Action<IndexContext, Boolean> {

    /**
     * This is the list of handlers for all the sources of data. One such handler would be the
     * {@link ikube.action.index.handler.database.IndexableTableHandler} for example,
     * {@link ikube.action.index.handler.filesystem.IndexableFileSystemHandler} would be another.
     */
    private List<IIndexableHandler> indexableHandlers;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean preExecute(final IndexContext indexContext) throws Exception {
        logger.info("Pre process action : " + this.getClass() + ", " + indexContext.getName());
        Server server = clusterManager.getServer();
        IndexWriter[] indexWriters;
        if (indexContext.isDelta()) {
            indexWriters = IndexManager.openIndexWriterDelta(indexContext);
        } else {
            IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), server.getAddress());
            indexWriters = new IndexWriter[]{indexWriter};
        }
        indexContext.setIndexWriters(indexWriters);
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected boolean internalExecute(final IndexContext indexContext) throws Exception {
        List<Indexable> indexables = indexContext.getChildren();
        for (final Indexable indexable : (Iterable<Indexable>) new ArrayList(indexables)) {
            indexable.setExceptions(0);
            // Update the action with the new indexable
            Server server = clusterManager.getServer();
            ikube.model.Action action = getAction(server, indexContext, null);
            // Now we set the current indexable name in the action
            String indexableName = indexable.getName();
            action.setIndexableName(indexableName);
            dataBase.merge(action);
            clusterManager.put(server.getAddress(), server);
            // Get the right handler for this indexable
            IIndexableHandler<Indexable> handler = getHandler(indexable);
            // Execute the handler and wait for the threads to finish
            logger.info("Indexable : " + indexable.getName());
            // This task it potentially the grand parent of multiple sub
            // tasks that have been split off recursively by the handler
            ForkJoinTask<?> forkJoinTask = handler.handleIndexableForked(indexContext, indexable);
            ThreadUtilities.executeForkJoinTasks(indexContext.getName(), indexable.getThreads(), forkJoinTask);
            ThreadUtilities.waitForFuture(forkJoinTask, Long.MAX_VALUE);
            logger.info("Finished indexable : " + indexable.getName());
        }
        return Boolean.TRUE;
    }

    /**
     * This method will return the action from the server that represents this logical action. To co-ordinate
     * the actions in the cluster, the server objects contain the actions that are being executed on the local
     * machine. The server objects are then distributed in the cluster in the grid.
     *
     * @param server        the server, typically the local server object, to get the action for
     * @param indexContext  the index context that is currently executing in this action
     * @param indexableName the indexable that is currently being indexed by this instance
     * @return the action from the server that is currently being executed, with
     */
    ikube.model.Action getAction(final Server server, final IndexContext indexContext, final String indexableName) {
        for (final ikube.model.Action action : server.getActions()) {
            if (action.getActionName().equals(this.getClass().getSimpleName())) {
                if (!action.getIndexName().equals(indexContext.getName())) {
                    continue;
                }
                if (StringUtils.isEmpty(indexableName)) {
                    // We return the first action in the case the indexable name has not been set. This
                    // is quite common because when the action is started, the logic that executes the action
                    // does not know which indexable will be indexed first
                    return action;
                } else {
                    // Else we look for the action that has the current indexable name
                    if (indexableName.equals(action.getIndexableName())) {
                        return action;
                    }
                }
            }
        }
        // This never gets thrown
        throw new RuntimeException("Action not found for class : " + this.getClass().getSimpleName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean postExecute(final IndexContext indexContext) throws Exception {
        logger.info("Post process action : " + this.getClass() + ", " + indexContext.getName());
        IndexManager.closeIndexWriters(indexContext);
        indexContext.setIndexWriters();
        return Boolean.TRUE;
    }

    /**
     * This method finds the correct handler for the indexable.
     *
     * @param indexable the indexable to find the handler for
     * @return the handler for the indexable or null if there is no handler for the indexable. This
     * will fail with a warning if there is no handler for the indexable
     */
    protected IIndexableHandler getHandler(final Indexable indexable) {
        for (final IIndexableHandler handler : indexableHandlers) {
            if (handler.getIndexableClass().equals(indexable.getClass())) {
                return handler;
            }
        }
        throw new RuntimeException("No handler defined for indexable : " + indexable);
    }

    public void setIndexableHandlers(final List<IIndexableHandler> indexableHandlers) {
        this.indexableHandlers = indexableHandlers;
    }

}