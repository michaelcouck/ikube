package ikube.analytics.neuroph;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.web.service.Analyzer;
import org.junit.After;
import org.junit.Test;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;

import java.net.MalformedURLException;
import java.net.URL;

import static ikube.toolkit.HttpClientUtilities.doPost;
import static junit.framework.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 09-09-2014
 */
public class NeurophAnalyzerIntegration extends AbstractTest {

    @After
    public void after() throws Exception {
        Context context = getContext();
        String destroyUri = getAnalyzerRestUri(Analyzer.DESTROY);
        doPost(destroyUri, context, Context.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void create() throws Exception {
        Context context = getContext();
        String createUri = getAnalyzerRestUri(Analyzer.CREATE);
        logger.warn(createUri + ":" + IConstants.GSON.toJson(context));
        Context result = doPost(createUri, context, Context.class);

        assertTrue(result.getAlgorithms().length > 0);
        assertNotNull(result.getAnalyzer());
        for (int i = 0; i < result.getAlgorithms().length; i++) {
            assertNotNull(result.getAlgorithms()[i]);
        }
    }

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
    public void build() throws Exception {
        train();

        Analysis analysis = new Analysis();
        analysis.setContext(getContext().getName());

        String buildUri = getAnalyzerRestUri(Analyzer.BUILD);
        logger.warn(buildUri + ":" + IConstants.GSON.toJson(analysis));
        Context context = doPost(buildUri, analysis, Context.class);
        assertTrue(context.isBuilt());
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

    @Test
    public void destroy() throws Exception {
        analyze();

        Analysis analysis = new Analysis();
        Context context = getContext();
        analysis.setContext(context.getName());

        String destroyUri = getAnalyzerRestUri(Analyzer.DESTROY);
        logger.warn(destroyUri + ":" + IConstants.GSON.toJson(context));
        doPost(destroyUri, context, Context.class);

        String contextUri = getAnalyzerRestUri(Analyzer.CONTEXT);
        context = doPost(contextUri, analysis, Context.class);
        assertNull(context);
    }

    private Context getContext() {
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

    @SuppressWarnings("StringBufferReplaceableByString")
    protected String getAnalyzerRestUri(final String service) throws MalformedURLException {
        StringBuilder builder = new StringBuilder();
        builder.append(IConstants.SEP);
        builder.append(IConstants.IKUBE);
        builder.append(AbstractTest.SERVICE);
        builder.append(Analyzer.ANALYZER);
        builder.append(service);
        return new URL("http", LOCALHOST, SERVER_PORT, builder.toString()).toString();
    }

}