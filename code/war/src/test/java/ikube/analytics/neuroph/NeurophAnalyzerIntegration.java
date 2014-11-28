package ikube.analytics.neuroph;

import ikube.IConstants;
import ikube.analytics.AnalyzerIntegration;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.web.service.Analyzer;
import org.junit.Test;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;

import static ikube.toolkit.REST.doPost;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 09-09-2014
 */
public class NeurophAnalyzerIntegration extends AnalyzerIntegration {

    @Test
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        create();

        Analysis analysis = new Analysis();
        analysis.setContext(getContext().getName());
        analysis.setInput("1,0,1");
        analysis.setOutput("1,0");

        String trainUri = getAnalyzerRestUri(Analyzer.TRAIN);
        logger.warn(trainUri + ":" + IConstants.GSON.toJson(analysis));
        Context context = doPost(trainUri, analysis, Context.class);
        assertFalse(context.isBuilt());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws Exception {
        build();

        Analysis analysis = new Analysis();
        analysis.setContext(getContext().getName());
        analysis.setInput("0,1,0");

        String analyzeUri = getAnalyzerRestUri(Analyzer.ANALYZE);
        logger.warn(analyzeUri + ":" + IConstants.GSON.toJson(analysis));
        analysis = doPost(analyzeUri, analysis, Analysis.class);
        assertNotNull(analysis.getOutput());
    }

    protected Context getContext() {
        Context context = new Context();
        context.setName("multi-layer-perceptron");
        context.setAnalyzer(NeurophAnalyzer.class.getName());

        context.setOptions(getOptions());
        context.setAlgorithms(MultiLayerPerceptron.class.getName());
        return context;
    }

    private Object[] getOptions() {
        return new Object[]{
                "-label", "label",
                "-outputLabels", "[one, two, three]",
                "-inputNeuronsCount", "3",
                "-hiddenNeuronsCount", "3",
                "-outputNeuronsCount", "2",
                "-neuronsInLayers", "[3, 3, 2]",
                TransferFunctionType.TANH};
    }

}