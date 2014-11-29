package ikube.analytics;

import ikube.AbstractTest;
import ikube.model.Context;
import org.junit.Assert;
import org.junit.Before;
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

    @Mock
    private Context context;
    @Mock
    private IAnalyzer analyzer;
    private Map<String, Context> contexts;
    @Spy
    @InjectMocks
    private AnalyzerManager analyzerManager;

    @Before
    public void before() {
        contexts = new HashMap<>();
        when(context.getAnalyzer()).thenReturn(analyzer);
        contexts.put("context", context);
    }

    @Test
    public void buildAnalyzer() throws Exception {
        analyzerManager.buildAnalyzer(context, Boolean.TRUE);
        IAnalyzer analyzer = (IAnalyzer) contexts.get("context").getAnalyzer();
        Assert.assertEquals(this.analyzer, analyzer);
        verify(context, times(1)).setBuilt(Boolean.TRUE);
    }

    @Test
    public void buildAnalyzers() throws Exception {
        analyzerManager.buildAnalyzers(contexts);
        verify(analyzerManager, times(1)).buildAnalyzer(any(Context.class));
    }

}
