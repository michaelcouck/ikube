package ikube.web.service;

import com.google.gson.Gson;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.analytics.weka.WekaClusterer;
import ikube.model.Analysis;
import ikube.model.AnalyzerInfo;
import ikube.model.Context;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import weka.clusterers.SimpleKMeans;

import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * TODO: Note to self. For some reason this test does not work on the Dell server!!!
 *
 * @author Michael Couck
 * @version 01.00
 * @since 05-02-2014
 */
@Ignore
public class AnalyzerIntegration extends BaseTest {

    private Gson gson;

    @Before
    public void before() {
        gson = new Gson();
        // We will stop this thread a full minute to wait for the server to start completely
        ThreadUtilities.sleep(60000);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void create() throws Exception {
        Context context = getContext("bmw-browsers.arff", "bmw-browsers");
        String content = gson.toJson(context);
        String url = getUrl(Analyzer.CREATE);
        executePost(url, content, Context.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        create();

        String line;

        File trainingDataFile = FileUtilities.findFileRecursively(new File("."), "bmw-browsers.csv");
        FileReader fileReader = new FileReader(trainingDataFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        while ((line = bufferedReader.readLine()) != null) {
            Analysis<String, double[]> analysis = getAnalysis("bmw-browsers", line);
            String content = gson.toJson(analysis);
            String url = getUrl(Analyzer.TRAIN);
            executePost(url, content, Analysis.class);
        }
    }

    @Test
    public void build() throws Exception {
        train();

        Analysis analysis = getAnalysis("bmw-browsers", null);
        String content = gson.toJson(analysis);
        String url = getUrl(Analyzer.BUILD);
        executePost(url, content, Analysis.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws Exception {
        build();

        String line;

        File trainingDataFile = FileUtilities.findFileRecursively(new File("."), "bmw-browsers.csv");
        FileReader fileReader = new FileReader(trainingDataFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        while ((line = bufferedReader.readLine()) != null) {
            Analysis<String, double[]> analysis = getAnalysis("bmw-browsers", line);
            String content = gson.toJson(analysis);
            String url = getUrl(Analyzer.ANALYZE);
            Analysis result = executePost(url, content, Analysis.class);
            assertTrue(Integer.parseInt(result.getClazz()) >= 0 && Integer.parseInt(result.getClazz()) <= 6);
        }
    }

    @Test
    public void destroy() throws Exception {
        analyze();

        String analyzersUrl = getUrl(Analyzer.ANALYZERS);
        String[] names = executeGet(analyzersUrl, String[].class);
        List<String> list = new ArrayList<>(Arrays.asList(names));
        assertTrue(list.contains("bmw-browsers"));

        Context context = getContext("bmw-browsers.arff", "bmw-browsers");
        String content = gson.toJson(context);
        String destroyUrl = getUrl(Analyzer.DESTROY);
        executePost(destroyUrl, content, Context.class);

        names = executeGet(analyzersUrl, String[].class);
        list = new ArrayList<>(Arrays.asList(names));
        assertFalse(list.contains("bmw-browsers"));
    }

    @Test
    public void context() throws Exception {
        analyze();

        Analysis analysis = getAnalysis("bmw-browsers", null);
        String content = gson.toJson(analysis);
        String url = getUrl(Analyzer.CONTEXT);
        Context context = executePost(url, content, Context.class);
        assertEquals(analysis.getAnalyzer(), context.getName());
    }

    @Test
    public void contexts() throws Exception {
        analyze();

        String contextsUrl = getUrl(Analyzer.CONTEXTS);
        String[] names = executeGet(contextsUrl, String[].class);
        List<String> list = new ArrayList<>(Arrays.asList(names));
        assertTrue(list.contains("bmw-browsers"));
    }

    @Test
    public void analyzers() throws Exception {
        String analyzersUrl = getUrl(Analyzer.ANALYZERS);
        String[] names = executeGet(analyzersUrl, String[].class);
        List<String> list = new ArrayList<>(Arrays.asList(names));
        logger.info("List : " + list);
        assertTrue(list.toString().equals("[bank-data, bmw-browsers, sentiment-smo]"));
    }

    private <T> T executeGet(final String url, final Class<T> type) throws Exception {
        GetMethod postMethod = new GetMethod(url);
        // postMethod.setQueryString(null);
        return executeMethod(postMethod, type);
    }

    private <T> T executePost(final String url, final String content, final Class<T> type) throws Exception {
        PostMethod postMethod = new PostMethod(url);
        StringRequestEntity stringRequestEntity = new StringRequestEntity(content, MediaType.APPLICATION_JSON, IConstants.ENCODING);
        postMethod.setRequestEntity(stringRequestEntity);
        return executeMethod(postMethod, type);
    }

    private <T> T executeMethod(final HttpMethod httpMethod, final Class<T> type) throws Exception {
        HTTP_CLIENT.executeMethod(httpMethod);
        InputStream inputStream = httpMethod.getResponseBodyAsStream();
        String response = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toString();
        int statusCode = httpMethod.getStatusCode();
        logger.info("Response : " + statusCode);
        assertEquals(200, statusCode);
        T result = gson.fromJson(response, type);
        logger.info("         : " + result);
        assertNotNull(result);
        return result;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    protected String getUrl(final String service) throws MalformedURLException {
        StringBuilder builder = new StringBuilder();
        builder.append(IConstants.SEP);
        builder.append(IConstants.IKUBE);
        builder.append(BaseTest.SERVICE);
        builder.append(Analyzer.ANALYZER);
        builder.append(service);
        return new URL("http", LOCALHOST, SERVER_PORT, builder.toString()).toString();
        // return new URL("http", "ikube.be", 80, builder.toString()).toString();
    }

    @SuppressWarnings("unchecked")
    private Context getContext(final String fileName, final String name) {
        File trainingDataFile = FileUtilities.findFileRecursively(new File("."), fileName);
        String trainingData = FileUtilities.getContent(trainingDataFile);

        Context context = new Context();
        context.setName(name);

        AnalyzerInfo analyzerInfo = new AnalyzerInfo();
        analyzerInfo.setAnalyzer(WekaClusterer.class.getName());
        analyzerInfo.setAlgorithm(SimpleKMeans.class.getName());

        context.setAnalyzerInfo(analyzerInfo);

        context.setOptions(new String[]{"-N", "6"});
        context.setMaxTraining(Integer.MAX_VALUE);
        context.setTrainingData(trainingData);

        return context;
    }

    private Analysis getAnalysis(final String analyzer, final String input) {
        Analysis<String, double[]> analysis = new Analysis<>();
        analysis.setAnalyzer(analyzer);
        analysis.setInput(input);
        analysis.setDistribution(Boolean.TRUE);
        analysis.setClassesAndClusters(Boolean.TRUE);
        analysis.setAlgorithm(Boolean.TRUE);
        analysis.setCorrelation(Boolean.TRUE);
        return analysis;
    }

}