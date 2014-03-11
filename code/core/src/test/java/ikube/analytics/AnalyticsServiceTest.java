package ikube.analytics;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.analytics.weka.WekaClassifier;
import ikube.cluster.hzc.ClusterManagerHazelcast;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import weka.classifiers.functions.SMO;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20-11-2013
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

    private IAnalyzer analyzer;
    private Analysis<?, ?> analysis;
    private AnalyticsService analyticsService;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        analyticsService = new AnalyticsService();
        analyzer = mock(IAnalyzer.class);
        analysis = mock(Analysis.class);

        ClusterManagerHazelcast clusterManager = mock(ClusterManagerHazelcast.class);

        when(analysis.getAnalyzer()).thenReturn(IConstants.ANALYZER);
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                final Callable callable = (Callable) invocation.getArguments()[0];
                Future<?> future = ThreadUtilities.submit(IConstants.IKUBE, new Runnable() {
                    public void run() {
                        try {
                            callable.call();
                        } catch (Exception e) {
                            logger.error("Error : ", e);
                        }
                    }
                });
                return Arrays.asList(future);
            }
        }).when(clusterManager).sendTaskToAll(any(Callable.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                final Callable callable = (Callable) invocation.getArguments()[0];
                return ThreadUtilities.submit(IConstants.IKUBE, new Runnable() {
                    public void run() {
                        try {
                            callable.call();
                        } catch (Exception e) {
                            logger.error("Error : ", e);
                        }
                    }
                });
            }
        }).when(clusterManager).sendTask(any(Callable.class));

        Map<String, IAnalyzer> analyzers = new HashMap<>();
        analyzers.put(analysis.getAnalyzer(), analyzer);
        Deencapsulation.setField(analyticsService, "analyzers", analyzers);
        Deencapsulation.setField(analyticsService, "clusterManager", clusterManager);

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
        when(clusterManager.sendTask(any(Callable.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                Callable callable = (Callable) invocation.getArguments()[0];
                callable.call();
                return callable;
            }
        });
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