package ikube.analytics;

import ikube.model.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

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

    public static Collection<IAnalyzer<?, ?>> buildAnalyzers(final Collection<Context> contexts) throws Exception {
        Collection<IAnalyzer<?, ?>> analyzers = new ArrayList<>();
        LOGGER.info("Building analyzer : " + contexts.size());
        for (final Context context : contexts) {
            IAnalyzer<?, ?> analyzer = buildAnalyzer(context);
            LOGGER.info("Building analyzer : " + context.getName());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("Analyzer output : " + analyzer);
            }
            analyzers.add(analyzer);
        }
        return analyzers;
    }

    public static IAnalyzer<?, ?> buildAnalyzer(final Context context) throws Exception {
        IAnalyzer<?, ?> analyzer = (IAnalyzer<?, ?>) context.getAnalyzer();
        analyzer.init(context);
        analyzer.build(context);
        return analyzer;
    }

}