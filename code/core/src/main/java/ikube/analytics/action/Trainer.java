package ikube.analytics.action;

import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;

import java.io.Serializable;

import static ikube.toolkit.ApplicationContextManager.getBean;

/**
 * This class is just a serializable snippet of logic that can be distributed over the
 * wire and executed on a remote server, essentially distributing the training throughout
 * the cluster.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Trainer extends Action<Void> implements Serializable {

    /**
     * The analysis object to use for the training
     */
    private Analysis analysis;

    public Trainer(final Analysis analysis) {
        this.analysis = analysis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Void call() throws Exception {
        // Get the analyzer on the local machine
        IAnalyzer analyzer = getBean(IAnalyticsService.class).getAnalyzer(analysis.getAnalyzer());
        // And train it
        analyzer.train(analysis);
        return null;
    }
}
