package ikube.web.service;

import ikube.AbstractTest;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.THREAD;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.Arrays;

import static ikube.toolkit.REST.doGet;
import static ikube.toolkit.REST.doPost;
import static junit.framework.Assert.*;

/**
 * Note to self: For some reason this test does not work on the Dell server!!!
 * Update: Does now it seems... :)
 * This test must still be completed and verified, perhaps with all sorts of analytics, like regression etc.
 * Update: Done.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 05-02-2014
 */
@SuppressWarnings("FieldCanBeLocal")
public class AnalyzerIntegration extends AbstractTest {

    private int sleep = 3000;
    private String line = "1,1,0,1,1,0,1,1";
    private String contextName = "bmw-browsers";
    private String dataFileName = "bmw-browsers.arff";

    @Test
    @SuppressWarnings("unchecked")
    public void create() throws Exception {
        Context context = getContext(dataFileName, contextName);
        String url = getAnalyzerUrl(Analyzer.CREATE);
        Context result = doPost(url, context, Context.class);
        // THREAD.sleep(sleep);
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
        // THREAD.sleep(sleep);
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
        THREAD.sleep(sleep);
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

        // TODO: Verify something here...
    }

    @Test
    public void destroy() throws Exception {
        Context context = getContext(dataFileName, contextName);
        String url = getAnalyzerUrl(Analyzer.CREATE);
        doPost(url, context, Context.class);

        Analysis analysis = getAnalysis(contextName, null);
        url = getAnalyzerUrl(Analyzer.BUILD);
        doPost(url, analysis, Context.class);

        analysis = getAnalysis(contextName, line);
        url = getAnalyzerUrl(Analyzer.ANALYZE);
        doPost(url, analysis, Analysis.class);

        analysis = getAnalysis(contextName, null);
        url = getAnalyzerUrl(Analyzer.CONTEXT);
        context = doPost(url, analysis, Context.class);
        // THREAD.sleep(sleep);

        assertNotNull(context);

        String destroyUrl = getAnalyzerUrl(Analyzer.DESTROY);
        doPost(destroyUrl, context, Context.class);
        // THREAD.sleep(sleep);

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

        analysis = getAnalysis(contextName, line);
        url = getAnalyzerUrl(Analyzer.ANALYZE);
        doPost(url, analysis, Analysis.class);

        analysis = getAnalysis(contextName, null);
        url = getAnalyzerUrl(Analyzer.CONTEXT);
        context = doPost(url, analysis, Context.class);
        // THREAD.sleep(sleep);
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
        // THREAD.sleep(sleep);
        assertTrue(Arrays.toString(contexts).contains(this.contextName));
    }

    @Test
    public void createBuildAnalyzeDestroy() throws Exception {
        Context context = getContext(dataFileName, contextName);
        String url = getAnalyzerUrl(Analyzer.CREATE_BUILD_ANALYZE_DESTROY);
        Analysis<?, ?> result = doPost(url, context, Analysis.class);
        // Verify that the result is ok, i.e. validate the prediction
        // Verify that the context and the analyzer is removed from the server
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    protected String getAnalyzerUrl(final String service) throws MalformedURLException {
        return getUrl(Analyzer.ANALYZER + service);
    }

}