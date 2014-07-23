package ikube.analytics;

import ikube.AbstractTest;
import ikube.analytics.action.*;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ApplicationContextManager;
import mockit.Deencapsulation;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import weka.classifiers.functions.SMO;
import weka.filters.Filter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20-11-2013
 */
public class AnalyticsServiceTest extends AbstractTest {

    @MockClass(realClass = Analyzer.class)
    public static class AnalyzerMock {
        @mockit.Mock
        public Analysis call() throws Exception {
            return mock(Analysis.class);
        }
    }

    @MockClass(realClass = SizesForClassesOrClusters.class)
    public static class SizesForClassesOrClustersMock {
        @mockit.Mock
        public Analysis call() throws Exception {
            return mock(Analysis.class);
        }
    }

    @Mock
    private SMO smo;
    @Mock
    private Filter filter;
    @Mock
    private Future future;

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
        Mockit.setUpMocks(ApplicationContextManagerMock.class, AnalyzerMock.class, SizesForClassesOrClustersMock.class);

        when(context.getAnalyzer()).thenReturn(analyzer);
        when(context.getAlgorithms()).thenReturn(new Object[]{smo});
        when(context.getFilters()).thenReturn(new Object[]{filter});
        when(context.getFileNames()).thenReturn(new String[]{"sentiment-smo.arff"});

        Map<String, Context> contexts = new HashMap<>();
        contexts.put(context.getName(), context);
        when(analyticsService.getContexts()).thenReturn(contexts);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(ApplicationContextManager.class, AnalyzerMock.class, SizesForClassesOrClustersMock.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void create() throws Exception {
        context = analyticsService.create(context);
        assertNotNull(context);
        verify(clusterManager, atLeastOnce()).sendTaskToAll(any(Creator.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        context = analyticsService.train(analysis);
        assertNotNull(context);
        verify(clusterManager, atLeastOnce()).sendTaskToAll(any(Trainer.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void build() throws Exception {
        context = analyticsService.build(analysis);
        assertNotNull(context);
        verify(clusterManager, atLeastOnce()).sendTaskToAll(any(Builder.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws Exception {
        when(clusterManager.sendTask(any(Analyzer.class))).thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class))).thenReturn(analysis);
        // when(analyzer.analyze(any(Context.class), any())).thenReturn(analysis);

        Analysis analysis = analyticsService.analyze(this.analysis);
        assertNotNull(analysis);

        when(this.analysis.isDistributed()).thenReturn(Boolean.TRUE);
        analyticsService.analyze(this.analysis);
        verify(clusterManager, atLeastOnce()).sendTask(any(Analyzer.class));
        verify(future, atLeastOnce()).get(anyLong(), any(TimeUnit.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void sizesForClassesOrClusters() throws Exception {
        when(clusterManager.sendTask(any(Analyzer.class))).thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class))).thenReturn(analysis);

        Analysis analysis = analyticsService.sizesForClassesOrClusters(this.analysis);
        assertNotNull(analysis);

        when(this.analysis.isDistributed()).thenReturn(Boolean.TRUE);
        analyticsService.sizesForClassesOrClusters(this.analysis);
        verify(clusterManager, atLeastOnce()).sendTask(any(SizesForClassesOrClusters.class));
        verify(future, atLeastOnce()).get(anyLong(), any(TimeUnit.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void destroy() throws Exception {
        analyticsService.destroy(context);
        verify(clusterManager, atLeastOnce()).sendTaskToAll(any(Destroyer.class));
    }

}