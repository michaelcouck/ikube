package ikube.action.rule;

import ikube.model.IndexContext;
import ikube.toolkit.ThreadUtilities;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * This rule checks to see if there are any remote indexes for the target index context
 * that are current, i.e. not out of date and not corrupt.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 30-03-2014
 */
public class IsRemoteIndexCurrent extends ARule<IndexContext> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean evaluate(final IndexContext indexContext) {
        IsIndexCurrentCallable isIndexCurrentCallable = new IsIndexCurrentCallable(indexContext);
        List<Future<Boolean>> futures = clusterManager.sendTaskToAll(isIndexCurrentCallable);
        ThreadUtilities.waitForFutures(futures, 60);
        boolean isRemoteIndexCurrent = Boolean.FALSE;
        for (final Future<Boolean> future : futures) {
            if (!future.isDone()) {
                logger.info("Remote execution not finished : " + future);
                continue;
            }
            try {
                isRemoteIndexCurrent |= future.get();
            } catch (final InterruptedException | ExecutionException e) {
                logger.error("Exception getting result from remote future : ", e);
            }
        }
        logger.debug("Remote index created : " + isRemoteIndexCurrent);
        return isRemoteIndexCurrent;
    }

}