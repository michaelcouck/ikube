package ikube.analytics.action;

import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class SizesForClassesOrClusters extends Action<Analysis> {

    private Analysis analysis;

    public SizesForClassesOrClusters(final Analysis analysis) {
        this.analysis = analysis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Analysis call() throws Exception {
        String clazz = analysis.getClazz();
        System.out.println("Sizes for classes and clusters remotely : " + clazz);
        getAnalyticsService().classesOrClusters(analysis);
        Object[] classesOrClusters = analysis.getClassesOrClusters();
        int[] sizesForClassesOrClusters = new int[analysis.getClassesOrClusters().length];
        IAnalyzer analyzer = getAnalyticsService().getAnalyzer(analysis.getAnalyzer());
        for (int i = 0; i < classesOrClusters.length; i++) {
            analysis.setClazz(classesOrClusters[i].toString());
            int sizeForClass = analyzer.sizeForClassOrCluster(analysis);
            sizesForClassesOrClusters[i] = sizeForClass;
        }
        analysis.setClazz(clazz);
        analysis.setSizesForClassesOrClusters(sizesForClassesOrClusters);
        return analysis;
    }
}
