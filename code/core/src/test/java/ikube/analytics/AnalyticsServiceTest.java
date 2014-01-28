package ikube.analytics;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.analytics.weka.WekaClassifier;
import ikube.model.Analysis;
import ikube.model.Context;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.functions.SMO;
import weka.filters.unsupervised.attribute.StringToWordVector;

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
        public static IAnalyzer<?, ?> buildAnalyzer(final Context context) throws Exception {
            return null;
        }
    }

    private Analysis analysis;
    private IAnalyzer analyzer;
    private AnalyticsService analyticsService;

    @Before
    public void before() throws Exception {
        analyticsService = new AnalyticsService();
        analysis = mock(Analysis.class);
        analyzer = mock(IAnalyzer.class);
        when(analysis.getAnalyzer()).thenReturn(IConstants.ANALYZER);

        Map<String, IAnalyzer> analyzers = new HashMap<>();
        analyzers.put(analysis.getAnalyzer(), analyzer);
        Deencapsulation.setField(analyticsService, analyzers);
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
    public void analyze() {
        Analysis analysis = analyticsService.analyze(this.analysis);
        assertEquals(this.analysis, analysis);
        verify(analysis, atLeastOnce()).setOutput(any());
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
        IAnalyzer analyzer = analyticsService.getAnalyzer(analysis);
        assertEquals(this.analyzer, analyzer);
    }

    @Test
    public void getAnalyzers() throws Exception {
        Map analyzers = analyticsService.getAnalyzers();
        assertNotNull(analyzers);
    }

}