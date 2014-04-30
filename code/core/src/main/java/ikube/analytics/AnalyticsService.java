package ikube.analytics;

import ikube.analytics.action.*;
import ikube.cluster.IClusterManager;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.HashMap;
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
public class AnalyticsService<I, O, C> implements IAnalyticsService<I, O, C> {

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
	@SuppressWarnings({ "UnusedDeclaration", "SpringJavaAutowiringInspection" })
    private IClusterManager clusterManager;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> create(final Context context) {
        logger.info("Create analytics service : " + context.getName());
        context.setFilter(null);
        context.setAlgorithm(null);
        Creator creator = new Creator(context);
        List<Future<Void>> futures = clusterManager.sendTaskToAll(creator);
        ThreadUtilities.waitForFutures(futures, 15);
        logger.info("Finished building remote analyzer : " + context.getName());
        return getAnalyzer(context.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> train(final Analysis<I, O> analysis) {
        Trainer trainer = new Trainer(analysis);
        List<Future<Void>> futures = clusterManager.sendTaskToAll(trainer);
        ThreadUtilities.waitForFutures(futures, 15);
        return getAnalyzer(analysis.getAnalyzer());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> build(final Analysis<I, O> analysis) {
        Builder builder = new Builder(analysis);
        List<Future<Void>> futures = clusterManager.sendTaskToAll(builder);
        ThreadUtilities.waitForFutures(futures, 15);
        return getAnalyzer(analysis.getAnalyzer());
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
				// TODO: User this rather than the future directly
				// ThreadUtilities.waitForFuture(future, 60 * 60);
                return (Analysis<I, O>) future.get(60 * 60, TimeUnit.SECONDS);
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
    public Analysis classesOrClusters(final Analysis<I, O> analysis) {
        // Create the callable that will be executed on the remote node
        ClassesOrClusters classesOrClusters = new ClassesOrClusters(analysis);
        if (analysis.isDistributed()) {
            // Set the flag so we don't get infinite recursion
            analysis.setDistributed(Boolean.FALSE);
            Future<?> future = clusterManager.sendTask(classesOrClusters);
            try {
				// TODO: User this rather than the future directly
				// ThreadUtilities.waitForFuture(future, 60 * 60);
                return (Analysis) future.get(60 * 60, TimeUnit.SECONDS);
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return classesOrClusters.call();
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
                return (Analysis<I, O>) future.get(60 * 60, TimeUnit.SECONDS);
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
        List<Future<Void>> futures = clusterManager.sendTaskToAll(destroyer);
        ThreadUtilities.waitForFutures(futures, 60);
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
    public Map<String, IAnalyzer> getAnalyzers() {
        Map<String, Context> contexts = getContexts();
        Map<String, IAnalyzer> analyzers = new HashMap<>();
        for (final Map.Entry<String, Context> mapEntry : contexts.entrySet()) {
            analyzers.put(mapEntry.getKey(), (IAnalyzer) mapEntry.getValue().getAnalyzer());
        }
        return analyzers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Context> getContexts() {
        return AnalyzerManager.getContexts();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> getAnalyzer(final String analyzerName) {
        Context context = getContext(analyzerName);
        if (context == null) {
            return null;
        }
        return (IAnalyzer<I, O, C>) context.getAnalyzer();
    }

}