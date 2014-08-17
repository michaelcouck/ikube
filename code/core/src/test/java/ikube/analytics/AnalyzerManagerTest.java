package ikube.analytics;

import ikube.AbstractTest;
import ikube.model.Context;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20-11-2013
 */
public class AnalyzerManagerTest extends AbstractTest {

    @Spy
    @InjectMocks
    private AnalyzerManager analyzerManager;
    @Mock
    private Context context;
    @Mock
    private IAnalyzer analyzer;

    @Test
    public void buildAnalyzers() throws Exception {
        when(context.getAnalyzer()).thenReturn(analyzer);
        Map<String, Context> contexts = new HashMap<>();
        contexts.put("context", context);
        analyzerManager.buildAnalyzers(contexts);
        IAnalyzer analyzer = (IAnalyzer) contexts.get("context").getAnalyzer();
        Assert.assertEquals(this.analyzer, analyzer);
        verify(context, times(1)).setBuilt(Boolean.TRUE);
    }

}
