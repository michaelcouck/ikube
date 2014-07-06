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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import weka.classifiers.functions.SMO;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.HashMap;
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

    @Mock
    private SMO smo;
    @Mock
    private Filter filter;
    @Mock
    private AnalyzerInfo analyzerInfo;

    @Mock
    private Context context;
    @Mock
    private IAnalyzer analyzer;
    @Mock
    private Analysis<?, ?> analysis;

    @Mock
    private AnalyzerManager analyzerManager;

    @Spy
    @InjectMocks
    private AnalyticsService analyticsService;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        Mockit.setUpMocks(ApplicationContextManagerMock.class);

        when(analyzerInfo.getAnalyzer()).thenReturn(WekaClassifier.class.getName());
        when(analyzerInfo.getAlgorithm()).thenReturn(SMO.class.getName());
        when(analyzerInfo.getFilter()).thenReturn(StringToWordVector.class.getName());

        when(context.getAnalyzerInfo()).thenReturn(analyzerInfo);
        when(context.getAlgorithm()).thenReturn(smo);
        when(context.getAnalyzer()).thenReturn(analyzer);
        when(context.getFilter()).thenReturn(filter);

        Map<String, Context> contexts = new HashMap<>();
        contexts.put(context.getName(), context);
        Deencapsulation.setField(analyticsService, "contexts", contexts);

        when(analyticsService.getAnalyzer(any(String.class))).thenReturn(analyzer);
        when(analyticsService.getContexts()).thenReturn(contexts);

        ApplicationContextManagerMock.setBean(AnalyzerManager.class, analyzerManager);
        ApplicationContextManagerMock.setBean(IAnalyticsService.class, analyticsService);
        Deencapsulation.setField(analyticsService, "clusterManager", clusterManager);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(ApplicationContextManager.class);
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

        when(analyticsService.getAnalyzer(any(String.class))).thenReturn(analyzer);
        Trainer trainer = new Trainer(analysis);
        trainer.call();

        verify(analyzer, atLeastOnce()).train(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void build() throws Exception {
        analyticsService.build(analysis);
        verify(clusterManager, atLeastOnce()).sendTaskToAll(any(Callable.class));

        when(analyticsService.getContext(any(String.class))).thenReturn(context);
        Builder builder = new Builder(analysis);
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

        when(analyticsService.getAnalyzer(any(String.class))).thenReturn(this.analyzer);
        Analyzer analyzer = new Analyzer(analysis);
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
        when(this.analysis.isDistributed()).thenReturn(Boolean.TRUE);
        when(this.analysis.isClassesAndClusters()).thenReturn(Boolean.TRUE);

        this.analyticsService.classesOrClusters(analysis);
        Analysis analysis = this.analyticsService.classesOrClusters(this.analysis);
        assertEquals(analysis, this.analysis);

        when(this.analysis.isDistributed()).thenReturn(Boolean.FALSE);
        this.analyticsService.classesOrClusters(analysis);
        analysis = this.analyticsService.classesOrClusters(this.analysis);
        assertEquals(analysis, this.analysis);

        verify(analysis, atLeast(1)).setClassesOrClusters(any(Object[].class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void sizesForClassesOrClusters() throws Exception {
        Future future = mock(Future.class);
        when(clusterManager.sendTask(any(Callable.class))).thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class))).thenReturn(analysis);
        when(analyzer.analyze(any())).thenReturn(analysis);
        when(this.analysis.isDistributed()).thenReturn(Boolean.TRUE);
        when(this.analysis.isSizesForClassesAndClusters()).thenReturn(Boolean.TRUE);
        when(analysis.getClassesOrClusters()).thenReturn(new Object[]{IConstants.POSITIVE, IConstants.NEGATIVE});

        this.analyticsService.sizesForClassesOrClusters(analysis);
        Analysis analysis = this.analyticsService.sizesForClassesOrClusters(this.analysis);
        assertEquals(analysis, this.analysis);

        when(this.analysis.isDistributed()).thenReturn(Boolean.FALSE);
        this.analyticsService.sizesForClassesOrClusters(analysis);
        analysis = this.analyticsService.sizesForClassesOrClusters(this.analysis);
        assertEquals(analysis, this.analysis);

        verify(analysis, atLeast(1)).setSizesForClassesOrClusters(any(int[].class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void destroy() throws Exception {
        analyticsService.destroy(context);
        verify(clusterManager, atLeastOnce()).sendTaskToAll(any(Callable.class));

        Destroyer destroyer = new Destroyer(context);
        when(analyticsService.getContext(anyString())).thenReturn(context);
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