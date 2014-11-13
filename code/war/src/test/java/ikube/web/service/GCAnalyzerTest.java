package ikube.web.service;

import ikube.AbstractTest;
import ikube.analytics.IAnalyticsService;
import ikube.model.Analysis;
import ikube.model.Context;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Michael couck
 * @version 01.00
 * @since 02-07-2013
 */
public class GCAnalyzerTest extends AbstractTest {

    @Spy
    @InjectMocks
    private GCAnalyzer gcAnalyzer;
    @Mock
    private IAnalyticsService analyticsService;
    @Mock
    private ikube.application.GCAnalyzer gcAnalyzerService;

    private Object[][][] matrices = new Object[][][]{
            {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}},
            {{10, 11, 12}, {13, 14, 15}, {16, 17, 18}},
            {{19, 20, 21}, {22, 23, 24}, {25, 26, 27}}
    };

    @Test
    @SuppressWarnings("unchecked")
    public void usedToMaxRatioPrediction() {
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return matrices;
            }
        }).when(gcAnalyzerService).getGcData(LOCALHOST);

        gcAnalyzer.usedToMaxRatioPrediction(LOCALHOST, 60);

        verify(analyticsService, times(3)).create(any(Context.class));
        verify(analyticsService, times(3)).analyze(any(Analysis.class));
        verify(analyticsService, times(3)).destroy(any(Context.class));
    }

    @Test
    public void matrixToString() {
        String first = gcAnalyzer.matrixToString(matrices[0]);
        String second = gcAnalyzer.matrixToString(matrices[1]);
        String third = gcAnalyzer.matrixToString(matrices[2]);

        assertEquals("1,2,3\n\r4,5,6\n\r7,8,9", first);
        assertEquals("10,11,12\n\r13,14,15\n\r16,17,18", second);
        assertEquals("19,20,21\n\r22,23,24\n\r25,26,27", third);
    }

}