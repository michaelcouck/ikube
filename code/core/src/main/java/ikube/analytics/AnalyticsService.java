package ikube.analytics;

import ikube.analytics.action.*;
import ikube.cluster.IClusterManager;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class is implemented as a state pattern. The user specifies the type of analyzer, and the
 * service 'connects' to the correct implementation and executes the analysis logic.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class AnalyticsService<I, O> implements IAnalyticsService<I, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsService.class);

    @Autowired
    private Map<String, Context> contexts;
    @Autowired
    private IClusterManager clusterManager;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Context create(final Context context) {
        Creator creator = new Creator(context);
        List<Future<Boolean>> futures = clusterManager.sendTaskToAll(creator);
        ThreadUtilities.waitForFutures(futures, 15);
        LOGGER.debug("Contexts : " + getContexts());
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Context train(final Analysis<I, O> analysis) {
        Trainer trainer = new Trainer(analysis);
        List<Future<Boolean>> futures = clusterManager.sendTaskToAll(trainer);
        ThreadUtilities.waitForFutures(futures, 15);
        LOGGER.debug("Contexts : " + getContexts());
        return getContext(analysis.getContext());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Context build(final Analysis<I, O> analysis) {
        Builder builder = new Builder(analysis);
        List<Future<Boolean>> futures = clusterManager.sendTaskToAll(builder);
        ThreadUtilities.waitForFutures(futures, 15);
        LOGGER.debug("Contexts : " + getContexts());
        return getContext(analysis.getContext());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Analysis<I, O> analyze(final Analysis<I, O> analysis) {
        Analyzer analyzer = new Analyzer(analysis);
        if (analysis.isDistributed()) {
            // Set the flag so we don't get infinite recursion
            analysis.setDistributed(Boolean.FALSE);
            // Create the callable that will be executed on one of the nodes
            Future<?> future = clusterManager.sendTask(analyzer);
            try {
                return (Analysis<I, O>) future.get(60, TimeUnit.SECONDS);
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return analyzer.call();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Analysis<I, O> sizesForClassesOrClusters(final Analysis<I, O> analysis) {
        // Create the callable that will be executed on the remote node
        SizesForClassesOrClusters sizesForClassesOrClusters = new SizesForClassesOrClusters(analysis);
        if (analysis.isDistributed()) {
            // Set the flag so we don't get infinite recursion
            analysis.setDistributed(Boolean.FALSE);
            Future<?> future = clusterManager.sendTask(sizesForClassesOrClusters);
            try {
                return (Analysis<I, O>) future.get(60, TimeUnit.SECONDS);
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return sizesForClassesOrClusters.call();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void destroy(final Context context) {
        // Create the callable that will be executed on the remote node
        Destroyer destroyer = new Destroyer(context);
        List<Future<Boolean>> futures = clusterManager.sendTaskToAll(destroyer);
        ThreadUtilities.waitForFutures(futures, 15);
        LOGGER.debug("Contexts : " + getContexts());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context getContext(final String name) {
        LOGGER.debug("Contexts : " + getContexts());
        return getContexts().get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Context> getContexts() {
        LOGGER.debug("Contexts : " + contexts);
        return contexts;
    }

}