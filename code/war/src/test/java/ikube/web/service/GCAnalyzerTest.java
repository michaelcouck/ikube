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

import java.util.Date;

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

    private Date date = new Date(1415956490991l);
    private Object[][][] matrices = new Object[][][]{
            {{date, 1, 2, 3}, {date, 4, 5, 6}, {date, 7, 8, 9}},
            {{date, 10, 11, 12}, {date, 13, 14, 15}, {date, 16, 17, 18}},
            {{date, 19, 20, 21}, {date, 22, 23, 24}, {date, 25, 26, 27}}
    };

    @Test
    @SuppressWarnings("unchecked")
    public void usedToMaxRatioPrediction() {
        int port = 8600;
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return matrices;
            }
        }).when(gcAnalyzerService).getGcData(LOCALHOST, port);

        gcAnalyzer.usedToMaxRatioPrediction(LOCALHOST, port, 60);

        verify(analyticsService, times(3)).create(any(Context.class));
        verify(analyticsService, times(3)).analyze(any(Analysis.class));
        verify(analyticsService, times(3)).destroy(any(Context.class));
    }

    @Test
    public void matrixToString() {
        String first = gcAnalyzer.matrixToString(matrices[0]);
        String second = gcAnalyzer.matrixToString(matrices[1]);
        String third = gcAnalyzer.matrixToString(matrices[2]);

        assertEquals("2014-11-14-10-14-50,1,2,3\n\r2014-11-14-10-14-50,4,5,6\n\r2014-11-14-10-14-50,7,8,9", first);
        assertEquals("2014-11-14-10-14-50,10,11,12\n\r2014-11-14-10-14-50,13,14,15\n\r2014-11-14-10-14-50,16,17,18", second);
        assertEquals("2014-11-14-10-14-50,19,20,21\n\r2014-11-14-10-14-50,22,23,24\n\r2014-11-14-10-14-50,25,26,27", third);
    }

}