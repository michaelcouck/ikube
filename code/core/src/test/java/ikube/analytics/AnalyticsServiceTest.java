package ikube.analytics;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Analysis;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;

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
    }

    @Test
    public void getAnalyzers() throws Exception {
        Map analyzers = analyticsService.getAnalyzers();
        assertNotNull(analyzers);
    }

    @Test
    public void analyze() {
        Analysis analysis = analyticsService.analyze(this.analysis);
        assertEquals(this.analysis, analysis);
        verify(analysis, atLeastOnce()).setOutput(any());
    }

    @Test
    public void getAnalyzer() throws Exception {
        IAnalyzer analyzer = analyticsService.getAnalyzer(analysis);
        assertEquals(this.analyzer, analyzer);
    }

}