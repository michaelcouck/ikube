package ikube.analytics.action;

import ikube.AbstractTest;
import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class SizesForClassesOrClustersTest extends AbstractTest {

    @Mock
    private Analysis analysis;
    @Mock
    private Context context;
    @Mock
    private IAnalyzer analyzer;
    @Mock
    private IAnalyticsService analyticsService;
    @Spy
    @InjectMocks
    private SizesForClassesOrClusters sizesForClassesOrClusters;

    @Before
    public void before() {
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return analyticsService;
            }
        }).when(sizesForClassesOrClusters).getAnalyticsService();
        when(analyticsService.getContext(Mockito.anyString())).thenReturn(context);
        when(context.getAnalyzer()).thenReturn(analyzer);
    }

    @Test
    public void call() throws Exception {
        Analysis analysis = sizesForClassesOrClusters.call();
        assertEquals(this.analysis, analysis);
        //noinspection unchecked
        verify(analyzer, atLeastOnce()).analyze(any(Context.class), any(Analysis.class));
    }

}