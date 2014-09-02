package ikube.web.service;

import ikube.AbstractTest;
import ikube.analytics.IAnalyticsService;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.web.toolkit.MockFactory;
import mockit.Mockit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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

    @Spy
    @InjectMocks
    private Analyzer analyzer;
    @Mock
    private Context context;
    @Mock
    private Analysis<String, String> analysis;
    @Mock
    private IAnalyticsService analyticsService;
    @Mock
    private HttpServletRequest httpServletRequest;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        Mockit.setUpMocks(MockFactory.SerializationUtilitiesMock.class);
        when(analysis.getAlgorithmOutput()).thenReturn("output");
        when(analyzer.buildResponse(any())).thenReturn(Response.status(Response.Status.OK).build());
        when(analyticsService.analyze(any(Analysis.class))).thenReturn(analysis);
    }

    @Test
    public void create() {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return context;
            }
        }).when(analyzer).context(any(Context.class));
        analyzer.create(context);
        verify(analyzer, times(1)).create(any(Context.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void train() {
        MockFactory.setMock(Analysis.class, new Analysis<>());
        when(analysis.getInput()).thenReturn("1,2,3,4,5\r\n1,2,3,4,5\r\n1,2,3,4,5");
        analyzer.train(analysis);
        verify(analyticsService, times(3)).train(any(Analysis.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void buid() {
        analyzer.build(analysis);
        verify(analyticsService, times(1)).build(any(Analysis.class));
        verify(analyzer, atLeastOnce()).buildResponse(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws IOException {
        analyzer.analyze(analysis);
        verify(analyticsService, times(1)).analyze(any(Analysis.class));
        verify(analyzer, atLeastOnce()).buildResponse(any());
    }

    @Test
    public void destroy() {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return context;
            }
        }).when(analyzer).context(any(Context.class));
        analyzer.destroy(context);
        verify(analyticsService, times(1)).destroy(context);
    }

    @Test
    public void context() {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return AnalyzerTest.this.context;
            }
        }).when(analyzer).context(any(Context.class));
        analyzer.context(analysis);
        verify(analyticsService, times(1)).getContext(anyString());
    }

    @Test
    public void contexts() {
        analyzer.contexts();
        verify(analyticsService, times(1)).getContexts();
    }

}