package ikube.web.service;

import ikube.IntegrationTest;
import ikube.cluster.IMonitorService;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.THREAD;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;

import static ikube.toolkit.REST.doGet;
import static ikube.toolkit.REST.doPost;
import static junit.framework.Assert.*;

/**
 * Note to self: For some reason this test does not work on the Dell server!!!
 * Update: Does now it seems... :)
 * This test must still be completed and verified, perhaps with all sorts of analytics, like regression etc.
 * Update: Done.
 * <p/>
 * TODO: Need to verify that all the operations are performed in the entire cluster
 *
 * @author Michael Couck
 * @version 01.00
 * @since 05-02-2014
 */
@SuppressWarnings("FieldCanBeLocal")
public class AnalyzerIntegration extends IntegrationTest {

    private String line = "1,1,0,1,1,0,1,1";
    private String contextName = "bmw-browsers";
    private String dataFileName = "bmw-browsers.arff";

    @Autowired
    private IMonitorService monitorService;

    @Before
    public void before() {
        // Start the executor service in the cluster
        monitorService.startupAll();
        THREAD.sleep(3000);
        if (!THREAD.isInitialized()) {
            logger.warn("Executor service not started! Starting manually...");
            THREAD.initialize();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void create() throws Exception {
        Context context = getContext(dataFileName, contextName);
        String url = getAnalyzerUrl(Analyzer.CREATE);
        Context result = doPost(url, context, Context.class);
        assertNotNull(result);
        assertTrue(result.getAlgorithms().length > 0);
        assertNotNull(result.getAnalyzer());
        for (int i = 0; i < result.getAlgorithms().length; i++) {
            assertNotNull(result.getAlgorithms()[i]);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        Context context = getContext(dataFileName, contextName);
        String url = getAnalyzerUrl(Analyzer.CREATE);
        doPost(url, context, Context.class);

        Analysis<String, double[]> analysis = getAnalysis(contextName, line);
        url = getAnalyzerUrl(Analyzer.TRAIN);
        analysis = doPost(url, analysis, Analysis.class);
        assertNotNull(analysis);
    }

    @Test
    public void build() throws Exception {
        Context context = getContext(dataFileName, contextName);
        String url = getAnalyzerUrl(Analyzer.CREATE);
        doPost(url, context, Context.class);

        Analysis analysis = getAnalysis(contextName, null);
        url = getAnalyzerUrl(Analyzer.BUILD);
        context = doPost(url, analysis, Context.class);
        assertTrue(context.isBuilt());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws Exception {
        Context context = getContext(dataFileName, contextName);
        String url = getAnalyzerUrl(Analyzer.CREATE);
        doPost(url, context, Context.class);

        Analysis analysis = getAnalysis(contextName, null);
        url = getAnalyzerUrl(Analyzer.BUILD);
        doPost(url, analysis, Context.class);

        analysis = getAnalysis(contextName, line);
        url = getAnalyzerUrl(Analyzer.ANALYZE);
        analysis = doPost(url, analysis, Analysis.class);

        // logger.info("BMW dealer cluster analysis result : " + ToStringBuilder.reflectionToString(analysis));

        ArrayList<ArrayList<Double>> output = (ArrayList<ArrayList<Double>>) analysis.getOutput();
        ArrayList<Double> result = output.get(0);
        // logger.info("BMW dealer cluster analysis result : " + result);
        assertEquals("The result from the analysis is the fourth group in the cluster : ", result.get(4), 1d);
    }

    @Test
    public void destroy() throws Exception {
        Context context = getContext(dataFileName, contextName);
        String url = getAnalyzerUrl(Analyzer.CREATE);
        doPost(url, context, Context.class);

        Analysis analysis = getAnalysis(contextName, null);
        url = getAnalyzerUrl(Analyzer.BUILD);
        doPost(url, analysis, Context.class);

        url = getAnalyzerUrl(Analyzer.CONTEXT);
        context = doPost(url, analysis, Context.class);
        assertNotNull(context);

        String destroyUrl = getAnalyzerUrl(Analyzer.DESTROY);
        doPost(destroyUrl, context, Context.class);

        context = doPost(url, analysis, Context.class);
        assertNull(context);
    }

    @Test
    public void context() throws Exception {
        Context context = getContext(dataFileName, contextName);
        String url = getAnalyzerUrl(Analyzer.CREATE);
        doPost(url, context, Context.class);

        Analysis analysis = getAnalysis(contextName, null);
        url = getAnalyzerUrl(Analyzer.BUILD);
        doPost(url, analysis, Context.class);

        url = getAnalyzerUrl(Analyzer.CONTEXT);
        context = doPost(url, analysis, Context.class);
        assertNotNull(context);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void contexts() throws Exception {
        Context context = getContext(dataFileName, contextName);
        String url = getAnalyzerUrl(Analyzer.CREATE);
        doPost(url, context, Context.class);

        Analysis analysis = getAnalysis(contextName, null);
        url = getAnalyzerUrl(Analyzer.BUILD);
        doPost(url, analysis, Context.class);

        String contextsUrl = getAnalyzerUrl(Analyzer.CONTEXTS);
        String[] contexts = doGet(contextsUrl, String[].class);
        assertTrue(Arrays.toString(contexts).contains(this.contextName));
    }

    @Test
    public void createBuildAnalyzeDestroy() throws Exception {
        Context context = getContext(dataFileName, contextName);
        String url = getAnalyzerUrl(Analyzer.CREATE_BUILD_ANALYZE_DESTROY);
        Analysis<?, ?> analysis = doPost(url, context, Analysis.class);

        @SuppressWarnings("unchecked")
        ArrayList<ArrayList<Double>> output = (ArrayList<ArrayList<Double>>) analysis.getOutput();
        ArrayList<Double> result = output.get(0);
        // Verify that the result is ok, i.e. validate the prediction
        assertEquals("The result from the analysis is the fourth group in the cluster : ", result.get(4), 1d);

        // Verify that the context and the analyzer is removed from the server
        String contextsUrl = getAnalyzerUrl(Analyzer.CONTEXTS);
        String[] contexts = doGet(contextsUrl, String[].class);
        assertFalse(Arrays.toString(contexts).contains(this.contextName));
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    protected String getAnalyzerUrl(final String service) throws MalformedURLException {
        return getServiceUrl(Analyzer.ANALYZER + service);
    }

}