package ikube.analytics;

import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * This factory for analyzers is implemented as builder. Typically taking multiple simple objects
 * and building a complex one, all the time shielding the complex object from the complexity of it's
 * construction.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */
public class AnalyzerManager implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerManager.class);
    private static final Map<String, Context> CONTEXTS = new HashMap<>();

    public static Map<String, Context> getContexts() {
        return CONTEXTS;
    }

    public static Collection<IAnalyzer> buildAnalyzers(final Collection<Context> contexts) throws Exception {
        Collection<IAnalyzer> analyzers = new ArrayList<>();
        for (final Context context : contexts) {
            IAnalyzer analyzer = buildAnalyzer(context);
            analyzers.add(analyzer);
        }
        return analyzers;
    }

    public static IAnalyzer buildAnalyzer(final Context context) throws Exception {
        return buildAnalyzer(context, Boolean.FALSE);
    }

    public static IAnalyzer buildAnalyzer(final Context context, final boolean waitForBuild) throws Exception {
        final IAnalyzer analyzer = (IAnalyzer) context.getAnalyzer();
        class Builder implements Runnable {
            public void run() {
                try {
                    LOGGER.info("Initializing analyzer : " + context.getName());
                    analyzer.init(context);
                    LOGGER.info("Building analyzer : " + context.getName());
                    analyzer.build(context);
                    LOGGER.info("Analyzer built and ready : " + context.getName());
                } catch (final Exception e) {
                    LOGGER.error("Exception building analyzer : " + analyzer, e);
                } finally {
                    ThreadUtilities.destroy(this.toString());
                }
            }
        }
        Builder builder = new Builder();
        if (!ThreadUtilities.isInitialized()) {
            ThreadUtilities.initialize();
        }
        Future future = ThreadUtilities.submit(builder.toString(), builder);
        if (waitForBuild) {
            // We'll wait until the analyzer is built completely
            ThreadUtilities.waitForFuture(future, Integer.MAX_VALUE);
        } else {
            // We'll wait a bit for the future to end, but potentially
            // this process can take hours, to build a large classifier of a million
            // vectors for example, so we return
            ThreadUtilities.waitForFuture(future, 3);
        }
        CONTEXTS.put(context.getName(), context);
        LOGGER.info("Analyzer finished building : " + future.isDone());
        return analyzer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        // Build the analyzers in parallel using all the cores
        final String name = "analyzer-builder";
        class Starter implements Runnable {
            @Override
            public void run() {
                ThreadUtilities.sleep(15000);
                Map<String, Context> contexts = applicationContext.getBeansOfType(Context.class);
                for (final Map.Entry<String, Context> mapEntry : contexts.entrySet()) {
                    class Builder implements Runnable {
                        @Override
                        public void run() {
                            try {
                                LOGGER.info("Context : " + mapEntry.getKey() + ", " + mapEntry.getValue().getName());
                                buildAnalyzer(mapEntry.getValue(), Boolean.FALSE);
                            } catch (final Exception e) {
                                throw new RuntimeException("Error building analyzer : " + mapEntry.getKey(), e);
                            }
                        }
                    }
                    ThreadUtilities.submit(name, new Builder());
                }
            }
        }
        ThreadUtilities.submit("analyzer-builder", new Starter());
    }

}