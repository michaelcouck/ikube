package ikube.analytics;

import ikube.AbstractTest;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20.11.13
 */
public class AnalyzerManagerTest extends AbstractTest {

    @Test
    public void buildAnalyzers() throws Exception {
        IAnalyzer analyzer = mock(IAnalyzer.class);
        IAnalyzer.IContext context = mock(IAnalyzer.IContext.class);
        when(context.getAnalyzer()).thenReturn(analyzer);
        IAnalyzer[] analyzers = AnalyzerManager.buildAnalyzers(new IAnalyzer.IContext[] { context, context, context });
        verify(analyzer, atLeastOnce()).build(context);
        assertEquals("There should be all the analyzers built : ", 3, analyzers.length);
    }

}
