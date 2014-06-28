package ikube.analytics;

import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * TODO: Make this not static
     */
    public static IAnalyzer buildAnalyzer(final Context context) throws Exception {
        return buildAnalyzer(context, Boolean.FALSE);
    }

    /**
     * TODO: Make this not static
     */
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

    @Value("${analyzer-manager-wait}")
    private long waitToBuildAnalyzers;

    public Map<String, Context> getContexts() {
        return CONTEXTS;
    }

    public Collection<IAnalyzer> buildAnalyzers(final Collection<Context> contexts) throws Exception {
        Collection<IAnalyzer> analyzers = new ArrayList<>();
        for (final Context context : contexts) {
            IAnalyzer analyzer = buildAnalyzer(context);
            analyzers.add(analyzer);
        }
        return analyzers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        // Build the analyzers in parallel using all the cores
        final String name = "analyzer-builder";
        class Builder implements Runnable {
            Context context;

            @Override
            public void run() {
                try {
                    LOGGER.info("Context : " + context.getName());
                    buildAnalyzer(context, Boolean.FALSE);
                } catch (final Exception e) {
                    throw new RuntimeException("Error building analyzer : " + context.getName(), e);
                }
            }
        }
        class Starter implements Runnable {
            @Override
            public void run() {
                ThreadUtilities.sleep(waitToBuildAnalyzers);
                Map<String, Context> contexts = applicationContext.getBeansOfType(Context.class);
                for (final Map.Entry<String, Context> mapEntry : contexts.entrySet()) {
                    Builder builder = new Builder();
                    builder.context = mapEntry.getValue();
                    ThreadUtilities.submit(name, builder);
                }
            }
        }
        ThreadUtilities.submit(name, new Starter());
    }

}