package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.analytics.AnalyzerManager;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import org.junit.Assert;
import org.junit.Test;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-11-2014
 */
public class WekaClassifierIntegration extends AbstractTest {

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws Exception {
        Context context = new Context();
        context.setAnalyzer(WekaClassifier.class.getName());
        context.setAlgorithms(NaiveBayesMultinomial.class.getName());
        context.setOptions("-D");
        context.setFileNames("sentiment-model-150000-300000.arff");
        context.setFilters(StringToWordVector.class.getName());
        context.setMaxTrainings(1000000);

        IAnalyzer analyzer = new AnalyzerManager().buildAnalyzer(context, Boolean.TRUE);
        Analysis analysis = new Analysis();
        analysis.setAddAlgorithmOutput(Boolean.TRUE);
        analysis.setContext(context.getName());
        analysis.setDistributed(Boolean.FALSE);

        analysis.setInput("I hate you");
        analysis = (Analysis) analyzer.analyze(context, analysis);
        Assert.assertEquals(IConstants.NEGATIVE, analysis.getClazz());

        analysis.setInput("I love you");
        analysis = (Analysis) analyzer.analyze(context, analysis);
        Assert.assertEquals(IConstants.POSITIVE, analysis.getClazz());

        analysis.setInput("What terrible weather we are having");
        analysis = (Analysis) analyzer.analyze(context, analysis);
        Assert.assertEquals(IConstants.NEGATIVE, analysis.getClazz());

        analysis.setInput("My beautiful little girl");
        analysis = (Analysis) analyzer.analyze(context, analysis);
        Assert.assertEquals(IConstants.POSITIVE, analysis.getClazz());
    }

    protected Context getContext() {
        Context context = new Context();
        context.setAnalyzer(WekaClassifier.class.getName());
        context.setAlgorithms(NaiveBayesMultinomial.class.getName());
        context.setOptions("-D");
        context.setFileNames("sentiment-model-150000-300000.arff");
        context.setFilters(StringToWordVector.class.getName());
        context.setMaxTrainings(1000000);

        return context;
    }

}