package ikube.anal;

import ikube.IConstants;
import ikube.Load;
import ikube.analytics.weka.WekaClassifier;
import ikube.model.Analysis;
import ikube.model.AnalyzerInfo;
import ikube.model.Context;
import ikube.security.WebServiceAuthentication;
import ikube.toolkit.FileUtilities;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.http.MediaType;
import weka.classifiers.functions.SimpleLinearRegression;

import java.io.File;
import java.io.InputStream;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 17-06-2014
 */
public class AnalLoad extends Load {

    protected HttpClient httpClient = new HttpClient();

    public static void main(String[] args) throws Exception {
        new AnalLoad().doMain(args);
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    protected void doMain(final String[] args) throws Exception {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(140);
        parser.parseArgument(args);

        String analyzerName = "linear-regression";
        String trainingData = FileUtilities.getContent(FileUtilities.findFileRecursively(new File("."), "regression.arff"));
        Context context = getContext(analyzerName, trainingData);
        String content = IConstants.GSON.toJson(context);

        new WebServiceAuthentication().authenticate(httpClient, "ikube.be", Integer.toString(80), "user", "user");

        // Create the regression analysis object
        String createPath = new StringBuilder("/ikube")
                .append("/service").append("/analyzer").append("/create")
                .toString();
        String createUrl = getUrl("ikube.be", 80, createPath);
        System.out.println(createUrl);
        // http://ikube.be:80/ikube/service/analyzer/create
        executePost(createUrl, content, Context.class);

        Analysis<Object, Object> analysis = getAnalysis(analyzerName, "205000,3529,9191,6,0,0");

        // Build the analyzer
        String buildPath = new StringBuilder("/ikube")
                .append("/service").append("/analyzer").append("/build")
                .toString();
        String buildUrl = getUrl("ikube.be", 80, buildPath);
        content = IConstants.GSON.toJson(analysis);
        executePost(buildUrl, content, Analysis.class);

        // Use the analysis object
        String analysisPath = new StringBuilder("/ikube")
                .append("/service").append("/analyzer").append("/analyze")
                .toString();
        String analysisUrl = getUrl("ikube.be", 80, analysisPath);
        content = IConstants.GSON.toJson(analysis);
        executePost(analysisUrl, content, Analysis.class);

        // Destroy the analysis object
        String destroyPath = new StringBuilder("/ikube")
                .append("/service").append("/analyzer").append("/destroy")
                .toString();
        String destroyUrl = getUrl("ikube.be", 80, destroyPath);
        content = IConstants.GSON.toJson(context);
        executePost(destroyUrl, content, Context.class);
    }

    private <T> T executePost(final String url, final String content, final Class<T> type) throws Exception {
        PostMethod postMethod = new PostMethod(url);
        StringRequestEntity stringRequestEntity = new StringRequestEntity(content, MediaType.APPLICATION_JSON.toString(), IConstants.ENCODING);
        postMethod.setRequestEntity(stringRequestEntity);
        return executeMethod(postMethod, type);
    }

    @SuppressWarnings("unchecked")
    private <T> T executeMethod(final HttpMethod httpMethod, final Class<T> type) throws Exception {
        httpClient.executeMethod(httpMethod);
        InputStream inputStream = httpMethod.getResponseBodyAsStream();
        String response = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toString();
        int statusCode = httpMethod.getStatusCode();
        logger.info("Response : " + statusCode);
        T result = IConstants.GSON.fromJson(response, type);
        logger.info("         : " + response);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Context getContext(final String name, final String trainingData) {
        Context context = new Context();
        context.setName(name);

        AnalyzerInfo analyzerInfo = new AnalyzerInfo();
        analyzerInfo.setAnalyzer(WekaClassifier.class.getName());
        analyzerInfo.setAlgorithm(SimpleLinearRegression.class.getName());

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

        analysis.setDistribution(Boolean.TRUE);
        analysis.setClassesAndClusters(Boolean.FALSE);
        analysis.setAlgorithm(Boolean.TRUE);
        analysis.setCorrelation(Boolean.TRUE);
        return analysis;
    }

}
