package ikube.analytics.weka;

import ikube.IConstants;
import ikube.analytics.AnalyzerIntegration;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.web.service.Analyzer;
import org.junit.Test;

import static ikube.toolkit.HttpClientUtilities.doPost;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 27-09-2014
 */
public class WekaForecastClassifierIntegration extends AnalyzerIntegration {

    @Override
    public void train() throws Exception {
        create();

        Analysis analysis = new Analysis();
        analysis.setContext(getContext().getName());
        //noinspection unchecked
        analysis.setInput("2014-09-26,587.55,587.98,574.18,575.06,1920700,575.06");
        //noinspection unchecked
        analysis.setOutput(null);

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
        analysis.setInput(getOptions());

        String analyzeUri = getAnalyzerRestUri(Analyzer.ANALYZE);
        logger.warn(analyzeUri + ":" + IConstants.GSON.toJson(analysis));
        analysis = doPost(analyzeUri, analysis, Analysis.class);
        assertNotNull(analysis.getOutput());
    }

    protected Context getContext() {
        Context context = new Context();
        context.setName("forecast-classifier");
        context.setAnalyzer(WekaForecastClassifier.class.getName());
        context.setOptions(getOptions());
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