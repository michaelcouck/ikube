package ikube.analytics;

import ikube.cluster.IClusterManager;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

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

    /**
     * Remote class to call for creation.
     */
    public class Creator implements Callable<IAnalyzer> {

        private Context context;

        public Creator(final Context context) {
            this.context = context;
        }

        @Override
        @SuppressWarnings("unchecked")
        public IAnalyzer call() throws Exception {
            // Instantiate the classifier, the algorithm and the filter
            Object analyzerName = context.getAnalyzerInfo().getAnalyzer();
            Object algorithmName = context.getAnalyzerInfo().getAlgorithm();
            Object filterName = context.getAnalyzerInfo().getFilter();
            context.setAnalyzer(Class.forName(String.valueOf(analyzerName)).newInstance());
            context.setAlgorithm(Class.forName(String.valueOf(algorithmName)).newInstance());
            if (filterName != null && !StringUtils.isEmpty(String.valueOf(filterName))) {
                context.setFilter(Class.forName(String.valueOf(filterName)).newInstance());
            }
            return AnalyzerManager.buildAnalyzer(context);
        }
    }

    /**
     * Remote class to call for training.
     */
    public class Trainer implements Callable<IAnalyzer> {

        private Analysis analysis;

        public Trainer(final Analysis analysis) {
            this.analysis = analysis;
        }

        @Override
        @SuppressWarnings("unchecked")
        public IAnalyzer call() throws Exception {
            IAnalyzer analyzer = getAnalyzer(analysis.getAnalyzer());
            analyzer.train(analysis);
            return analyzer;
        }
    }

    /**
     * Remote class to call for building.
     */
    public class Builder implements Callable<IAnalyzer> {

        private Analysis analysis;

        public Builder(final Analysis analysis) {
            this.analysis = analysis;
        }

        @Override
        @SuppressWarnings("unchecked")
        public IAnalyzer call() throws Exception {
            Context context = getContext(analysis.getAnalyzer());
            IAnalyzer analyzer = (IAnalyzer) context.getAnalyzer();
            analyzer.build(context);
            return analyzer;
        }
    }

    /**
     * Remote class to call for analyzing.
     */
    public class Analyzer implements Callable<Analysis> {

        private Analysis analysis;

        public Analyzer(final Analysis analysis) {
            this.analysis = analysis;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Analysis call() throws Exception {
            IAnalyzer<I, O, C> analyzer = getAnalyzer(analysis.getAnalyzer());
            analyzer.analyze((I) analysis);
            return analysis;
        }
    }

    /**
     * Remote class to call for classes or clusters.
     */
    public class ClassesOrClusters implements Callable<Analysis> {

        private Analysis analysis;

        public ClassesOrClusters(final Analysis analysis) {
            this.analysis = analysis;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Analysis call() throws Exception {
            IAnalyzer analyzer = getAnalyzer(analysis.getAnalyzer());
            Object[] classesOrClusters = analyzer.classesOrClusters();
            analysis.setClassesOrClusters(classesOrClusters);
            return analysis;
        }
    }

    /**
     * Remote class to call for sizes of classes or clusters.
     */
    public class SizesForClassesOrClusters implements Callable<Analysis> {

        private Analysis analysis;

        public SizesForClassesOrClusters(final Analysis analysis) {
            this.analysis = analysis;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Analysis call() throws Exception {
            String clazz = analysis.getClazz();
            classesOrClusters(analysis);
            Object[] classesOrClusters = analysis.getClassesOrClusters();
            int[] sizesForClassesOrClusters = new int[analysis.getClassesOrClusters().length];
            IAnalyzer analyzer = getAnalyzer(analysis.getAnalyzer());
            for (int i = 0; i < classesOrClusters.length; i++) {
                analysis.setClazz(classesOrClusters[i].toString());
                int sizeForClass = analyzer.sizeForClassOrCluster(analysis);
                sizesForClassesOrClusters[i] = sizeForClass;
            }
            analysis.setClazz(clazz);
            analysis.setSizesForClassesOrClusters(sizesForClassesOrClusters);
            return analysis;
        }
    }

    /**
     * Remote class to call for destroying the analyzer.
     */
    public class Destroyer implements Callable {

        private Context context;

        public Destroyer(final Context context) {
            this.context = context;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object call() throws Exception {
            Context context = getContexts().remove(this.context.getName());
            if (context != null) {
                IAnalyzer analyzer = (IAnalyzer) context.getAnalyzer();
                if (analyzer != null) {
                    try {
                        analyzer.destroy(context);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                    return analyzer;
                }
            }
            return null;
        }
    }

    @Autowired
    private IClusterManager clusterManager;

    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> create(final Context context) {
        Creator creator = new Creator(context);
        List<Future<IAnalyzer>> futures = clusterManager.sendTaskToAll(creator);
        ThreadUtilities.waitForFutures(futures, 15);
        return getAnalyzer(context.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> train(final Analysis<I, O> analysis) {
        Trainer trainer = new Trainer(analysis);
        List<Future<IAnalyzer>> futures = clusterManager.sendTaskToAll(trainer);
        ThreadUtilities.waitForFutures(futures, 15);
        return getAnalyzer(analysis.getAnalyzer());
    }

    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> build(final Analysis<I, O> analysis) {
        Builder builder = new Builder(analysis);
        List<Future<IAnalyzer>> futures = clusterManager.sendTaskToAll(builder);
        ThreadUtilities.waitForFutures(futures, 15);
        return getAnalyzer(analysis.getAnalyzer());
    }

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
                return (Analysis) future.get(60, TimeUnit.SECONDS);
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

    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> destroy(final Context context) {
        // Create the callable that will be executed on the remote node
        Destroyer destroyer = new Destroyer(context);
        Future<?> future = clusterManager.sendTask(destroyer);
        ThreadUtilities.waitForFuture(future, 60);
        try {
            return (IAnalyzer<I, O, C>) destroyer.call();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Context getContext(final String name) {
        return getContexts().get(name);
    }

    @Override
    public Map<String, IAnalyzer> getAnalyzers() {
        Map<String, Context> contexts = getContexts();
        Map<String, IAnalyzer> analyzers = new HashMap<>();
        for (final Map.Entry<String, Context> mapEntry : contexts.entrySet()) {
            analyzers.put(mapEntry.getKey(), (IAnalyzer) mapEntry.getValue().getAnalyzer());
        }
        return analyzers;
    }

    @Override
    public Map<String, Context> getContexts() {
        return AnalyzerManager.getContexts();
    }

    @SuppressWarnings("unchecked")
    IAnalyzer<I, O, C> getAnalyzer(final String analyzerName) {
        Context context = getContext(analyzerName);
        if (context == null) {
            return null;
        }
        return (IAnalyzer<I, O, C>) context.getAnalyzer();
    }

}