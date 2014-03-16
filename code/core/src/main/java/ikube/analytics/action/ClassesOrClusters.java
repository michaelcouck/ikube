package ikube.analytics.action;

import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class ClassesOrClusters extends Action<Analysis> {

    private Analysis analysis;

    public ClassesOrClusters(final Analysis analysis) {
        this.analysis = analysis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Analysis call() throws Exception {
        IAnalyzer analyzer = getAnalyticsService().getAnalyzer(analysis.getAnalyzer());
        // System.out.println("Classes or clusters remotely : " + analyzer);
        Object[] classesOrClusters = analyzer.classesOrClusters();
        analysis.setClassesOrClusters(classesOrClusters);
        return analysis;
    }
}
