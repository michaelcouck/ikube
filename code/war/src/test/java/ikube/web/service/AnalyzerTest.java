package ikube.web.service;

import ikube.BaseTest;
import ikube.analytics.IAnalyticsService;
import ikube.model.Analysis;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael couck
 * @version 01.00
 * @since 02-07-2013
 */
public class AnalyzerTest extends BaseTest {

    /**
     * Class under test
     */
    private Analyzer analyzer;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        Analysis<?, ?> analysis = mock(Analysis.class);
        analyzer = mock(Analyzer.class);
        IAnalyticsService analyticsService = Mockito.mock(IAnalyticsService.class);

        when(analysis.getAlgorithmOutput()).thenReturn("output");
        when(analyzer.analyze(any(HttpServletRequest.class))).thenCallRealMethod();
        when(analyzer.unmarshall(any(Class.class), any(HttpServletRequest.class))).thenReturn(analysis);
        when(analyticsService.analyze(any(Analysis.class))).thenReturn(analysis);

        Deencapsulation.setField(analyzer, analyticsService);
    }

    @Test
    public void analyze() {
        analyzer.analyze(null);
        Mockito.verify(analyzer, Mockito.atLeastOnce()).buildJsonResponse(any());
    }
}