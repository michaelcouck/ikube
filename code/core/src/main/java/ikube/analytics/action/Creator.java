package ikube.analytics.action;

import ikube.analytics.AnalyzerManager;
import ikube.model.Context;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * This class is just a serializable snippet of logic that can be distributed over the
 * wire and executed on a remote server, essentially creating the same analyzer in each
 * server in the cluster so that the analysis can be distributed throughout the cluster.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Creator extends Action<Void> implements Serializable {

    /**
     * The context object that will be used for creating the analyzer
     */
    private Context context;

    public Creator(final Context context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Void call() throws Exception {
        Object analyzerName = context.getAnalyzerInfo().getAnalyzer();
        Object algorithmName = context.getAnalyzerInfo().getAlgorithm();
        Object filterName = context.getAnalyzerInfo().getFilter();
        context.setAnalyzer(Class.forName(String.valueOf(analyzerName)).newInstance());
        context.setAlgorithm(Class.forName(String.valueOf(algorithmName)).newInstance());
        if (filterName != null && !StringUtils.isEmpty(String.valueOf(filterName))) {
            context.setFilter(Class.forName(String.valueOf(filterName)).newInstance());
        }
        // Build and set the analyzer here in the remote machine
        AnalyzerManager.buildAnalyzer(context);
        return null;
    }
}
