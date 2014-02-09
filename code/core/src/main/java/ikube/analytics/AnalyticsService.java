package ikube.analytics;

import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.Timer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is implemented as a state pattern. The user specifies the type of analyzer, and the service 'connects' to the correct
 * implementation and executes the analysis logic.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10.04.13
 */
public class AnalyticsService<I, O, C> implements IAnalyticsService<I, O, C>, BeanPostProcessor {

    private Map<String, Context> contexts = new HashMap<>();
    private Map<String, IAnalyzer> analyzers = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> create(final Context context) {
        try {
            // Instantiate the classifier, the algorithm and the filter
            Object algorithmName = context.getAlgorithm();
            context.setAlgorithm(Class.forName(String.valueOf(algorithmName)).newInstance());
            Object analyzerName = context.getAnalyzer();
            context.setAnalyzer(Class.forName(String.valueOf(analyzerName)).newInstance());
            Object filterName = context.getFilter();
            if (filterName != null && !StringUtils.isEmpty(String.valueOf(filterName))) {
                context.setFilter(Class.forName(String.valueOf(filterName)).newInstance());
            }

            IAnalyzer<I, O, C> analyzer = (IAnalyzer<I, O, C>) AnalyzerManager.buildAnalyzer(context);
            contexts.put(context.getName(), context);
            analyzers.put(context.getName(), analyzer);
            return analyzer;
        } catch (final Exception e) {
            throw new RuntimeException("Exception creating analyzer : " + context.getName(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> train(final Analysis<I, O> analysis) {
        final IAnalyzer<I, O, C> analyzer = getAnalyzer(analysis.getAnalyzer());
        try {
            analyzer.train((I) analysis);
        } catch (final Exception e) {
            throw new RuntimeException("Exception training analyzer : " + analysis, e);
        }
        return analyzer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> build(final Analysis<I, O> analysis) {
        Context context = getContext(analysis.getAnalyzer());
        IAnalyzer<I, O, C> analyzer = getAnalyzer(analysis.getAnalyzer());
        try {
            analyzer.build(context);
        } catch (final Exception e) {
            throw new RuntimeException("Exception building analyzer : " + context.getName(), e);
        }
        return analyzer;
    }

    @Override
    public Analysis<I, O> analyze(final Analysis<I, O> analysis) {
        final IAnalyzer<I, O, C> analyzer = getAnalyzer(analysis.getAnalyzer());
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            @SuppressWarnings("unchecked")
            public void execute() {
                try {
                    analyzer.analyze((I) analysis);
                } catch (final Exception e) {
                    analysis.setException(e);
                    throw new RuntimeException("Exception analyzing data : " + analyzer, e);
                }
            }
        });
        analysis.setDuration(duration);
        analysis.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return analysis;
    }

    public Analysis classesOrClusters(final Analysis<I, O> analysis) {
        try {
            IAnalyzer analyzer = getAnalyzer(analysis.getAnalyzer());
            Object[] classesOrClusters = analyzer.classesOrClusters();
            analysis.setClassesOrClusters(classesOrClusters);
            return analysis;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Analysis<I, O> sizesForClassesOrClusters(final Analysis<I, O> analysis) {
        try {
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
            analysis.setSizesForClassesOrClusters(sizesForClassesOrClusters);
            analysis.setClazz(clazz);
            return analysis;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> destroy(final Context context) {
        contexts.remove(context.getName());
        IAnalyzer<I, O, C> analyzer = analyzers.remove(context.getName());
        if (analyzer != null) {
            try {
                analyzer.destroy(context);
            } catch (Exception e) {
                throw new RuntimeException("Exception destroying analyzer : " + context, e);
            }
        }
        return analyzer;
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        // We collect all the analyzers that have been defined in the configuration here
        if (IAnalyzer.class.isAssignableFrom(bean.getClass())) {
            analyzers.put(beanName, (IAnalyzer) bean);
        } else if (Context.class.isAssignableFrom(bean.getClass())) {
            contexts.put(beanName, (Context) bean);
        }
        return bean;
    }

    @SuppressWarnings("unchecked")
    IAnalyzer<I, O, C> getAnalyzer(final String analyzerName) {
        Map<String, IAnalyzer> analyzers = getAnalyzers();
        IAnalyzer<?, ?, ?> analyzer = analyzers.get(analyzerName);
        return (IAnalyzer<I, O, C>) analyzer;
    }

    @Override
    public Context getContext(final String analyzerName) {
        IAnalyzer analyzer = getAnalyzer(analyzerName);
        for (final Map.Entry<String, Context> mapEntry : contexts.entrySet()) {
            if (mapEntry.getValue().getAnalyzer().equals(analyzer)) {
                return mapEntry.getValue();
            }
        }
        throw new RuntimeException("Couldn't find context for analyzer : " + analyzerName + ", " + analyzer);
    }

    @Override
    public Map<String, IAnalyzer> getAnalyzers() {
        return analyzers;
    }

    public Map<String, Context> getContexts() {
        return contexts;
    }
}