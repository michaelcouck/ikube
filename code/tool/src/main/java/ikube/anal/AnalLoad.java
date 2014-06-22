package ikube.anal;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import ikube.Load;
import ikube.analytics.weka.WekaClassifier;
import ikube.model.Analysis;
import ikube.model.AnalyzerInfo;
import ikube.model.Context;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kohsuke.args4j.CmdLineParser;
import weka.classifiers.functions.IsotonicRegression;

import java.io.File;

import static ikube.Constants.GSON;
import static ikube.toolkit.FileUtilities.findFileRecursively;
import static ikube.toolkit.FileUtilities.getContent;
import static ikube.toolkit.HttpClientUtilities.doPost;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 17-06-2014
 */
public class AnalLoad extends Load {

    public static void main(String[] args) throws Exception {
        new AnalLoad().doMain(args);
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    protected void doMain(final String[] args) throws Exception {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(140);
        parser.parseArgument(args);

        String analyzerName = "regression";
        File file = findFileRecursively(new File("."), "regression-mineco");
        String trainingData = getContent(file);

        /*doMain(analyzerName, SimpleLinearRegression.class.getName(), trainingData);
        doMain(analyzerName, GaussianProcesses.class.getName(), trainingData);*/
        doMain(analyzerName, IsotonicRegression.class.getName(), trainingData);
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private void doMain(final String analyzerName, final String algorithm, final String trainingData) {
        String url = "ikube.be";
        int port = 80;
        String path = new StringBuilder("/ikube").append("/service").append("/analyzer").toString();

        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("user", "user"));

        // Create the regression analysis object
        Context context = getContext(analyzerName, algorithm, trainingData);
        String content = GSON.toJson(context);
        String createUrl = getUrl(url, port, path + "/create");
        Object create = doPost(createUrl, content, Context.class);
        logger.info("Create : " + create);

        // Build the analyzer
        String buildUrl = getUrl(url, port, path + "/build");
        Analysis<Object, Object> analysis = getAnalysis(analyzerName, "1652452,2008,44021,8");
        content = GSON.toJson(analysis);
        Object build = doPost(buildUrl, content, Analysis.class);
        logger.info("Build : " + build);

        // Use the analysis object
        String analysisUrl = getUrl(url, port, path + "/analyze");
        content = GSON.toJson(analysis);
        Object analyze = doPost(analysisUrl, content, Analysis.class);
        logger.info("Analyze : " + ToStringBuilder.reflectionToString(analyze));

        // Destroy the analysis object
        String destroyUrl = getUrl(url, port, path + "/destroy");
        content = GSON.toJson(context);
        Object destroy = doPost(destroyUrl, content, Context.class);
        logger.info("Destroy : " + destroy);
    }

    @SuppressWarnings("unchecked")
    private Context getContext(final String name, final String algorithm, final String trainingData) {
        Context context = new Context();
        context.setName(name);

        AnalyzerInfo analyzerInfo = new AnalyzerInfo();
        analyzerInfo.setAnalyzer(WekaClassifier.class.getName());
        analyzerInfo.setAlgorithm(algorithm);

        context.setAnalyzerInfo(analyzerInfo);

        // context.setOptions(new String[] { "-N", "6" });
        context.setMaxTraining(Integer.MAX_VALUE);
        context.setTrainingData(trainingData);

        return context;
    }

    private Analysis<Object, Object> getAnalysis(final String analyzer, final Object input) {
        Analysis<Object, Object> analysis = new Analysis<>();
        analysis.setAnalyzer(analyzer);
        analysis.setInput(input);

        analysis.setAlgorithm(Boolean.TRUE);
        analysis.setCorrelation(Boolean.TRUE);
        analysis.setDistribution(Boolean.TRUE);
        analysis.setClassesAndClusters(Boolean.TRUE);
        return analysis;
    }

}
