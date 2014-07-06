package ikube.web.service;

import ikube.AbstractTest;
import ikube.analytics.IAnalyticsService;
import ikube.model.Analysis;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Michael couck
 * @version 01.00
 * @since 02-07-2013
 */
public class AnalyzerTest extends AbstractTest {

    /**
     * Class under test
     */
    @Spy
    @InjectMocks
    private Analyzer analyzer;
    @Mock
    private Analysis<?, ?> analysis;
    @Mock
    private IAnalyticsService analyticsService;
    @Mock
    private HttpServletRequest httpServletRequest;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        when(analysis.getAlgorithmOutput()).thenReturn("output");
        when(analysis.isClassesAndClusters()).thenReturn(Boolean.TRUE);
        when(analyzer.buildJsonResponse(any())).thenReturn(Response.status(Response.Status.OK).build());
        when(analyticsService.analyze(any(Analysis.class))).thenReturn(analysis);
    }

    @Test
    public void analyze() throws IOException {
        when(analyzer.analyze(httpServletRequest)).thenCallRealMethod();
        analyzer.analyze(httpServletRequest);
        verify(analyzer, atLeastOnce()).buildJsonResponse(any());
    }
}