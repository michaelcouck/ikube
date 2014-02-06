package ikube.web.service;

import com.google.gson.Gson;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.analytics.weka.WekaClusterer;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.FileUtilities;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.Before;
import org.junit.Test;
import weka.clusterers.SimpleKMeans;

import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;

import static junit.framework.Assert.*;

public class AnalyzerIntegration extends BaseTest {

    private Gson gson;

    @Before
    public void before() {
        gson = new Gson();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void create() throws Exception {
        Context context = getContext();
        String content = gson.toJson(context);
        String url = getUrl(Analyzer.CREATE);
        execute(url, content, Context.class);
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
            execute(url, content, Analysis.class);
        }
    }

    @Test
    public void build() throws Exception {
        train();
        Context context = getContext();
        String content = gson.toJson(context);
        String url = getUrl(Analyzer.BUILD);
        execute(url, content, Analysis.class);
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
            Analysis result = execute(url, content, Analysis.class);
            assertTrue(Integer.parseInt(result.getClazz()) >= 0 && Integer.parseInt(result.getClazz()) <= 6);
        }
    }

    @Test
    public void destroy() {
    }

    @Test
    public void analyzers() {
    }

    @Test
    public void context() {
    }

    @Test
    public void contexts() {
    }

    @Test
    public void newLineToLineBreak() {
    }

    private <T> T execute(final String url, final String content, final Class<T> type) throws Exception {
        PostMethod postMethod = new PostMethod(url);
        StringRequestEntity stringRequestEntity = new StringRequestEntity(content, MediaType.APPLICATION_JSON, IConstants.ENCODING);
        postMethod.setRequestEntity(stringRequestEntity);

        HTTP_CLIENT.executeMethod(postMethod);
        assertEquals(200, postMethod.getStatusCode());
        String response = FileUtilities.getContents(postMethod.getResponseBodyAsStream(), Integer.MAX_VALUE).toString();
        T result = gson.fromJson(response, type);
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
    }

    @SuppressWarnings("unchecked")
    private Context getContext() {
        File trainingDataFile = FileUtilities.findFileRecursively(new File("."), "bmw-browsers.arff");
        String trainingData = FileUtilities.getContent(trainingDataFile);

        Context context = new Context();
        context.setName("bmw-browsers");
        context.setAnalyzer(WekaClusterer.class.getName());
        context.setAlgorithm(SimpleKMeans.class.getName());
        context.setOptions(new String[]{"-N", "6"});
        context.setMaxTraining(Integer.MAX_VALUE);
        context.setTrainingData(trainingData);

        return context;
    }

    private Analysis getAnalysis(final String analyzer, final String input) {
        Analysis<String, double[]> analysis = new Analysis<>();
        analysis.setAnalyzer(analyzer);
        analysis.setInput(input);
        return analysis;
    }

}