package ikube.analytics;

import ikube.model.Context;
import ikube.toolkit.THREAD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class AnalyzerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerManager.class);

    public void buildAnalyzers(final Map<String, Context> contexts) throws Exception {
        for (final Map.Entry<String, Context> mapEntry : contexts.entrySet()) {
            buildAnalyzer(mapEntry.getValue());
        }
    }

    public IAnalyzer buildAnalyzer(final Context context) throws Exception {
        return buildAnalyzer(context, Boolean.FALSE);
    }

    public IAnalyzer buildAnalyzer(final Context context, final boolean waitForBuild) throws Exception {
        if (String.class.isAssignableFrom(context.getAnalyzer().getClass())) {
            context.setAnalyzer(Class.forName(context.getAnalyzer().toString()).newInstance());
        }
        final IAnalyzer analyzer = (IAnalyzer) context.getAnalyzer();
        class Builder implements Runnable {
            public void run() {
                try {
                    LOGGER.info("Building analyzer : " + context.getName());
                    analyzer.init(context);
                    analyzer.build(context);
                    context.setBuilt(Boolean.TRUE);
                    LOGGER.info("Analyzer built and ready : " + context.getName());
                } catch (final Exception e) {
                    LOGGER.error("Exception building analyzer : " + analyzer + ", " + context.getName(), e);
                } finally {
                    THREAD.destroy(this.toString());
                }
            }
        }
        Builder builder = new Builder();
        if (!THREAD.isInitialized()) {
            THREAD.initialize();
        }
        Future future = THREAD.submit(builder.toString(), builder);
        if (waitForBuild) {
            // We'll wait until the analyzer is built completely
            THREAD.waitForFuture(future, Integer.MAX_VALUE);
        }
        if (future != null && future.isDone()) {
            LOGGER.info("Analyzer finished building : " + context.getName());
        } else {
            LOGGER.info("Analyzer still building : " + context.getName());
        }
        return analyzer;
    }

}