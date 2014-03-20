package ikube.analytics;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.analytics.action.Analyzer;
import ikube.analytics.action.Builder;
import ikube.analytics.action.Destroyer;
import ikube.analytics.action.Trainer;
import ikube.analytics.weka.WekaClassifier;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.Analysis;
import ikube.model.AnalyzerInfo;
import ikube.model.Context;
import ikube.toolkit.ApplicationContextManager;
import mockit.Deencapsulation;
import mockit.Mockit;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import weka.classifiers.functions.SMO;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20-11-2013
 */
public class AnalyticsServiceTest extends AbstractTest {

    private Context context;
    private IAnalyzer analyzer;
    private Analysis<?, ?> analysis;
    private AnalyticsService analyticsService;

    @BeforeClass
    public static void beforeClass() {
        Mockit.setUpMocks(ApplicationContextManagerMock.class);
    }

    @AfterClass
    public static void afterClass() {
        Mockit.tearDownMocks(ApplicationContextManager.class);
    }

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        analyzer = mock(IAnalyzer.class);
        analysis = mock(Analysis.class);

        SMO smo = mock(SMO.class);
        Filter filter = mock(Filter.class);
        AnalyzerInfo analyzerInfo = mock(AnalyzerInfo.class);

        when(analyzerInfo.getAnalyzer()).thenReturn(WekaClassifier.class.getName());
        when(analyzerInfo.getAlgorithm()).thenReturn(SMO.class.getName());
        when(analyzerInfo.getFilter()).thenReturn(StringToWordVector.class.getName());

        analyticsService = new AnalyticsService();

        context = mock(Context.class);
        when(context.getAnalyzerInfo()).thenReturn(analyzerInfo);
        when(context.getAlgorithm()).thenReturn(smo);
        when(context.getAnalyzer()).thenReturn(analyzer);
        when(context.getFilter()).thenReturn(filter);

        AnalyzerManager.getContexts().put(context.getName(), context);

        Deencapsulation.setField(analyticsService, "clusterManager", clusterManager);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void create() throws Exception {
        analyticsService.create(context);
        verify(context, atLeastOnce()).setFilter(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        analyticsService.train(analysis);
        verify(clusterManager, atLeastOnce()).sendTaskToAll(any(Callable.class));

        Trainer trainer = new Trainer(analysis);
        IAnalyticsService service = trainer.getAnalyticsService();
        when(service.getAnalyzer(any(String.class))).thenReturn(analyzer);
        trainer.call();

        verify(analyzer, atLeastOnce()).train(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void build() throws Exception {
        analyticsService.build(analysis);
        verify(clusterManager, atLeastOnce()).sendTaskToAll(any(Callable.class));

        Builder builder = new Builder(analysis);
        IAnalyticsService service = builder.getAnalyticsService();
        when(service.getContext(any(String.class))).thenReturn(context);
        builder.call();

        verify(analyzer, atLeastOnce()).build(any(Context.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws Exception {
        Future future = mock(Future.class);
        when(clusterManager.sendTask(any(Callable.class))).thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class))).thenReturn(analysis);
        when(analyzer.analyze(any())).thenReturn(analysis);

        Analysis analysis = analyticsService.analyze(this.analysis);
        assertEquals(analysis, this.analysis);

        when(analysis.isDistributed()).thenReturn(Boolean.TRUE);
        analyticsService.analyze(this.analysis);
        verify(clusterManager, atLeastOnce()).sendTask(any(Callable.class));

        Analyzer analyzer = new Analyzer(analysis);
        IAnalyticsService service = analyzer.getAnalyticsService();
        when(service.getAnalyzer(any(String.class))).thenReturn(this.analyzer);
        analyzer.call();

        verify(this.analyzer, atLeastOnce()).analyze(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void classesOrClusters() throws Exception {
        Future future = mock(Future.class);
        when(clusterManager.sendTask(any(Callable.class))).thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class))).thenReturn(analysis);
        when(analyzer.analyze(any())).thenReturn(analysis);

        IAnalyticsService analyticsService = ApplicationContextManager.getBean(IAnalyticsService.class);
        when(analyticsService.getContexts()).thenReturn(AnalyzerManager.getContexts());
        Analysis analysis = this.analyticsService.classesOrClusters(this.analysis);
        assertEquals(analysis, this.analysis);

        when(this.analysis.isDistributed()).thenReturn(Boolean.TRUE);
        this.analyticsService.classesOrClusters(analysis);
        verify(analysis, atLeast(1)).setClassesOrClusters(any(Object[].class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void sizesForClassesOrClusters() throws Exception {
        Future future = mock(Future.class);
        when(clusterManager.sendTask(any(Callable.class))).thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class))).thenReturn(analysis);
        when(analyzer.analyze(any())).thenReturn(analysis);

        IAnalyticsService analyticsService = ApplicationContextManager.getBean(IAnalyticsService.class);
        when(analyticsService.getContexts()).thenReturn(AnalyzerManager.getContexts());
        when(analysis.getClassesOrClusters()).thenReturn(new Object[]{IConstants.POSITIVE, IConstants.NEGATIVE});
        Analysis analysis = this.analyticsService.sizesForClassesOrClusters(this.analysis);
        assertEquals(analysis, this.analysis);

        when(this.analysis.isDistributed()).thenReturn(Boolean.TRUE);
        this.analyticsService.sizesForClassesOrClusters(analysis);
        verify(analysis, atLeast(1)).setSizesForClassesOrClusters(any(int[].class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void destroy() throws Exception {
        analyticsService.destroy(context);
        verify(clusterManager, atLeastOnce()).sendTaskToAll(any(Callable.class));

        Destroyer destroyer = new Destroyer(context);
        IAnalyticsService service = destroyer.getAnalyticsService();
        when(service.getContexts()).thenReturn(AnalyzerManager.getContexts());
        destroyer.call();

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

}