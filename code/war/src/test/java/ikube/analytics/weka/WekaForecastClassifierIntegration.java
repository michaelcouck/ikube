package ikube.analytics.weka;

import ikube.IConstants;
import ikube.analytics.AnalyzerIntegration;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.web.service.Analyzer;
import org.junit.Test;
import weka.classifiers.functions.SMO;

import static ikube.toolkit.HttpClientUtilities.doPost;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 27-09-2014
 */
public class WekaForecastClassifierIntegration extends AnalyzerIntegration {

    @Test
    public void create() throws Exception {
        Context context = getContext();
        String createUri = getAnalyzerRestUri(Analyzer.CREATE);
        logger.warn(createUri + ":" + IConstants.GSON.toJson(context));
        Context result = doPost(createUri, context, Context.class);
        assertNotNull(result);
    }

    @Test
    @Override
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        create();

        Analysis analysis = new Analysis();
        analysis.setContext(getContext().getName());
        analysis.setInput("2014-09-29,587.55,587.98,574.18,575.06,1920700,575.06");

        String trainUri = getAnalyzerRestUri(Analyzer.TRAIN);
        logger.warn(trainUri + ":" + IConstants.GSON.toJson(analysis));
        Context context = doPost(trainUri, analysis, Context.class);
        assertFalse(context.isBuilt());
    }

    @Test
    public void build() throws Exception {
        train();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws Exception {
        build();

        Analysis analysis = new Analysis();
        analysis.setContext(getContext().getName());
        analysis.setInput(getOptions());

        String analyzeUri = getAnalyzerRestUri(Analyzer.ANALYZE);
        logger.warn(analyzeUri + ":" + IConstants.GSON.toJson(analysis));
        analysis = doPost(analyzeUri, analysis, Analysis.class);
        assertNotNull(analysis.getOutput());
    }

    @Override
    protected Context getContext() {
        Context context = new Context();
        context.setName("forecast-classifier");
        context.setAnalyzer(WekaForecastClassifier.class.getName());
        context.setAlgorithms(SMO.class.getName());
        context.setTrainingDatas(
                "2014-09-26,587.55,587.98,574.18,575.06,1920700,575.06\n\r" +
                        "2014-09-27,587.55,587.98,574.18,575.06,1920700,575.06\n\r" +
                        "2014-09-28,587.55,587.98,574.18,575.06,1920700,575.06\n\r");
        return context;
    }

    private Object[] getOptions() {
        return new String[]{
                "-fieldsToForecast", "6",
                "-timeStampField", "0",
                "-minLag", "1",
                "-maxLag", "1",
                "-forecasts", "5"
        };
    }

}