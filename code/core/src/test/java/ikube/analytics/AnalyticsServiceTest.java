package ikube.analytics;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.analytics.weka.WekaClassifier;
import ikube.cluster.IClusterManager;
import ikube.cluster.hzc.ClusterManagerHazelcast;
import ikube.model.Analysis;
import ikube.model.Context;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import weka.classifiers.functions.SMO;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20.11.13
 */
public class AnalyticsServiceTest extends AbstractTest {

    @MockClass(realClass = AnalyzerManager.class)
    static class AnalyzerManagerMock {
        @Mock
        @SuppressWarnings("UnusedDeclaration")
        public static IAnalyzer<?, ?, ?> buildAnalyzer(final Context context) throws Exception {
            return null;
        }
    }

    private static IClusterManager CLUSTER_MANAGER;

    @BeforeClass
    public static void beforeClass() {
        CLUSTER_MANAGER = new ClusterManagerHazelcast();
        ((ClusterManagerHazelcast) CLUSTER_MANAGER).setListeners(Collections.EMPTY_LIST);
    }

    private IAnalyzer analyzer;
    private Analysis<?, ?> analysis;
    private AnalyticsService analyticsService;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        analyticsService = new AnalyticsService();
        analyzer = mock(IAnalyzer.class);
        analysis = mock(Analysis.class);

        when(analysis.getAnalyzer()).thenReturn(IConstants.ANALYZER);

        Map<String, IAnalyzer> analyzers = new HashMap<>();
        analyzers.put(analysis.getAnalyzer(), analyzer);
        Deencapsulation.setField(analyticsService, "analyzers", analyzers);
        Deencapsulation.setField(analyticsService, "clusterManager", CLUSTER_MANAGER);
        Mockit.setUpMocks(AnalyzerManagerMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(AnalyzerManager.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void create() {
        Context context = mock(Context.class);
        when(context.getAlgorithm()).thenReturn(SMO.class.getName());
        when(context.getAnalyzer()).thenReturn(WekaClassifier.class.getName());
        when(context.getFilter()).thenReturn(StringToWordVector.class.getName());
        analyticsService.create(context);
        verify(context, atLeastOnce()).setFilter(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        analyticsService.train(analysis);
        verify(analyzer, atLeastOnce()).train(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws Exception {
        Analysis analysis = analyticsService.analyze(this.analysis);
        assertEquals(this.analysis, analysis);
        verify(analyzer, atLeast(1)).analyze(any());

        when(this.analysis.isDistributed()).thenReturn(Boolean.TRUE);
        analysis = analyticsService.analyze(this.analysis);
        assertEquals(this.analysis, analysis);
        verify(analyzer, atLeast(2)).analyze(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void destroy() throws Exception {
        Context context = mock(Context.class);
        when(context.getAnalyzer()).thenReturn(analyzer);
        analyticsService.getAnalyzers().put(context.getName(), analyzer);
        analyticsService.destroy(context);
        verify(analyzer, atLeastOnce()).destroy(any(Context.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAnalyzer() throws Exception {
        IAnalyzer analyzer = analyticsService.getAnalyzer(analysis.getAnalyzer());
        assertEquals(this.analyzer, analyzer);
    }

    @Test
    public void getAnalyzers() throws Exception {
        Map analyzers = analyticsService.getAnalyzers();
        assertNotNull(analyzers);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void classesOrClusters() throws Exception {
        analyticsService.classesOrClusters(analysis);
        verify(analysis, atLeast(1)).setClassesOrClusters(any(Object[].class));

        when(this.analysis.isDistributed()).thenReturn(Boolean.TRUE);
        analyticsService.classesOrClusters(analysis);
        verify(analysis, atLeast(2)).setClassesOrClusters(any(Object[].class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void sizesForClassesOrClusters() {
        when(analysis.getClassesOrClusters()).thenReturn(new Object[]{IConstants.POSITIVE, IConstants.NEGATIVE});
        analyticsService.sizesForClassesOrClusters(analysis);
        verify(analysis, atLeast(1)).setSizesForClassesOrClusters(any(int[].class));

        when(this.analysis.isDistributed()).thenReturn(Boolean.TRUE);
        analyticsService.sizesForClassesOrClusters(analysis);
        verify(analysis, atLeast(2)).setSizesForClassesOrClusters(any(int[].class));
    }

}