package ikube.analytics;

import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
        final IAnalyzer analyzer = (IAnalyzer) context.getAnalyzer();
        class Builder implements Runnable {
            public void run() {
                try {
                    LOGGER.info("Initializing analyzer : " + context.getName());
                    analyzer.init(context);
                    LOGGER.info("Building analyzer : " + context.getName());
                    analyzer.build(context);
                    LOGGER.info("Analyzer built and ready : " + context.getName());
                    if (context.getAnalyzerInfo() != null) {
                        context.getAnalyzerInfo().setBuilt(Boolean.TRUE);
                    }
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
        if (future != null && future.isDone()) {
            LOGGER.info("Analyzer finished building : " + future.isDone());
        } else {
            LOGGER.info("Analyzer still building : " + context.getName());
        }
        return analyzer;
    }

}