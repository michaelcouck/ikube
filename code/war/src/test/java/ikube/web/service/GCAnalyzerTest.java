package ikube.web.service;

import ikube.AbstractTest;
import ikube.analytics.IAnalyticsService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.core.Response;

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

    @Test
    public void usedToMaxRatioPrediction() {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return new Object[][][]{{{}}};
            }
        }).when(gcAnalyzerService).getGcData(LOCALHOST);
        @SuppressWarnings("UnusedDeclaration")
        Response response = gcAnalyzer.usedToMaxRatioPrediction(LOCALHOST, 60);
        // TODO: Complete this test
    }

}