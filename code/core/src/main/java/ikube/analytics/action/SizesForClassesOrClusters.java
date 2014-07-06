package ikube.analytics.action;

import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;

import java.io.Serializable;

import static ikube.toolkit.ApplicationContextManager.getBean;

/**
 * This class is just a serializable snippet of logic that can be distributed over the
 * wire and executed on a remote server, essentially distributing the analysis throughout
 * the cluster.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class SizesForClassesOrClusters extends Action<Analysis> implements Serializable {

    /**
     * The analysis object to use for the analysis
     */
    private Analysis analysis;

    public SizesForClassesOrClusters(final Analysis analysis) {
        this.analysis = analysis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Analysis call() throws Exception {
        String clazz = analysis.getClazz();
        // Get the local analytics service and execute the analysis
        getBean(IAnalyticsService.class).classesOrClusters(analysis);
        Object[] classesOrClusters = analysis.getClassesOrClusters();
        int[] sizesForClassesOrClusters = new int[analysis.getClassesOrClusters().length];
        IAnalyzer analyzer = getBean(IAnalyticsService.class).getAnalyzer(analysis.getAnalyzer());
        // System.out.println("Analytics service : " + analyticsService + ", " + analyzer);
        // Calculate the sizes for the classes or clusters, as the case may be
        for (int i = 0; i < classesOrClusters.length; i++) {
            if (classesOrClusters[i] == null) {
                continue;
            }
            analysis.setClazz(classesOrClusters[i].toString());
            int sizeForClass = analyzer.sizeForClassOrCluster(analysis);
            sizesForClassesOrClusters[i] = sizeForClass;
        }
        analysis.setClazz(clazz);
        analysis.setSizesForClassesOrClusters(sizesForClassesOrClusters);
        // And return the analysis object to the local machine
        return analysis;
    }
}
