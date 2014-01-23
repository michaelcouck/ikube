package ikube.analytics;

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

    public static IAnalyzer<?, ?>[] buildAnalyzers(final IAnalyzer.IContext[] contexts) throws Exception {
        Collection<IAnalyzer<?, ?>> analyzers = new ArrayList<>();
        for (final IAnalyzer.IContext context : contexts) {
            IAnalyzer<?, ?> analyzer = (IAnalyzer<?, ?>) context.getAnalyzer();
            LOGGER.info("Building analyzer : " + context + ", " + analyzer);
            analyzer.init(context);
            analyzer.build(context);
            analyzers.add(analyzer);
        }
        return analyzers.toArray(new IAnalyzer<?, ?>[analyzers.size()]);
    }

}