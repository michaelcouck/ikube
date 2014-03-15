package ikube.analytics.action;

import ikube.analytics.AnalyzerManager;
import ikube.analytics.IAnalyzer;
import ikube.model.Context;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.Callable;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Creator implements Callable<IAnalyzer> {

    private Context context;

    public Creator(final Context context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer call() throws Exception {
        // Instantiate the classifier, the algorithm and the filter
        Object analyzerName = context.getAnalyzerInfo().getAnalyzer();
        Object algorithmName = context.getAnalyzerInfo().getAlgorithm();
        Object filterName = context.getAnalyzerInfo().getFilter();
        context.setAnalyzer(Class.forName(String.valueOf(analyzerName)).newInstance());
        context.setAlgorithm(Class.forName(String.valueOf(algorithmName)).newInstance());
        if (filterName != null && !StringUtils.isEmpty(String.valueOf(filterName))) {
            context.setFilter(Class.forName(String.valueOf(filterName)).newInstance());
        }
        return AnalyzerManager.buildAnalyzer(context);
    }
}
