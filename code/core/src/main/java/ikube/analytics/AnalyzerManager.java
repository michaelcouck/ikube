package ikube.analytics;

import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;

/**
 * This factory for analyzers is implemented as builder. Typically taking multiple simple objects and building a complex one, all the time
 * shielding the complex object from the complexity of it's construction.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10.04.13
 */
public final class AnalyzerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerManager.class);

    public static Collection<IAnalyzer<?, ?, ?>> buildAnalyzers(final Collection<Context> contexts) throws Exception {
        Collection<IAnalyzer<?, ?, ?>> analyzers = new ArrayList<>();
        for (final Context context : contexts) {
            IAnalyzer<?, ?, ?> analyzer = buildAnalyzer(context);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("Analyzer output : " + analyzer);
            }
            analyzers.add(analyzer);
        }
        return analyzers;
    }

    public static IAnalyzer<?, ?, ?> buildAnalyzer(final Context context) throws Exception {
        final IAnalyzer<?, ?, ?> analyzer = (IAnalyzer<?, ?, ?>) context.getAnalyzer();
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
        // We'll wait a bit for the future to end, but potentially
        // this process can take hours, to build a large classifier of a million
        // vectors for example, so we return
        ThreadUtilities.waitForFuture(future, 3);
        LOGGER.info("Analyzer finished building : " + future.isDone());
        return analyzer;
    }

}