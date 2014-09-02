package ikube.analytics.neuroph;

import ikube.AbstractTest;
import ikube.model.Analysis;
import ikube.model.Context;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import java.util.Arrays;

public class NeurophAnalyzerTest extends AbstractTest {

    @Spy
    private Context context;
    @Spy
    private Analysis analysis;
    @Spy
    @InjectMocks
    private NeurophAnalyzer neurophAnalyzer;

    @Test
    public void analyze() throws Exception {
        neurophAnalyzer.init(context);
        neurophAnalyzer.train(context, analysis);
        neurophAnalyzer.build(context);
        neurophAnalyzer.analyze(context, analysis);
        double[] output = (double[]) analysis.getOutput();
        for (final double feature : output) {
            logger.error(Double.valueOf(feature).toString());
        }
    }

}
