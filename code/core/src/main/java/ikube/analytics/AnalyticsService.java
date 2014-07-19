package ikube.analytics;

import ikube.analytics.action.*;
import ikube.cluster.IClusterManager;
import ikube.model.Analysis;
import ikube.model.Context;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
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

    static {
        // We register a converter for the Bean utils so it
        // doesn't complain when the value is null
        ConvertUtils.register(new Converter() {
            @Override
            public Object convert(final Class type, final Object value) {
                return value;
            }
        }, Timestamp.class);
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());

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
        logger.info("Create analytics service : " + context.getName());
        Creator creator = new Creator(context);
        clusterManager.sendTaskToAll(creator);
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Context train(final Analysis<I, O> analysis) {
        Trainer trainer = new Trainer(analysis);
        clusterManager.sendTaskToAll(trainer);
        return getContext(analysis.getContext());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Context build(final Analysis<I, O> analysis) {
        Builder builder = new Builder(analysis);
        clusterManager.sendTaskToAll(builder);
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
        clusterManager.sendTaskToAll(destroyer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context getContext(final String name) {
        return getContexts().get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Context> getContexts() {
        return contexts;
    }

}