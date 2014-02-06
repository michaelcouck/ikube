package ikube.analytics;

import ikube.AbstractTest;
import ikube.model.Context;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

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
        Context context = mock(Context.class);
        when(context.getAnalyzer()).thenReturn(analyzer);
        Collection<IAnalyzer<?, ?, ?>> analyzers = AnalyzerManager.buildAnalyzers(Arrays.asList(context, context, context));
        verify(analyzer, atLeastOnce()).build(context);
        assertEquals("There should be all the analyzers built : ", 3, analyzers.size());
    }

}
