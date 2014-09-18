package ikube.analytics.action;

import ikube.AbstractTest;
import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20-07-2014
 */
public class AnalyzerTest extends AbstractTest {

    @Spy
    @InjectMocks
    private Analyze analyzer;
    @Mock
    private Analysis analysis;
    @Mock
    private Context context;
    @Mock
    private IAnalyzer ianalyzer;
    @Mock
    private IAnalyticsService analyticsService;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return analyticsService;
            }
        }).when(analyzer).getAnalyticsService();

        when(analyticsService.getContext(anyString())).thenReturn(context);
        when(context.isBuilt()).thenReturn(Boolean.TRUE);
        when(context.getAnalyzer()).thenReturn(ianalyzer);
        when(ianalyzer.analyze(any(Context.class), any(Analysis.class))).thenReturn(this.analysis);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void call() throws Exception {
        Analysis analysis = analyzer.call();
        Assert.assertEquals(this.analysis, analysis);
        verify(ianalyzer, atLeastOnce()).analyze(any(Context.class), any(Analysis.class));
    }

}
